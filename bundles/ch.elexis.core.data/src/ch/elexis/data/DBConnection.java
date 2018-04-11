package ch.elexis.data;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.cache.IPersistentObjectCache;
import ch.elexis.core.data.cache.MultiGuavaCache;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.net.NetTool;

/**
 * Class managing a connection to a DB using a {@link JdbcLink}, and also the cache used by
 * {@link PersistentObject} loaded. </br>
 * </br>
 * By introducing this class it is possible to manage {@link PersistentObject} from different DBs in
 * one Elexis instance.
 * 
 * @author thomas
 * 		
 */
public class DBConnection {
	
	private static Logger logger = LoggerFactory.getLogger(DBConnection.class);
	
	public static final int CACHE_DEFAULT_LIFETIME = 15;
	public static final int CACHE_MIN_LIFETIME = 5;
	public static final int CACHE_TIME_MAX = 300;
	
	protected int default_lifetime;
	private IPersistentObjectCache<String> cache;
	
	private JdbcLink jdbcLink;
	
	private String username;
	private String pcname;
	private String tracetable;
	
	private boolean runningFromScratch = false;
	
	private String dbUser;
	private String dbPw;
	private String dbFlavor;
	
	private String dbConnectString;
	private String dbDriver;
	
	private File runFromScratchDB = null;
	
	public DBConnection(){
		default_lifetime =
			CoreHub.localCfg.get(Preferences.ABL_CACHELIFETIME, CACHE_DEFAULT_LIFETIME);
		if (default_lifetime < CACHE_MIN_LIFETIME) {
			default_lifetime = CACHE_MIN_LIFETIME;
			CoreHub.localCfg.set(Preferences.ABL_CACHELIFETIME, CACHE_MIN_LIFETIME);
		}
		
		cache = new MultiGuavaCache<String>(default_lifetime, TimeUnit.SECONDS);
		
		logger.info("Cache setup: default_lifetime " + default_lifetime);
	}
	
	public void setDBPassword(String password){
		this.dbPw = password;
	}
	
	public void setDBUser(String username){
		this.dbUser = username;
	}
	
	public void setDBFlavor(String dbFlavor){
		this.dbFlavor = dbFlavor;
	}
	
	public String getDBFlavor(){
		return jdbcLink.DBFlavor;
	}
	
	public String getDBConnectString(){
		if (jdbcLink != null) {
			return jdbcLink.getConnectString();
		} else {
			return dbConnectString;
		}
	}
	
	public void setDBConnectString(String connectString){
		this.dbConnectString = connectString;
	}
	
	public void setDBDriver(String driver){
		this.dbDriver = driver;
	}
	
	public String getDBDriver(){
		return dbDriver;
	}
	
	/**
	 * Check if the configured values of dbFlavor, dbSpec, dbUser & dbPw allow connecting directly
	 * to a database.
	 * 
	 * @return
	 */
	public boolean isDirectConnectConfigured(){
		return dbFlavor != null && dbFlavor.length() >= 2 && dbConnectString != null
			&& dbConnectString.length() > 5 && dbUser != null && dbPw != null;
	}
	
	/**
	 * Try connecting directly to a database using the configured dbFlavor, dbSpec, dbUser & dbPw
	 * values.
	 * 
	 * @return
	 */
	public boolean directConnect(){
		String msg = "Connecting to DB using " + dbFlavor + " " + dbConnectString + " " + dbUser;
		logger.info(msg);
		
		if (dbFlavor.equalsIgnoreCase("mysql"))
			dbDriver = "com.mysql.jdbc.Driver";
		else if (dbFlavor.equalsIgnoreCase("postgresql"))
			dbDriver = "org.postgresql.Driver";
		else if (dbFlavor.equalsIgnoreCase("h2"))
			dbDriver = "org.h2.Driver";
		else
			dbDriver = "invalid";
		if (!dbDriver.equalsIgnoreCase("invalid")) {
			jdbcLink = new JdbcLink(dbDriver, dbConnectString, dbFlavor);
			boolean ret = jdbcLink.connect(dbUser, dbPw);
			if (ret) {
				logger.debug("Verbunden mit " + dbDriver + ", " + dbConnectString);
			} else {
				logger.debug("Verbindung fehlgeschlagen mit " + dbDriver + ", " + dbConnectString);
			}
			return ret;
		} else {
			msg = "can't connect to test database invalid. dbFlavor" + dbFlavor;
			logger.error(msg);
		}
		return false;
	}
	
	/**
	 * Directly set the {@link JdbcLink} used.
	 * 
	 * @param jdbcLink
	 */
	public void setJdbcLink(JdbcLink jdbcLink){
		this.jdbcLink = jdbcLink;
	}
	
