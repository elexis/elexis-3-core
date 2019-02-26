package ch.elexis.core.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
	
	@Override
	public List<String> getBillingSystemConstants(IBillingSystem billingSystem){
		String bc = ConfigServiceHolder.get().get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/constants", null); //$NON-NLS-1$
		if (bc == null) {
			return Collections.emptyList();
		} else {
			return Arrays.asList(bc.split("#")); //$NON-NLS-1$
		}
	}
	
	@Override
	public String getBillingSystemConstant(IBillingSystem billingSystem, String name){
		List<String> constants = getBillingSystemConstants(billingSystem);
		for (String bc : constants) {
			String[] val = bc.split("="); //$NON-NLS-1$
			if (val[0].equalsIgnoreCase(name)) {
				return val[1];
			}
		}
		return ""; //$NON-NLS-1$
	}
}
