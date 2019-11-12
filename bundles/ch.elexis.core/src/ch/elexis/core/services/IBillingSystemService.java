package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.ch.BillingLaw;

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
	 */
	public String getDefaultPrintSystem(IBillingSystem system);
	
	public List<String> getBillingSystemConstants(IBillingSystem billingSystem);
	
	public String getBillingSystemConstant(IBillingSystem billingSystem, String name);
	
	/**
	 * Find a billing system by its name
	 * 
	 * @param gesetz
	 * @return
	 */
	public Optional<IBillingSystem> getBillingSystem(String gesetz);
	
	/**
	 * Find all installed billing systems.
	 * 
	 * @return an Array with the names of all configured billing systems
	 */
	public List<IBillingSystem> getBillingSystems();
	
	/**
	 * Adds a new {@link IBillingSystem} or modifies the parameters of an already existing
	 * 
	 * @param name to identify the billing system
	 * @param serviceCode
	 * @param defaultPrinter
	 * @param requirements
	 * @param law
	 * @return the {@link IBillingSystem} just added or modified
	 */
	public IBillingSystem addOrModifyBillingSystem(String name, String serviceCode,
		String defaultPrinter, String requirements, BillingLaw law);
}
