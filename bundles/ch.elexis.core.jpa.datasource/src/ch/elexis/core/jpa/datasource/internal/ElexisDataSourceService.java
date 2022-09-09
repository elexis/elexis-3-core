package ch.elexis.core.jpa.datasource.internal;

import java.util.Hashtable;
import java.util.Optional;

import javax.sql.DataSource;

import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.status.ObjectStatus;

@Component(immediate = true, property = "id=default")
public class ElexisDataSourceService implements IElexisDataSource {

	private static DataSource currentDataSource;

	private IStatus connectionStatus;

	@Activate
	public void activate() {
		DataSourceConnectionParser dataSourceConnectionParser = new DataSourceConnectionParser();
		Optional<DBConnection> dbConnection = dataSourceConnectionParser.parseAvailableParameters();
		if (dbConnection.isPresent()) {
			setDBConnection(dbConnection.get(), dataSourceConnectionParser.getConfigSource());
			connectionStatus = new ObjectStatus(IStatus.OK, "ch.elexis.core.jpa.datasource",
					dataSourceConnectionParser.getConfigSource(), "ok", null, dbConnection);
		}
	}

	private IStatus setDBConnection(DBConnection dbConnection, int connectionSourceCode) {
		try {
			currentDataSource = ProxyPoolingDataSourceBuilder.build(dbConnection);
			Hashtable<String, String> properties = new Hashtable<>();
			properties.put("id", "default");
			FrameworkUtil.getBundle(getClass()).getBundleContext().registerService(DataSource.class, currentDataSource,
					properties);
			connectionStatus = new ObjectStatus(IStatus.OK, "ch.elexis.core.jpa.datasource", connectionSourceCode, "ok",
					null, dbConnection);
			return connectionStatus;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// Logging might not even been initialized yet
			// so leave the stack trace to sysout
			e.printStackTrace();
			connectionStatus = new ObjectStatus(IStatus.ERROR, "ch.elexis.core.jpa.datasource", connectionSourceCode,
					e.getMessage(), e, dbConnection);
			return connectionStatus;
		}
	}

	@Override
	public IStatus setDBConnection(DBConnection dbConnection) {
		return setDBConnection(dbConnection, 0);
	}

//	private DBConnection getEnvironmentProvidedDbConnection() {
//		Map<String, String> env = System.getenv();
//		String dbType = env.get(DB_TYPE);
//		String dbHost = env.get(DB_HOST);
//		String dbDatabase = env.get(DB_DATABASE);
//		String dbUsername = env.get(DB_USERNAME);
//		String dbPassword = env.get(DB_PASSWORD);
//
//		if (isNotBlank(dbType) && isNotBlank(dbHost) && isNotBlank(dbDatabase) && isNotBlank(dbUsername)
//				&& isNotBlank(dbPassword)) {
//			Optional<DBType> dbTypeType = DBConnection.DBType.valueOfIgnoreCase(dbType);
//			if (dbTypeType.isPresent()) {
//				return new ElexisEnvironmentDBConnection(dbTypeType.get(), dbHost, dbDatabase, dbUsername, dbPassword,
//						env.get(DB_JDBC_PARAMETER_STRING));
//			} else {
//				log.warn("Can not resolve dbType [{}], ignoring environment variable set connection", dbType);
//			}
//		}
//		return null;
//	}
//
//	private class ElexisEnvironmentDBConnection extends DBConnection {
//
//		private static final long serialVersionUID = -3727881455745909885L;
//
//		public ElexisEnvironmentDBConnection(DBType dbType, String dbHost, String dbDatabase, String dbUsername,
//				String dbPassword, String jdbcParameterString) {
//			rdbmsType = dbType;
//			databaseName = dbDatabase;
//			username = dbUsername;
//			password = dbPassword;
//			connectionString = "jdbc:" + dbType.dbType.toLowerCase() + "://" + dbHost + "/" + dbDatabase;
//			if (isNotBlank(jdbcParameterString)) {
//				connectionString += "?" + jdbcParameterString;
//			}
//		}
//
//	}

	@Override
	public ObjectStatus getCurrentConnectionStatus() {
		if (connectionStatus instanceof ObjectStatus) {
			return ((ObjectStatus) connectionStatus);
		}
		if (connectionStatus != null) {
			return new ObjectStatus(connectionStatus, null);
		}
		return null;
	}

}
