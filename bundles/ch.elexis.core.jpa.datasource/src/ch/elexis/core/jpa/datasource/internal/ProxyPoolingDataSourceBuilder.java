package ch.elexis.core.jpa.datasource.internal;

import java.sql.Driver;
import java.time.Duration;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool2.impl.GenericObjectPool;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.jpa.datasource.internal.jfr.JFRQueryExecutionListener;
import ch.elexis.core.utils.CoreUtil;
import net.ttddyy.dsproxy.support.ProxyDataSourceBuilder;

public class ProxyPoolingDataSourceBuilder {

	public static DataSource build(DBConnection dbConnection)
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {

		String driverName = StringUtils.defaultString(dbConnection.rdbmsType.driverName);
		String username = StringUtils.defaultString(dbConnection.username);
		String password = StringUtils.defaultString(dbConnection.password);
		String jdbcString = StringUtils.defaultString(dbConnection.connectionString);

		Driver driver = (Driver) Class.forName(driverName).newInstance();

		Properties properties = new Properties();
		properties.put("user", username);
		properties.put("password", password.toString());

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

		PoolingDataSource<PoolableConnection> poolingDataSource = new PoolingDataSource<>(
				connectionPool);

		JFRQueryExecutionListener jfrQueryExecutionListener = new JFRQueryExecutionListener();

		ProxyDataSourceBuilder proxyDataSourceBuilder = ProxyDataSourceBuilder.create(poolingDataSource)
				.listener(jfrQueryExecutionListener);
		if (CoreUtil.isTestMode()) {
			// in test mode, use QueryCountHolder to get the count state
			proxyDataSourceBuilder.countQuery();
		}
		return proxyDataSourceBuilder.build();
	}

}
