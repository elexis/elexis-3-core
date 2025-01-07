package ch.elexis.core.application.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.extension.ICoreOperationAdvisor;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IElexisDataSource;
import ch.elexis.data.PersistentObjectDataSourceActivator;

@Component
public class DeskServicesHolder {

	private static IAccessControlService accessControlService;

	private static IConfigService configService;

	private static IElexisDataSource elexisDatasource;

	private static PersistentObjectDataSourceActivator poDatasourceActivator;

	private static ICoreOperationAdvisor coreOperationAdvisor;

	@Reference
	public void setAccessControlService(IAccessControlService accessControlService) {
		DeskServicesHolder.accessControlService = accessControlService;
	}

	@Reference
	public void setConfigService(IConfigService configService) {
		DeskServicesHolder.configService = configService;
	}

	@Reference(target = "(id=default)")
	public void setElexisDataSource(IElexisDataSource elexisDatasource) {
		DeskServicesHolder.elexisDatasource = elexisDatasource;
	}

	@Reference
	public void setCoreOperationAdvisor(ICoreOperationAdvisor coreOperationAdvisor) {
		DeskServicesHolder.coreOperationAdvisor = coreOperationAdvisor;
	}

	@Reference
	public void setPersistentObjectDataSourceActivator(PersistentObjectDataSourceActivator poDatasourceActivator) {
		DeskServicesHolder.poDatasourceActivator = poDatasourceActivator;
	}

	public static IAccessControlService getAccessControlService() {
		return accessControlService;
	}

	public static IConfigService getConfigService() {
		return configService;
	}

	public static IElexisDataSource getElexisDatasource() {
		return elexisDatasource;
	}

	public static ICoreOperationAdvisor getCoreOperationAdvisor() {
		return coreOperationAdvisor;
	}

	public static PersistentObjectDataSourceActivator getPersistentObjectDataSourceActivator() {
		return poDatasourceActivator;
	}

	public static void waitForServices(int timeout) {
		while (accessControlService == null || configService == null || elexisDatasource == null
				|| coreOperationAdvisor == null || poDatasourceActivator == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				// ignore
			}
			if ((timeout -= 100) < 0) {
				LoggerFactory.getLogger(DeskServicesHolder.class).error(
						"Missing service accessControlService={} configService={} elexisDatasource={} coreOperationAdvisor={} poDatasourceActivator={}",
						accessControlService, configService, elexisDatasource, coreOperationAdvisor,
						poDatasourceActivator);
				throw new IllegalStateException("Services initializaion timed out");
			}
		}
	}
}
