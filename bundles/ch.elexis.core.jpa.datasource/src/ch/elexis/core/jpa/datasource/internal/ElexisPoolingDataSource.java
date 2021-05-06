package ch.elexis.core.jpa.datasource.internal;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;

public class ElexisPoolingDataSource extends PoolingDataSource<PoolableConnection> implements DataSource {
	
	private static Logger log = LoggerFactory.getLogger(ElexisPoolingDataSource.class);
	
	private DBConnection dbConnection;
	
	private GenericObjectPool<PoolableConnection> connectionPool;
	
	public ElexisPoolingDataSource(DBConnection dbConnection){
		super(createConnectionPool(dbConnection));
		this.dbConnection = dbConnection;
	}
	
	public void activate()
		throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		connectionPool = createConnectionPool(dbConnection);
		if (connectionPool != null) {
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
	
	private static GenericObjectPool<PoolableConnection> createConnectionPool(DBConnection dbConnection)
		 {
		String driverName = StringUtils.defaultString(dbConnection.rdbmsType.driverName);
		String username = StringUtils.defaultString(dbConnection.username);
		String password = StringUtils.defaultString(dbConnection.password);
		String jdbcString = StringUtils.defaultString(dbConnection.connectionString);
		
		Driver driver = null;
		try {
			driver = (Driver) Class.forName(driverName).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			e.printStackTrace();
			log.error("Error getting database driver class", e);
		}
		
		Properties properties = new Properties();
		properties.put("user", username);
		properties.put("password", password);
		
		log.info("db connection pool [" + driver + ", " + jdbcString + ", " + username
			+ "] initialization");
		
		ConnectionFactory connectionFactory =
			new DriverConnectionFactory(driver, jdbcString, properties);
		
		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
		poolableConnectionFactory.setValidationQuery("SELECT 1;");
		poolableConnectionFactory.setDefaultReadOnly(Boolean.FALSE);
		poolableConnectionFactory.setDefaultAutoCommit(Boolean.TRUE);
		
		GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<PoolableConnection>(poolableConnectionFactory);
		connectionPool.setMaxTotal(32);
		connectionPool.setMinIdle(8);
		connectionPool.setMaxWaitMillis(10000);
		connectionPool.setTestOnBorrow(true);
		poolableConnectionFactory.setPool(connectionPool);
	
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