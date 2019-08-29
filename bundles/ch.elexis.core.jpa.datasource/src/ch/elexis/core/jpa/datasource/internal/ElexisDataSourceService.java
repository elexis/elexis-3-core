package ch.elexis.core.jpa.datasource.internal;

import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;

import static ch.elexis.core.constants.ElexisEnvironmentPropertyConstants.*;
import ch.elexis.core.jpa.datasource.test.TestDatabaseConnection;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.utils.CoreUtil;

@Component(immediate = true, property = "id=default")
public class ElexisDataSourceService implements IElexisDataSource {
	
	private static Logger log = LoggerFactory.getLogger(ElexisDataSourceService.class);
	
	private static ServiceRegistration<DataSource> servReg;
	private static ElexisPoolingDataSource currentDataSource;
	
	@Activate
	public void activate(){
		System.out.println("Activating ElexisDataSourceService ...");
		log.debug("Activating ...");
		if (CoreUtil.isTestMode()) {
			log.warn("- test-mode -");
			IStatus setDBConnection = setDBConnection(new TestDatabaseConnection());
			if (!setDBConnection.isOK()) {
				log.error("Error setting db connection", setDBConnection.getMessage());
				System.out.println("ERROR " + setDBConnection.getMessage());
			}
			return;
		}
		
		DBConnection connection = getEnvironmentProvidedDbConnection();
		if (connection != null) {
			log.info("Initializing Database connection via environment variables.");
			setDBConnection(connection);
		}
		
	}
	
	public IStatus setDBConnection(DBConnection dbConnection){
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
			return Status.OK_STATUS;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			// Logging might not even been initialized yet
			// so leave the stack trace to sysout
			e.printStackTrace();
			
			return new Status(Status.ERROR, "ch.elexis.core.jpa.datasource", e.getMessage());
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
					dbUsername, dbPassword);
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
			String dbUsername, String dbPassword){
			rdbmsType = dbType;
			databaseName = dbDatabase;
			username = dbUsername;
			password = dbPassword;
			connectionString = "jdbc:" + dbType.driverName + "//" + dbHost + "/" + dbDatabase;
		}
		
	}
	
}
