package ch.elexis.core.services.eenv;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.ElexisEnvironmentPropertyConstants;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.services.IConfigService;

/**
 * Programmatically register the {@link ElexisEnvironmentService} if conditions are met. That is, an
 * Elexis-Environment host value is passed.
 */
@Component
public class ElexisEnvironmentServiceActivator {
	
	private ServiceRegistration<IElexisEnvironmentService> serviceRegistration;
	
	@Reference
	private IConfigService configService;
	
	@Activate
	public void activate(){
		// 1. try via system property
		String elexisEnvironmentHost =
			System.getProperty(ElexisSystemPropertyConstants.EE_HOSTNAME);
		// 2. if empty fetch via environment variable
		if (StringUtils.isBlank(elexisEnvironmentHost)) {
			elexisEnvironmentHost = System.getenv(ElexisEnvironmentPropertyConstants.EE_HOSTNAME);
		}
		// 3. if empty fetch via config service
		if (StringUtils.isBlank(elexisEnvironmentHost)) {
			elexisEnvironmentHost =
				configService.get(IElexisEnvironmentService.CFG_EE_HOSTNAME, null);
		}
		
		Logger log = LoggerFactory.getLogger(getClass());
		
		if (StringUtils.isNotBlank(elexisEnvironmentHost)) {
			try {
				// activate the service
				ElexisEnvironmentService elexisEnvironmentService =
					new ElexisEnvironmentService(elexisEnvironmentHost);
				serviceRegistration =
					FrameworkUtil.getBundle(ElexisEnvironmentServiceActivator.class)
						.getBundleContext().registerService(IElexisEnvironmentService.class,
							elexisEnvironmentService, null);
				
				log.info("Bound to elexis-environment v{} on [{}]",
					elexisEnvironmentService.getVersion(), elexisEnvironmentService.getHostname());
				
			} catch (Exception e) {
				log.warn("Initializing elexis-environment failed", e);
			}
			
		} else {
			log.debug("No elexis-environment configured");
		}
		
	}
	
	@Deactivate
	public void deactivate(){
		if (serviceRegistration != null) {
			serviceRegistration.unregister();
		}
	}
	
}
