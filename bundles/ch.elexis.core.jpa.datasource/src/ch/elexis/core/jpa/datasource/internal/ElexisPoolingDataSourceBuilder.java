package ch.elexis.core.jpa.datasource.internal;

import java.sql.Driver;
import java.time.Duration;
import java.util.Properties;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;

public class ElexisPoolingDataSourceBuilder {

	private static Logger log = LoggerFactory.getLogger(ElexisPoolingDataSourceBuilder.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static PoolingDataSource build(DBConnection dbConnection)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		ObjectPool<PoolableConnection> connectionPool = createConnectionPool(dbConnection);
		return new PoolingDataSource(connectionPool);
	}

	private static ObjectPool<PoolableConnection> createConnectionPool(DBConnection dbConnection)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String driverName = StringUtils.defaultString(dbConnection.rdbmsType.driverName);
		String username = StringUtils.defaultString(dbConnection.username);
		String password = StringUtils.defaultString(dbConnection.password);
		String jdbcString = StringUtils.defaultString(dbConnection.connectionString);

		Driver driver = (Driver) Class.forName(driverName).newInstance();

		Properties properties = new Properties();
		properties.put("user", username);
		properties.put("password", password);

		log.debug("db connection pool [" + driver + ", " + jdbcString + ", " + username + "] initialization");

		ConnectionFactory connectionFactory = new DriverConnectionFactory(driver, jdbcString, properties);

		PoolableConnectionFactory poolableConnectionFactory = new PoolableConnectionFactory(connectionFactory, null);
		poolableConnectionFactory.setDefaultAutoCommit(true);
		poolableConnectionFactory.setDefaultReadOnly(false);
		poolableConnectionFactory.setValidationQuery("SELECT 1;");

		GenericObjectPool<PoolableConnection> connectionPool = new GenericObjectPool<>(poolableConnectionFactory);
		connectionPool.setMaxTotal(32);
		connectionPool.setMinIdle(8);
		connectionPool.setMaxWait(Duration.ofSeconds(10));
		connectionPool.setTestOnBorrow(true);

		poolableConnectionFactory.setPool(connectionPool);

		return connectionPool;

	}

}