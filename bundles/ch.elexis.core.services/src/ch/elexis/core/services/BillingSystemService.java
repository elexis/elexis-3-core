package ch.elexis.core.services;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.services.holder.ConfigServiceHolder;

@Component
public class BillingSystemService implements IBillingSystemService {
	
	@Override
	public String getRequirements(IBillingSystem system){
		String value = ConfigServiceHolder.get().get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ system.getName() + "/bedingungen", null);
		return value;
	}
	
	/**
	 * 
	 * @param billingSystem
	 * @return
	 * @since 3.6 moved from {@link Fall}
	 */
	public String getDefaultPrintSystem(IBillingSystem system){
		String value = ConfigServiceHolder.get().get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ system.getName() + "/standardausgabe", null);
		return value;
	}
}
