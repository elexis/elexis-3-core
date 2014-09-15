package ch.elexis.core.scheduler.internal;

import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;

import org.quartz.utils.ConnectionProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.util.SqlRunner;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.JdbcLink;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

/**
 * Connects the Quartz back-end to the database using PersistentObject#JdbcLink. As it is not
 * guaranteed that a {@link JdbcLink} is available at any time, we fall back to our own instatiation
 * if necessary.
 * 
 */
public class PersistentObjectConnectionProvider implements ConnectionProvider {
	
	private static Logger log = LoggerFactory.getLogger(PersistentObjectConnectionProvider.class);
	
	@Override
	public Connection getConnection() throws SQLException{
		JdbcLink jdbcLink = PersistentObject.getConnection();
		if (jdbcLink != null) {
			return jdbcLink.getConnection();
		} else {
			return generateOwnJdbcLink().getConnection();
		}
	}
	
	private JdbcLink generateOwnJdbcLink(){
		// the connection was not yet initialized, so we build our own one
		Hashtable<Object, Object> hConn = PersistentObject.getConnectionHashtable();
		
		String driver =
			PersistentObject
				.checkNull((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_DRIVER));
		String connectstring =
			PersistentObject.checkNull((String) hConn
				.get(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING));
		
		String flavor =
			PersistentObject.checkNull((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_TYPE));
		String username =
			PersistentObject.checkNull((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_USER));
		String password =
			PersistentObject.checkNull((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_PASS));
		
		log.warn("Building own JdbcLink using " + connectstring);
		
		JdbcLink jdbcLink = new JdbcLink(driver, connectstring, flavor);
		jdbcLink.connect(username, password);
		return jdbcLink;
	}
	
	@Override
	public void shutdown() throws SQLException{}
	
	@Override
	/**
	 * Quartz requires its own database persisted back-end; we create the required tables
	 * on startup
	 */
	public void initialize() throws SQLException{
		JdbcLink jdbcLink = PersistentObject.getConnection();

		if (jdbcLink == null) {
			jdbcLink = generateOwnJdbcLink();
		} 
		
		Connection connection = jdbcLink.getConnection();
		
		DatabaseMetaData meta = connection.getMetaData();
		ResultSet tables = meta.getTables(null, null, "QRTZ_LOCKS", new String[] {
			"TABLE"
		});
		if (!tables.next()) {
			// we did not find a table called "QRTZ_LOCKS"
			initializeDatabase(jdbcLink);
		}
		
		tables.close();
		connection.close();
	}
	
	/**
	 * initialize the database with the required tables for the Quartz JobStore
	 * @param connection 
	 */
	private void initializeDatabase(JdbcLink jdbcl){
		log.warn("Quartz tables not found, initializing database");
		
		log.debug("Initializing quartz tables for " + jdbcl.DBFlavor);
		
		URL sqlStringUrl = null;

		switch (jdbcl.DBFlavor) {
		case JdbcLink.DBFLAVOR_MYSQL:
			sqlStringUrl = Activator.getResource("rsc/tables_mysql.sql");
			break;
		case JdbcLink.DBFLAVOR_POSTGRESQL:
			sqlStringUrl = Activator.getResource("rsc/tables_postgres.sql");
			break;
		case JdbcLink.DBFLAVOR_H2:
			sqlStringUrl = Activator.getResource("rsc/tables_h2.sql");
			break;
		default:
			log.error("Invalid database flavor");
			break;
		}
		
		String[] sqlA = new String[1];
		try {
			sqlA[0] = Resources.toString(sqlStringUrl, Charsets.UTF_8);
		} catch (IOException e) {
			log.error("Error opening database initialization script", e);
		}
		SqlRunner runner = new SqlRunner(sqlA, Activator.PLUGIN_ID, jdbcl);
		runner.runSql();
	}
	
}
