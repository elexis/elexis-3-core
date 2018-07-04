package ch.elexis.core.jpa.datasource.internal;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.utils.CoreUtil;

@Component(property = "name=elexis.datasource")
public class ElexisPoolingDataSource extends PoolingDataSource
		implements DataSource, IElexisDataSource {

	private static Logger log = LoggerFactory.getLogger(ElexisPoolingDataSource.class);

	private ObjectPool<Connection> connectionPool;
	
	public void setDBConnection(DBConnection dbConnection){
		if (connectionPool != null) {
			try {
				connectionPool.close();
			} catch (Exception e) {
				log.error("db pool close error", e);
			}
		}
		connectionPool = createConnectionPool(dbConnection);
		if (connectionPool != null) {
			setPool(connectionPool);
			try (Connection conn = getConnection()) {
				log.info("db connection pool initialization success");
			} catch (SQLException e) {
				log.error("db connection pool initialization error", e);
			}
		} else {
			log.error("db connection pool initialization failed - no connection pool");
		}
	}
	
	@Deactivate
	public void deactivate(){
		if (connectionPool != null) {
			try {
				log.info("Deactivating, closing db connection pool");
				connectionPool.close();
			} catch (Exception e) {
				log.warn("Error closing db connection pool", e);
			}
		}
	}
	
	private ObjectPool<Connection> createConnectionPool(DBConnection dbConnection) {
		String driverName = dbConnection.rdbmsType.driverName;
		String username = dbConnection.username;
		String password = dbConnection.password;
		String connection = dbConnection.connectionString;
		try {
			Driver driver = (Driver) Class.forName(driverName).newInstance();

			Properties properties = new Properties();
			properties.put("user", username);
			properties.put("password", password);

			ConnectionFactory connectionFactory = new DriverConnectionFactory(driver, connection, properties);

			GenericObjectPool<Connection> connectionPool = new GenericObjectPool<>(null);
			connectionPool.setMaxActive(32);
			connectionPool.setMinIdle(2);
			connectionPool.setMaxWait(10000);
			connectionPool.setTestOnBorrow(true);

			new PoolableConnectionFactory(connectionFactory, connectionPool, null, "SELECT 1;", false, true);
			return connectionPool;
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			log.error("db connection pool initialization error", e);
			return null;
		}
	}

	@Override
	public Connection getConnection(String uname, String passwd) throws SQLException {
		return getConnection();
	}

	/**
	 * @return an h2 based test database connection
	 */
	private DBConnection getTestDatabaseConnection(){
		DBConnection retVal = new DBConnection();
		retVal.connectionString = "jdbc:h2:mem:elexisTest;DB_CLOSE_DELAY=-1";
		String trace = System.getProperty("elexis.test.dbtrace");
		if (trace != null && "true".equalsIgnoreCase(trace)) {
			retVal.connectionString += ";TRACE_LEVEL_SYSTEM_OUT=2";
		}
		retVal.rdbmsType = DBType.H2;
		retVal.username = "sa";
		retVal.password = "";
		return retVal;
	}
	
	@Override
	public Connection getConnection() throws SQLException {
		if (CoreUtil.isTestMode() && connectionPool == null) {
			setDBConnection(getTestDatabaseConnection());
		}
		return super.getConnection();
	}
}