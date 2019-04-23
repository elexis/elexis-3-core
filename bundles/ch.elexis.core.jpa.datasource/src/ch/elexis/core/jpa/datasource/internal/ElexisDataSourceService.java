package ch.elexis.core.jpa.datasource.internal;

import java.util.Hashtable;

import javax.sql.DataSource;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.jpa.datasource.test.TestDatabaseConnection;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.core.utils.CoreUtil;

@Component(immediate = true)
public class ElexisDataSourceService implements IElexisDataSource {
	
	private static Logger log = LoggerFactory.getLogger(ElexisDataSourceService.class);
	
	private static ServiceRegistration<DataSource> servReg;
	private static ElexisPoolingDataSource currentDataSource;
	
	@Activate
	public void activate(){
		if (CoreUtil.isTestMode()) {
			IStatus setDBConnection = setDBConnection(new TestDatabaseConnection());
			if(!setDBConnection.isOK()) {
				log.error("Error setting db connection", setDBConnection.getMessage());
			}
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
			servReg = FrameworkUtil.getBundle(getClass()).getBundleContext()
				.registerService(DataSource.class, currentDataSource, new Hashtable<>());
			return Status.OK_STATUS;
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			return new Status(Status.ERROR, "ch.elexis.core.jpa.datasource", e.getMessage());
		}
	}
	
}
