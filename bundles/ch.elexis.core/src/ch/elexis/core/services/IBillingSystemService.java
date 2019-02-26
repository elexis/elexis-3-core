package ch.elexis.core.services;

import java.util.List;

import ch.elexis.core.model.IBillingSystem;

public interface IBillingSystemService {
	
	/**
	 * Retrieve requirements of a given {@link IBillingSystem}.
	 * 
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 */
	public String getRequirements(IBillingSystem system);
	
	/**
	 * Get the configured default print system name for a given {@link IBillingSystem}.
	 * 
	 * @param billingSystem
	 * @return
	 * @since 3.6 moved from {@link Fall}
	 */
	public String getDefaultPrintSystem(IBillingSystem system);
	
	public List<String> getBillingSystemConstants(IBillingSystem billingSystem);
	
	public String getBillingSystemConstant(IBillingSystem billingSystem, String name);
}
