package ch.elexis.core.model;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.model.util.internal.ModelUtil;

public class BillingSystem implements IBillingSystem {
	
	private static final String CFG_BILLINGLAW = "defaultBillingLaw";
	
	private String name;
	
	public BillingSystem(String name){
		this.name = name;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public void setName(String value){
		name = value;
	}
	
	@Override
	public BillingLaw getLaw(){
		return BillingLaw.valueOf(BillingSystem.getConfigurationValue(getName(),
			BillingSystem.CFG_BILLINGLAW, BillingLaw.KVG.name()));
	}
	
	@Override
	public void setLaw(BillingLaw value){
		throw new UnsupportedOperationException();
	}
	
	private static String getConfigurationValue(String billingSystemName, String attributeName,
		String defaultIfNotDefined){
		String ret = ModelUtil.getConfig(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystemName + "/" + attributeName, defaultIfNotDefined); //$NON-NLS-1$
		return ret;
	}
	
}
