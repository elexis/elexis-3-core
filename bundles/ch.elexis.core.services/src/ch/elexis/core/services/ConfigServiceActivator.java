package ch.elexis.core.services;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.constants.ElexisSystemPropertyConstants;

/**
 * Depending on our setup we either activate the legacy config service using
 * direct database access or the config service that connects to EE via Rest
 */
@Component(service = ConfigServiceActivator.class, immediate = true)
public class ConfigServiceActivator {

	public static final String LEGACY = "LegacyConfigService";
	public static final String EEDEP = "EEDependentConfigService";

	@Activate
	public void activate(ComponentContext context) {
		if (ElexisSystemPropertyConstants.IS_EE_DEPENDENT_OPERATION_MODE) {
			context.enableComponent(EEDEP);
		} else {
			context.enableComponent(LEGACY);
		}
	}

}
