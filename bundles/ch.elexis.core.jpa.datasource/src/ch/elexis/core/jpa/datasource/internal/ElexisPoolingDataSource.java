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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;

public class ElexisPoolingDataSource extends PoolingDataSource implements DataSource {
	
	private static Logger log = LoggerFactory.getLogger(ElexisPoolingDataSource.class);
	
	private DBConnection dbConnection;
	
	private ObjectPool<Connection> connectionPool;
	
	public ElexisPoolingDataSource(DBConnection dbConnection){
		this.dbConnection = dbConnection;
	}
	
	public void activate()
		throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		connectionPool = createConnectionPool(dbConnection);
		if (connectionPool != null) {
			setPool(connectionPool);
			try (Connection conn = getConnection()) {
				log.info("db connection pool [" + dbConnection.connectionString
					+ "] initialization success");
			} catch (SQLException e) {
				log.error("db connection pool [" + dbConnection.connectionString
					+ "] initialization error", e);
			}
		} else {
			log.error("db connection pool [" + dbConnection.connectionString
				+ "] initialization failed - no connection pool");
		}
	}
	
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
	
	private ObjectPool<Connection> createConnectionPool(DBConnection dbConnection)
		throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		String driverName = StringUtils.defaultString(dbConnection.rdbmsType.driverName);
		String username = StringUtils.defaultString(dbConnection.username);
		String password = StringUtils.defaultString(dbConnection.password);
		String jdbcString = StringUtils.defaultString(dbConnection.connectionString);
		
		Driver driver = (Driver) Class.forName(driverName).newInstance();
		
		Properties properties = new Properties();
		properties.put("user", username);
		properties.put("password", password);
		
		log.info("db connection pool [" + driver + ", " + jdbcString + ", " + username
			+ "] initialization");
		
		ConnectionFactory connectionFactory =
			new DriverConnectionFactory(driver, jdbcString, properties);
		
		GenericObjectPool<Connection> connectionPool = new GenericObjectPool<>(null);
		connectionPool.setMaxActive(32);
		connectionPool.setMinIdle(8);
		connectionPool.setMaxWait(10000);
		connectionPool.setTestOnBorrow(true);
		
		new PoolableConnectionFactory(connectionFactory, connectionPool, null, "SELECT 1;", false,
			true);
		return connectionPool;
		
	}
	
	@Override
	public Connection getConnection(String uname, String passwd) throws SQLException{
		return getConnection();
	}
	
	@Override
	public Connection getConnection() throws SQLException{
		return super.getConnection();
	}
}