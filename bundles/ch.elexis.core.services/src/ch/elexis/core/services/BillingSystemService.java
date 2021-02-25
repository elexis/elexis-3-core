package ch.elexis.core.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.BillingSystem;
import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.ch.BillingLaw;

@Component
public class BillingSystemService implements IBillingSystemService {
	
	@Reference
	public IConfigService configService;
	
	private LoadingCache<String, BillingSystem> cache;
	
	private static final String CFG_KEY_BILLINGLAW = "defaultBillingLaw";
	
	public BillingSystemService(){
		cache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.SECONDS)
			.build(new BillingSystemLoader());
	}
	
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
	public IBillingSystem getDefaultBillingSystem(){
		Optional<IBillingSystem> billingSystem = getBillingSystem("KVG");
		if (billingSystem.isPresent()) {
			return billingSystem.get();
		}
		return addOrModifyBillingSystem(Messages.Fall_KVG_Name, Messages.Fall_TarmedLeistung,
			Messages.Fall_TarmedPrinter, Messages.Fall_KVGRequirements, BillingLaw.KVG);
	}
	
	@Override
	public Optional<IBillingSystem> getBillingSystem(String name){
		try {
			if (name != null) {
				BillingSystem ret = cache.get(name);
				if (ret != BillingSystem.UNKNOWN) {
					return Optional.of(ret);
				}
			}
		} catch (ExecutionException e) {
			LoggerFactory.getLogger(getClass()).warn("Error getting billing system [" + name + "]",
				e);
		}

		return Optional.empty();
	}
	
	private class BillingSystemLoader extends CacheLoader<String, BillingSystem> {
		
		@Override
		public BillingSystem load(String key) throws Exception{
			String billingSystemName = getConfigurationValue(key, "name", null);
			if (billingSystemName != null) {
				String configuredLaw = getConfigurationValue(key, CFG_KEY_BILLINGLAW, null);
				// compatibility with changed BillingLaw enum (ticket #15019)
				if ("MVG".equals(configuredLaw)) {
					configuredLaw = "MV";
				}
				if ("IVG".equals(configuredLaw)) {
					configuredLaw = "IV";
				}
				if (configuredLaw != null) {
					BillingLaw law = BillingLaw.valueOf(configuredLaw);
					BillingSystem billingSystem = new BillingSystem(key, law);
					// TODO more attributes
					return billingSystem;
				} else {
					LoggerFactory.getLogger(getClass())
						.warn("Could not determine law for billing system [" + key + "]");
				}
			}
			return BillingSystem.UNKNOWN;
		}
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
		
		cache.invalidateAll();
		
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
