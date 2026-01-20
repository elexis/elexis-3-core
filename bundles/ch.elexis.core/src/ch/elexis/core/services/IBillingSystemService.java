package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IBillingSystem;
import ch.elexis.core.model.ch.BillingLaw;

public interface IBillingSystemService {

	/**
	 * Retrieve optionals of a given {@link IBillingSystem}.
	 *
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D
	 *         for Kontakt, Text, Date TM Text Multiline TS Text Styled CS Combo
	 *         saved as string CN Combo saved as numeric (selected index) LS List
	 *         items, saved as strings, tab-delimited LN List items, saved as
	 *         numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric,
	 *         selected index
	 */
	public String getOptionals(IBillingSystem system);

	/**
	 * Retrieve requirements of a given {@link IBillingSystem}.
	 *
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D
	 *         for Kontakt, Text, Date TM Text Multiline TS Text Styled CS Combo
	 *         saved as string CN Combo saved as numeric (selected index) LS List
	 *         items, saved as strings, tab-delimited LN List items, saved as
	 *         numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric,
	 *         selected index
	 */
	public String getRequirements(IBillingSystem system);

	/**
	 * Get the configured default print system name for a given
	 * {@link IBillingSystem}.
	 *
	 * @param billingSystem
	 * @return
	 */
	public String getDefaultPrintSystem(IBillingSystem system);

	/**
	 * Get the configured default insurance reason for a given
	 * {@link IBillingSystem}. If no configuration is found default value based on
	 * the law is returned.
	 *
	 * @param system
	 * @return
	 */
	public String getDefaultInsuranceReason(IBillingSystem system);

	public List<String> getBillingSystemConstants(IBillingSystem billingSystem);

	public String getBillingSystemConstant(IBillingSystem billingSystem, String name);

	/**
	 * Returns the default billing system, which is always asserted to be available
	 *
	 * @return
	 */
	public IBillingSystem getDefaultBillingSystem();

	/**
	 * Find a billing system by its name
	 *
	 * @param name
	 * @return
	 */
	public Optional<IBillingSystem> getBillingSystem(String name);

	/**
	 * Find all installed billing systems.
	 *
	 * @return an Array with the names of all configured billing systems
	 */
	public List<IBillingSystem> getBillingSystems();

	/**
	 * Adds a new {@link IBillingSystem} or modifies the parameters of an already
	 * existing
	 *
	 * @param name           to identify the billing system
	 * @param defaultPrinter
	 * @param requirements
	 * @param law
	 * @return the {@link IBillingSystem} just added or modified
	 */
	public IBillingSystem addOrModifyBillingSystem(String name, String defaultPrinter, String requirements,
			BillingLaw law);

	/**
	 * Get a matching {@link BillingLaw} instance for the provided law short name.
	 *
	 * @param law
	 * @return
	 */
	public BillingLaw getBillingLaw(String law);

	/**
	 * returns true if the billing system specified by the param is DISabled else
	 * returns false
	 *
	 * @param billingSystem String, the name of the billing system to be tested
	 * @since 3.13 moved from ch.elexis.data.BillingSystem
	 */
	public boolean isDisabled(IBillingSystem billingSystem);

	/**
	 * 
	 * @param billingSystem
	 * @return
	 * @since 3.13 moved from ch.elexis.data.BillingSystem
	 */
	public boolean isCostBearerDisabled(IBillingSystem billingSystem);

	/**
	 * Retrieve unused/saved definitions of previously used required and optional
	 * field for a given billingSystem
	 *
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D
	 *         for Kontakt, Text, Date TM Text Multiline TS Text Styled CS Combo
	 *         saved as string CN Combo saved as numeric (selected index) LS List
	 *         items, saved as strings, tab-delimited LN List items, saved as
	 *         numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric,
	 *         selected index
	 * @since 3.6 moved from ch.elexis.data.Fall
	 * @since 3.13 moved from ch.elexis.data.BillingSystem
	 */
	public String getUnused(IBillingSystem billingSystem);
}
