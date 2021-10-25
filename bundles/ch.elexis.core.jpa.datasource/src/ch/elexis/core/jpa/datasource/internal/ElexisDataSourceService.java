package ch.elexis.core.jpa.datasource.internal;

import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_DATABASE;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_HOST;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_JDBC_PARAMETER_STRING;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_PASSWORD;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_TYPE;
import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.DB_USERNAME;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.eclipse.core.runtime.IStatus;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.jpa.datasource.test.TestDatabaseConnection;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.status.ObjectStatus;
import ch.elexis.core.utils.CoreUtil;

@Component(immediate = true, property = "id=default")
public class ElexisDataSourceService implements IElexisDataSource {
	
	private static Logger log = LoggerFactory.getLogger(ElexisDataSourceService.class);
	
	private static ServiceRegistration<DataSource> servReg;
	private static ElexisPoolingDataSource currentDataSource;
	
	private IStatus connectionStatus;
	
	@Activate
	public void activate(){
		log.debug("@Activate");
		
		if (CoreUtil.isTestMode()) {
			log.warn("- test-mode -");
			connectionStatus = setDBConnection(new TestDatabaseConnection());
			if (!connectionStatus.isOK()) {
				log.error("Error setting db connection", connectionStatus.getMessage());
				System.out.println("ERROR " + connectionStatus.getMessage());
			}
			return;
		}
		
		DBConnection connection = getEnvironmentProvidedDbConnection();
		if (connection != null) {
			log.info("Initializing Database connection via environment variables.");
			connectionStatus = setDBConnection(connection);
			if (!connectionStatus.isOK()) {
				log.error("Error setting db connection", connectionStatus.getMessage());
				System.out.println("ERROR " + connectionStatus.getMessage());
			} 
			return;
		}
	}
	
	@Deactivate
	public void deactivate() {
		log.debug("@Deactivate");
	}
	
	@Override
	public IStatus setDBConnection(DBConnection dbConnection){
		int code = 3;
		if (dbConnection instanceof TestDatabaseConnection) {
			code = 1;
		} else if (dbConnection instanceof ElexisEnvironmentDBConnection) {
			code = 2;
		}
		
		log.debug("setDBConnection [{}] " + dbConnection, code);
		try {
			if (servReg != null) {
				log.info("Unregistering service registration");
				currentDataSource.deactivate();
				servReg.unregister();
				servReg = null;
				currentDataSource = null;
			}
			
			currentDataSource = new ElexisPoolingDataSource(dbConnection);
			currentDataSource.activate();
			Hashtable<String, String> properties = new Hashtable<>();
			properties.put("id", "default");
			servReg = FrameworkUtil.getBundle(getClass()).getBundleContext()
				.registerService(DataSource.class, currentDataSource, properties);
			connectionStatus = new ObjectStatus(IStatus.OK, "ch.elexis.core.jpa.datasource", code,
				"ok", null, dbConnection);
			return connectionStatus;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// Logging might not even been initialized yet
			// so leave the stack trace to sysout
			e.printStackTrace();
			connectionStatus = new ObjectStatus(IStatus.ERROR, "ch.elexis.core.jpa.datasource",
				code, e.getMessage(), e, dbConnection);
			return connectionStatus;
		}
	}
	
	private DBConnection getEnvironmentProvidedDbConnection(){
		Map<String, String> env = System.getenv();
		String dbType = env.get(DB_TYPE);
		String dbHost = env.get(DB_HOST);
		String dbDatabase = env.get(DB_DATABASE);
		String dbUsername = env.get(DB_USERNAME);
		String dbPassword = env.get(DB_PASSWORD);
		
		if (isNotBlank(dbType) && isNotBlank(dbHost) && isNotBlank(dbDatabase)
			&& isNotBlank(dbUsername) && isNotBlank(dbPassword)) {
			Optional<DBType> dbTypeType = DBConnection.DBType.valueOfIgnoreCase(dbType);
			if (dbTypeType.isPresent()) {
				return new ElexisEnvironmentDBConnection(dbTypeType.get(), dbHost, dbDatabase,
					dbUsername, dbPassword, env.get(DB_JDBC_PARAMETER_STRING));
			} else {
				log.warn(
					"Can not resolve dbType [{}], ignoring environment variable set connection",
					dbType);
			}
		}
		return null;
	}
	
	private class ElexisEnvironmentDBConnection extends DBConnection {
		
		private static final long serialVersionUID = -3727881455745909885L;
		
		public ElexisEnvironmentDBConnection(DBType dbType, String dbHost, String dbDatabase,
			String dbUsername, String dbPassword, String jdbcParameterString){
			rdbmsType = dbType;
			databaseName = dbDatabase;
			username = dbUsername;
			password = dbPassword;
			connectionString =
				"jdbc:" + dbType.dbType.toLowerCase() + "://" + dbHost + "/" + dbDatabase;
			if (isNotBlank(jdbcParameterString)) {
				connectionString += "?" + jdbcParameterString;
			}
		}
		
	}

	@Override
	public ObjectStatus getCurrentConnectionStatus(){
		if (connectionStatus instanceof ObjectStatus) {
			return ((ObjectStatus) connectionStatus);
		}
		if (connectionStatus != null) {
			return new ObjectStatus(connectionStatus, null);
		}
		return null;
	}
	
}
