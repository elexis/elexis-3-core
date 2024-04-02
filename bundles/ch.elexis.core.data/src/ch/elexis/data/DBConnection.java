package ch.elexis.data;

import java.io.IOException;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.cache.IPersistentObjectCache;
import ch.elexis.core.data.cache.MultiGuavaCache;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.StringTool;

/**
 * Class managing a connection to a DB using a {@link JdbcLink}, and also the
 * cache used by {@link PersistentObject} loaded. </br>
 * </br>
 * By introducing this class it is possible to manage {@link PersistentObject}
 * from different DBs in one Elexis instance.
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

	public DBConnection() {
		default_lifetime = CoreHub.localCfg.get(Preferences.ABL_CACHELIFETIME, CACHE_DEFAULT_LIFETIME);
		if (default_lifetime < CACHE_MIN_LIFETIME) {
			default_lifetime = CACHE_MIN_LIFETIME;
			CoreHub.localCfg.set(Preferences.ABL_CACHELIFETIME, CACHE_MIN_LIFETIME);
		}

		cache = new MultiGuavaCache<>(default_lifetime, TimeUnit.SECONDS);

		logger.info("Cache setup: default_lifetime " + default_lifetime);
	}

	public void setDBPassword(String password) {
		this.dbPw = password;
	}

	public void setDBUser(String username) {
		this.dbUser = username;
	}

	public void setDBFlavor(String dbFlavor) {
		this.dbFlavor = dbFlavor;
	}

	public String getDBFlavor() {
		return jdbcLink.DBFlavor;
	}

	public String getDBConnectString() {
		if (jdbcLink != null) {
			return jdbcLink.getConnectString();
		} else {
			return dbConnectString;
		}
	}

	/**
	 * @since 3.10
	 */
	public String getRawDBConnectString() {
		return dbConnectString;
	}

	public void setDBConnectString(String connectString) {
		this.dbConnectString = connectString;
	}

	public void setDBDriver(String driver) {
		this.dbDriver = driver;
	}

	public String getDBDriver() {
		return dbDriver;
	}

	/**
	 * Check if the configured values of dbFlavor, dbSpec, dbUser & dbPw allow
	 * connecting directly to a database.
	 *
	 * @return
	 */
	public boolean isDirectConnectConfigured() {
		return dbFlavor != null && dbFlavor.length() >= 2 && dbConnectString != null && dbConnectString.length() > 5
				&& dbUser != null && dbPw != null;
	}

	/**
	 * Try connecting directly to a database using the configured dbFlavor, dbSpec,
	 * dbUser & dbPw values.
	 *
	 * @return
	 */
	public boolean directConnect() {
		String msg = "Connecting to DB using " + dbFlavor + StringUtils.SPACE + dbConnectString + StringUtils.SPACE
				+ dbUser;
		logger.info(msg);

		if (dbFlavor.equalsIgnoreCase("mysql"))
			dbDriver = JdbcLink.MYSQL_DRIVER_CLASS_NAME;
		else if (dbFlavor.equalsIgnoreCase("postgresql"))
			dbDriver = JdbcLink.POSTGRESQL_DRIVER_CLASS_NAME;
		else if (dbFlavor.equalsIgnoreCase("h2"))
			dbDriver = JdbcLink.H2_DRIVER_CLASS_NAME;
		else
			dbDriver = "invalid";
		if (!dbDriver.equalsIgnoreCase("invalid")) {
			if (dbConnectString.startsWith("jdbc:h2:")
					&& System.getProperty(ElexisSystemPropertyConstants.CONN_DB_H2_AUTO_SERVER) != null
					&& !dbConnectString.contains(";AUTO_SERVER")) {
				dbConnectString = dbConnectString + ";AUTO_SERVER=TRUE";
			}
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
	public void setJdbcLink(JdbcLink jdbcLink) {
		this.jdbcLink = jdbcLink;
	}

	/**
	 * Create a H2 db {@link JdbcLink}. It is not connected, but driver, connection
	 * string, etc. is configured.
	 *
	 * @param string
	 */
	public void createH2Link(String string) {
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
	public boolean connect() {
		if (jdbcLink == null && dbDriver != null && dbConnectString != null && dbFlavor != null) {
			applyMySqlTimeZoneWorkaround();
			jdbcLink = new JdbcLink(dbDriver, dbConnectString, dbFlavor);
		} else if (jdbcLink == null && dbConnectString != null && dbFlavor == null && dbDriver == null) {
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

	private boolean parseConnectString() {
		if (dbConnectString != null && dbConnectString.length() > 5) {
			String url = dbConnectString;
			String cleanURI = url.substring(5);

			URI uri = URI.create(cleanURI);
			setDBFlavor(uri.getScheme());
			return true;
		}
		return false;
	}

	public void setRunningFromScratch(boolean runningFromScratch) {
		this.runningFromScratch = runningFromScratch;
	}

	public boolean isRunningFromScratch() {
		return ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH
				.equals(System.getProperty(ElexisSystemPropertyConstants.RUN_MODE));
	}

	public void runFromScatch() throws IOException {
		logger.info("RunFromScratch test database created in mem");
		dbUser = "sa";
		dbPw = StringTool.leer;
		jdbcLink = JdbcLink.createH2Link("jdbc:h2:mem:elexisFromScratch;DB_CLOSE_DELAY=-1");
	}

	public void disconnect() {
		if (jdbcLink.DBFlavor != null && jdbcLink.DBFlavor.startsWith("hsqldb")) {
			jdbcLink.exec("SHUTDOWN COMPACT");
		}
		jdbcLink.disconnect();
		logger.info("Verbindung zur Datenbank " + jdbcLink.getConnectString() + " getrennt.");
		jdbcLink = null;
		cache.stat();
	}

	public String queryString(String sql) {
		return jdbcLink.queryString(sql);
	}

	public IPersistentObjectCache<String> getCache() {
		return cache;
	}

	public PreparedStatement getPreparedStatement(String sql) {
		return jdbcLink.getPreparedStatement(sql);
	}

	public void releasePreparedStatement(PreparedStatement statement) {
		jdbcLink.releasePreparedStatement(statement);
	}

	public Stm getStatement() {
		return jdbcLink.getStatement();
	}

	public void releaseStatement(Stm stm) {
		jdbcLink.releaseStatement(stm);
	}

	public int exec(String sql) {
		return jdbcLink.exec(sql);
	}

	public String wrapFlavored(String wert) {
		return jdbcLink.wrapFlavored(wert);
	}

	public boolean isTrace() {
		return tracetable != null;
	}

	public int getDefaultLifeTime() {
		return default_lifetime;
	}

	public void doTrace(String sql) {
		StringBuffer tracer = new StringBuffer();
		tracer.append("INSERT INTO ").append(tracetable);
		tracer.append(" (logtime,Workstation,Username,action) VALUES (");
		tracer.append(System.currentTimeMillis()).append(",");
		tracer.append(pcname).append(",");
		tracer.append(username).append(",");
		tracer.append(JdbcLink.wrap(sql.replace('\'', '/'))).append(")");
		exec(tracer.toString());
	}

	public JdbcLink getJdbcLink() {
		return jdbcLink;
	}

	public Connection getConnection() {
		return jdbcLink.getConnection();
	}

	/**
	 * @since 3.7 due to mysql jdbc update a timezone problem may exist, see e.g.
	 *        https://github.com/elexis/elexis-3-core/issues/273 - we fix this by
	 *        adding this parameter if not yet included
	 */
	public void applyMySqlTimeZoneWorkaround() {
		if (dbFlavor.equalsIgnoreCase("mysql") && !dbConnectString.contains("serverTimezone")) {
			if (dbConnectString.contains("?")) {
				dbConnectString += "&serverTimezone=Europe/Zurich";
			} else {
				dbConnectString += "?serverTimezone=Europe/Zurich";
			}
			logger.info("MySQL dbConnection string correction [{}]", dbConnectString);
		}
	}
}