	/**
	 * Create a H2 db {@link JdbcLink}. It is not connected, but driver, connection string, etc. is
	 * configured.
	 * 
	 * @param string
	 */
	public void createH2Link(String string){
		jdbcLink = JdbcLink.createH2Link(string);
	}
	
	/**
	 * Connect to the database using a {@link JdbcLink}.</br>
	 * </br>
	 * Following configurations are possible:</br>
	 * - dbDriver, dbConnectString, dbFlavor, dbUser, dbPass</br>
	 * - dbConnectString, dbUser, dbPass</br>
	 * - jdbcLink, dbUser, dbPass</br>
	 * 
	 * @return success
	 */
	public boolean connect(){
		if (jdbcLink == null && dbDriver != null && dbConnectString != null && dbFlavor != null) {
			jdbcLink = new JdbcLink(dbDriver, dbConnectString, dbFlavor);
		} else if (jdbcLink == null && dbConnectString != null && dbFlavor == null
			&& dbDriver == null) {
			if (parseConnectString()) {
				return directConnect();
			}
		}
		if (jdbcLink != null && dbUser != null && dbPw != null) {
			boolean ret = jdbcLink.connect(dbUser, dbPw);
			if (ret) {
				logger.debug("Verbunden mit " + dbDriver + ", " + dbConnectString);
			} else {
				logger.debug("Verbindung fehlgeschlagen mit " + dbDriver + ", " + dbConnectString);
			}
			return ret;
		}
		return false;
	}
	
	private boolean parseConnectString(){
		if (dbConnectString != null && dbConnectString.length() > 5) {
			String url = dbConnectString;
			String cleanURI = url.substring(5);
			
			URI uri = URI.create(cleanURI);
			setDBFlavor(uri.getScheme());
			return true;
		}
		return false;
	}
	
	public void setRunningFromScratch(boolean runningFromScratch){
		this.runningFromScratch = runningFromScratch;
	}
	
	public boolean isRunningFromScratch(){
		return runningFromScratch;
	}
	
	public void runFromScatch() throws IOException{
		runFromScratchDB = File.createTempFile("elexis", "db");
		logger
			.info("RunFromScratch test database created in " + runFromScratchDB.getAbsolutePath());
		dbUser = "sa";
		dbPw = StringTool.leer;
		jdbcLink = JdbcLink.createH2Link(runFromScratchDB.getAbsolutePath());
	}
	
	public void disconnect(){
		if (jdbcLink.DBFlavor.startsWith("hsqldb")) {
			jdbcLink.exec("SHUTDOWN COMPACT");
		}
		jdbcLink.disconnect();
		logger.info("Verbindung zur Datenbank " + jdbcLink.getConnectString() + " getrennt.");
		jdbcLink = null;
		if (runFromScratchDB != null) {
			File dbFile = new File(runFromScratchDB.getAbsolutePath() + ".h2.db");
			logger.info("Deleting runFromScratchDB was " + runFromScratchDB + " and " + dbFile);
			dbFile.delete();
			runFromScratchDB.delete();
		}
		cache.stat();
	}
	
	public String queryString(String sql){
		return jdbcLink.queryString(sql);
	}
	
	public IPersistentObjectCache<String> getCache(){
		return cache;
	}
	
	public PreparedStatement getPreparedStatement(String sql){
		return jdbcLink.getPreparedStatement(sql);
	}
	
	public void releasePreparedStatement(PreparedStatement statement){
		jdbcLink.releasePreparedStatement(statement);
	}
	
	public Stm getStatement(){
		return jdbcLink.getStatement();
	}
	
	public void releaseStatement(Stm stm){
		jdbcLink.releaseStatement(stm);
	}
	
	public int exec(String sql){
		return jdbcLink.exec(sql);
	}
	
	public String wrapFlavored(String wert){
		return jdbcLink.wrapFlavored(wert);
	}
	
	public boolean isTrace(){
		return tracetable != null;
	}
	
	public int getDefaultLifeTime(){
		return default_lifetime;
	}
	
	public void doTrace(String sql){
		StringBuffer tracer = new StringBuffer();
		tracer.append("INSERT INTO ").append(tracetable);
		tracer.append(" (logtime,Workstation,Username,action) VALUES (");
		tracer.append(System.currentTimeMillis()).append(",");
		tracer.append(pcname).append(",");
		tracer.append(username).append(",");
		tracer.append(JdbcLink.wrap(sql.replace('\'', '/'))).append(")");
		exec(tracer.toString());
	}
	
	public JdbcLink getJdbcLink(){
		return jdbcLink;
	}
	
	public Connection getConnection(){
		return jdbcLink.getConnection();
	}
}
