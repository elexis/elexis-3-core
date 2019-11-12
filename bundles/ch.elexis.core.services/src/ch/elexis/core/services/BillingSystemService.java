package ch.elexis.core.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.BillingSystem;
import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.ch.BillingLaw;

@Component
public class BillingSystemService implements IBillingSystemService {
	
	@Reference
	public IConfigService configService;
	
	private static final String CFG_KEY_BILLINGLAW = "defaultBillingLaw";
	
	@Override
	public String getRequirements(IBillingSystem system){
		String value = configService.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ system.getName() + "/bedingungen", null);
		return value;
	}
	
	@Override
	public String getDefaultPrintSystem(IBillingSystem system){
		String value = configService.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ system.getName() + "/standardausgabe", null);
		return value;
	}
	
	@Override
	public List<String> getBillingSystemConstants(IBillingSystem billingSystem){
		String bc = configService.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
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
	
	@Override
	public Optional<IBillingSystem> getBillingSystem(String name){
		String billingSystemName = getConfigurationValue(name, "name", null);
		if (billingSystemName != null) {
			BillingLaw law =
				BillingLaw.valueOf(getConfigurationValue(name, CFG_KEY_BILLINGLAW, null));
			BillingSystem billingSystem = new BillingSystem(name, law);
			// TODO more attributes
			return Optional.of(billingSystem);
		}
		
		return Optional.empty();
	}
	
	@Override
	public List<IBillingSystem> getBillingSystems(){
		// TODO implement
		return null;
	}
	
	@Override
	public IBillingSystem addOrModifyBillingSystem(String name, String serviceCode,
		String defaultPrinter, String requirements, BillingLaw law){
		
		setConfigurationValue(name, "name", name);
		setConfigurationValue(name, "leistungscodes", serviceCode);
		setConfigurationValue(name, "standardausgabe", defaultPrinter);
		setConfigurationValue(name, "bedingungen", requirements);
		setConfigurationValue(name, CFG_KEY_BILLINGLAW, law.name());
		
		return new BillingSystem(name, law);
	}
	
	private String getConfigurationValue(String billingSystemName, String attributeName,
		String defaultIfNotDefined){
		String ret = configService.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystemName + "/" + attributeName, defaultIfNotDefined); //$NON-NLS-1$
		return ret;
	}
	
	private void setConfigurationValue(String billingSystemName, String attributeName,
		String attributeValue){
		String key = Preferences.LEISTUNGSCODES_CFG_KEY + "/" + billingSystemName; //$NON-NLS-1$
		configService.set(key + "/" + attributeName, attributeValue);
	}
	
}
