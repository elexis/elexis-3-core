package ch.elexis.core.services.rcp;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;

/**
 * Depending on our setup we either activate the legacy config service using
 * direct database access or the config service that connects to EE via Rest
 */
@Component(service = EEDependentServicesActivator.class, immediate = true)
public class EEDependentServicesActivator {

	public static final String CONFIGSERVICE_LEGACY = "LegacyConfigService";
	public static final String CONFIGSERVICE_EEDEP = "EEDependentConfigService";
	public static final String XIDSERVICE_LEGACY = "LegacyXidService";
	public static final String XIDSERVICE_EEDEP = "EEDependentXidService";

	@Activate
	public void activate(ComponentContext context) {
		if (ElexisSystemPropertyConstants.IS_EE_DEPENDENT_OPERATION_MODE) {
			LoggerFactory.getLogger(getClass()).info("Enabling EE Dependent services");
			context.enableComponent(CONFIGSERVICE_EEDEP);
			context.enableComponent(XIDSERVICE_EEDEP);
		} else {
			context.enableComponent(CONFIGSERVICE_LEGACY);
			context.enableComponent(XIDSERVICE_LEGACY);
		}
	}

}
