/*******************************************************************************
 * Copyright (c) 2005-2017, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   MEDEVIT - initial implementation
 *******************************************************************************/
package ch.elexis.data;

import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.util.Extensions;
import ch.rgw.tools.StringTool;

/**
 * A BilingSystem defines a way to invoice services. Each {@link Fall} adheres to one
 * {@link BillingSystem} configured for it.
 * 
 * @since 3.6
 */
public class BillingSystem {
	
	private static Logger log = LoggerFactory.getLogger(BillingSystem.class);
	
	public static final String CFG_BILLINGLAW = "defaultBillingLaw";
	public static final String CFG_NOCOSTBEARER = "noCostBearer";
	
	public static final String KVG_NAME = Messages.Fall_KVG_Name;
	public static final String UVG_NAME = Messages.Fall_UVG_Name;
	public static final String MV_NAME = Messages.Fall_MV_Name;
	public static final String IV_NAME = Messages.Fall_IV_Name;
	private static final String KVG_REQUIREMENTS = Messages.Fall_KVGRequirements; //$NON-NLS-1$
	public static final String UVG_REQUIREMENTS = Messages.Fall_UVGRequirements; //$NON-NLS-1$
	public static final String CONST_TARMED_DRUCKER = Messages.Fall_TarmedPrinter; //$NON-NLS-1$
	public static final String CONST_TARMED_LEISTUNG = Messages.Fall_TarmedLeistung; //$NON-NLS-1$
	public static final String VVG_NAME = Messages.Fall_VVG_Name;
	public static final String PRIVATE_NAME = Messages.Fall_Private_Name; //$NON-NLS-1$	
	
	public static String getConfigurationValue(String billingSystemName, String attributeName,
		String defaultIfNotDefined){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystemName + "/" + attributeName, defaultIfNotDefined); //$NON-NLS-1$
		return ret;
	}
	
	public static void setConfigurationValue(String billingSystemName, String attributeName,
		String attributeValue){
		String key = Preferences.LEISTUNGSCODES_CFG_KEY + "/" + billingSystemName; //$NON-NLS-1$
		CoreHub.globalCfg.set(key + "/" + attributeName, attributeValue);
	}
	
	/**
	 * returns true if the billing system specified by the param is DISabled else returns false
	 * 
	 * @param billingSystem
	 *            String, the name of the billing system to be tested
	 */
	public static boolean isDisabled(final String billingSystemName){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystemName + "/disabled", "0"); //$NON-NLS-1$ //$NON-NLS-2$
		return !ret.equalsIgnoreCase("0");
	}
	
	/**
	 * Retrieve unused/saved definitions of previously used required and optional field for a given
	 * billingSystem
	 * 
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 * @since 3.6 moved from {@link Fall}
	 */
	public static String getUnused(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/unused", null); //$NON-NLS-1$
		return ret;
	}
	
	/**
	 * Retrieve optionals of a given billingSystem
	 * 
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 * @since 3.6 moved from {@link Fall}
	 */
	public static String getOptionals(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/fakultativ", null); //$NON-NLS-1$
		return ret;
	}
	
	/**
	 * Retrieve requirements of a given billingSystem
	 * 
	 * @param billingSystem
	 * @return a ; separated String of fields name:type where type is one of K,T,D for Kontakt,
	 *         Text, Date TM Text Multiline TS Text Styled CS Combo saved as string CN Combo saved
	 *         as numeric (selected index) LS List items, saved as strings, tab-delimited LN List
	 *         items, saved as numerics, tab-delimited (selected indexes) X CheckBox always saved as
	 *         numeric RS Radios, saved as string RN Radios, saved as numeric, selected index
	 * @since 3.6 moved from {@link Fall}
	 */
	public static String getRequirements(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/bedingungen", null); //$NON-NLS-1$
		return ret;
	}
	
	/**
	 * 
	 * @param billingSystem
	 * @return
	 * @since 3.6 moved from {@link Fall}
	 */
	public static String[] getBillingSystemConstants(final String billingSystem){
		String bc = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/constants", null); //$NON-NLS-1$
		if (bc == null) {
			return new String[0];
		} else {
			return bc.split("#"); //$NON-NLS-1$
		}
	}
	
	/**
	 * 
	 * @param billingSystem
	 * @param constant
	 * @return
	 * @since 3.6 moved from {@link Fall}
	 */
	public static String getBillingSystemConstant(final String billingSystem,
		final String constant){
		String[] c = getBillingSystemConstants(billingSystem);
		for (String bc : c) {
			String[] val = bc.split("="); //$NON-NLS-1$
			if (val[0].equalsIgnoreCase(constant)) {
				return val[1];
			}
		}
		return ""; //$NON-NLS-1$
	}
	
	/**
	 * add a billing system constant
	 * 
	 * @param billingSystem
	 *            the Billing system
	 * @param constant
	 *            a String of the form name=value
	 * @since 3.6 moved from {@link Fall}
	 */
	public static void addBillingSystemConstant(final String billingSystem, final String constant){
		if (constant.indexOf('=') != -1) {
			String bc = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
				+ billingSystem + "/constants", null); //$NON-NLS-1$
			if (bc != null) {
				bc += "#" + constant; //$NON-NLS-1$
			} else {
				bc = constant;
			}
			CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/" + billingSystem //$NON-NLS-1$
				+ "/constants", bc); //$NON-NLS-1$
		}
	}
	
	/**
	 * 
	 * @param billingSystem
	 * @param constant
	 * @since 3.6 moved from {@link Fall}
	 */
	public static void removeBillingSystemConstant(final String billingSystem,
		final String constant){
		String bc = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/constants", null); //$NON-NLS-1$
		bc = bc.replaceAll(constant, ""); //$NON-NLS-1$
		bc = bc.replaceAll("##", "#"); //$NON-NLS-1$ //$NON-NLS-2$
		bc = bc.replaceFirst("#$", ""); //$NON-NLS-1$ //$NON-NLS-2$
		bc = bc.replaceFirst("^#", ""); //$NON-NLS-1$ //$NON-NLS-2$
		CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/" + billingSystem //$NON-NLS-1$
			+ "/constants", bc); //$NON-NLS-1$
	}
	
	public static boolean isCostBearerDisabled(String billingSystemName){
		return Boolean.valueOf(BillingSystem.getConfigurationValue(billingSystemName,
			CFG_NOCOSTBEARER, Boolean.FALSE.toString()));
	}
	
	/**
	 * Find all installed billing systems. If we do not find any, we assume that this is an old
	 * installation and try to update. If we find a tarmed-Plugin installed, we create
	 * default-tarmed billings.
	 * 
	 * @return an Array with the names of all configured billing systems
	 * @since 3.6 moved from {@link Fall}
	 */
	public static String[] getAbrechnungsSysteme(){
		String[] ret = CoreHub.globalCfg.nodes(Preferences.LEISTUNGSCODES_CFG_KEY);
		if ((ret == null) || (ret.length == 0)) {
			List<IConfigurationElement> list =
				Extensions.getExtensions(ExtensionPointConstantsData.RECHNUNGS_MANAGER); //$NON-NLS-1$
			for (IConfigurationElement ic : list) {
				if (ic.getAttribute("name").startsWith("Tarmed")) { //$NON-NLS-1$ //$NON-NLS-2$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/name", //$NON-NLS-1$
						KVG_NAME);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/gesetz", //$NON-NLS-1$
						"KVG"); //$NON-NLS-1$
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/leistungscodes", //$NON-NLS-1$
						CONST_TARMED_LEISTUNG);
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/standardausgabe", //$NON-NLS-1$
						CONST_TARMED_DRUCKER);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/KVG/bedingungen", //$NON-NLS-1$
						KVG_REQUIREMENTS);
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/name", //$NON-NLS-1$
						UVG_NAME);
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/leistungscodes", //$NON-NLS-1$
						CONST_TARMED_LEISTUNG);
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/standardausgabe", //$NON-NLS-1$
						CONST_TARMED_DRUCKER);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/bedingungen", //$NON-NLS-1$
						UVG_REQUIREMENTS);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/UVG/gesetz", //$NON-NLS-1$
						"UVG"); //$NON-NLS-1$
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/name", IV_NAME); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/leistungscodes", //$NON-NLS-1$
						CONST_TARMED_LEISTUNG);
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/standardausgabe", //$NON-NLS-1$
						CONST_TARMED_DRUCKER);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/bedingungen", //$NON-NLS-1$
						"KostentrÃ¤ger:K;Fallnummer:T"); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/IV/gesetz", //$NON-NLS-1$
						"IVG"); //$NON-NLS-1$
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/name", MV_NAME); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/leistungscodes", //$NON-NLS-1$
						CONST_TARMED_LEISTUNG);
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/standardausgabe", //$NON-NLS-1$
						CONST_TARMED_DRUCKER);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/bedingungen", //$NON-NLS-1$
						"KostentrÃ¤ger:K"); //$NON-NLS-1$
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/MV/gesetz", //$NON-NLS-1$
						"MVG"); //$NON-NLS-1$
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/name", //$NON-NLS-1$
						PRIVATE_NAME);
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/leistungscodes", //$NON-NLS-1$
						CONST_TARMED_LEISTUNG);
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/standardausgabe", //$NON-NLS-1$
						CONST_TARMED_DRUCKER);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/privat/gesetz", //$NON-NLS-1$
						"VVG"); //$NON-NLS-1$
					
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/name", //$NON-NLS-1$
						VVG_NAME);
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/leistungscodes", //$NON-NLS-1$
						CONST_TARMED_LEISTUNG);
					CoreHub.globalCfg.set(
						Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/standardausgabe", //$NON-NLS-1$
						CONST_TARMED_DRUCKER);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/bedingungen", //$NON-NLS-1$
						KVG_REQUIREMENTS);
					CoreHub.globalCfg.set(Preferences.LEISTUNGSCODES_CFG_KEY + "/VVG/gesetz", //$NON-NLS-1$
						"VVG"); //$NON-NLS-1$
					
					PersistentObject.getConnection().exec(
						"UPDATE VK_PREISE set typ='UVG' WHERE typ='ch.elexis.data.TarmedLeistungUVG'"); //$NON-NLS-1$
					PersistentObject.getConnection().exec(
						"UPDATE VK_PREISE set typ='KVG' WHERE typ='ch.elexis.data.TarmedLeistungKVG'"); //$NON-NLS-1$
					PersistentObject.getConnection().exec(
						"UPDATE VK_PREISE set typ='IV' WHERE typ='ch.elexis.data.TarmedLeistungIV'"); //$NON-NLS-1$
					PersistentObject.getConnection().exec(
						"UPDATE VK_PREISE set typ='MV' WHERE typ='ch.elexis.data.TarmedLeistungMV'"); //$NON-NLS-1$
					update();
					break;
				}
			}
			ret = CoreHub.globalCfg.nodes(Preferences.LEISTUNGSCODES_CFG_KEY);
			if (ret == null) {
				return new String[] {
					Messages.Fall_Undefined
				};
			}
		}
		return ret;
	}
	
	/**
	 * This is an update only for swiss installations that takes the old tarmed cases to the new
	 * system
	 * 
	 * @since 3.6 moved from {@link Fall}
	 */
	private static void update(){
		Query<Fall> qbe = new Query<Fall>(Fall.class);
		for (Fall fall : qbe.execute()) {
			if (fall.getInfoString(Fall.FLD_EXT_KOSTENTRAEGER).equals(StringConstants.EMPTY)) {
				fall.setInfoString(Fall.FLD_EXT_KOSTENTRAEGER, PersistentObject.checkNull(fall //$NON-NLS-1$
					.get(Fall.FLD_KOSTENTRAEGER)));
			}
			if (fall.getInfoString(Fall.FLD_EXT_RECHNUNGSEMPFAENGER)
				.equals(StringConstants.EMPTY)) {
				fall.setInfoString(Fall.FLD_EXT_RECHNUNGSEMPFAENGER, PersistentObject.checkNull(fall //$NON-NLS-1$
					.get(Fall.FLD_GARANT_ID)));
			}
			if (fall.getInfoString("Versicherungsnummer").equals(StringConstants.EMPTY)) { //$NON-NLS-1$ //$NON-NLS-2$
				fall.setInfoString("Versicherungsnummer", PersistentObject.checkNull(fall //$NON-NLS-1$
					.get(Fall.FLD_VERS_NUMMER)));
			}
			if (fall.getInfoString("Fallnummer").equals(StringConstants.EMPTY)) { //$NON-NLS-1$ //$NON-NLS-2$
				fall.setInfoString("Fallnummer", PersistentObject.checkNull(fall //$NON-NLS-1$
					.get(Fall.FLD_FALL_NUMMER)));
			}
			if (fall.getInfoString("Unfallnummer").equals(StringConstants.EMPTY)) { //$NON-NLS-1$ //$NON-NLS-2$
				fall.setInfoString("Unfallnummer", PersistentObject.checkNull(fall //$NON-NLS-1$
					.get(Fall.FLD_FALL_NUMMER)));
			}
		}
	}
	
	/**
	 * 
	 * @param systemname
	 * @param codesystem
	 * @param ausgabe
	 * @param requirements
	 * @since 3.6 moved from {@link Fall}
	 */
	public static void createAbrechnungssystem(final String systemname, final String codesystem,
		final String ausgabe, final String... requirements){
		String key = Preferences.LEISTUNGSCODES_CFG_KEY + "/" + systemname; //$NON-NLS-1$
		CoreHub.globalCfg.set(key + "/name", systemname); //$NON-NLS-1$
		CoreHub.globalCfg.set(key + "/leistungscodes", codesystem); //$NON-NLS-1$
		CoreHub.globalCfg.set(key + "/standardausgabe", ausgabe); //$NON-NLS-1$
		CoreHub.globalCfg.set(key + "/bedingungen", StringTool.join(requirements, //$NON-NLS-1$
			";")); //$NON-NLS-1$
	}
	
	/**
	 * 
	 * @param systemName
	 * @since 3.6 moved from {@link Fall}
	 */
	public static void removeAbrechnungssystem(final String systemName){
		CoreHub.globalCfg.remove(Preferences.LEISTUNGSCODES_CFG_KEY + "/" + systemName); //$NON-NLS-1$
		CoreHub.globalCfg.flush();
	}
	
	/**
	 * 
	 * @param billingSystem
	 * @return
	 * @since 3.6 moved from {@link Fall}
	 */
	public static String getCodeSystem(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/leistungscodes", null); //$NON-NLS-1$
		if (ret == null) { // compatibility
			BillingSystem.getAbrechnungsSysteme();
			ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
				+ billingSystem + "/leistungscodes", "?"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ret;
	}
	
	/**
	 * 
	 * @param billingSystem
	 * @return
	 * @since 3.6 moved from {@link Fall}
	 */
	public static String getDefaultPrintSystem(final String billingSystem){
		String ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
			+ billingSystem + "/standardausgabe", null); //$NON-NLS-1$
		if (ret == null) { // compatibility
			BillingSystem.getAbrechnungsSysteme();
			ret = CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_CFG_KEY + "/" //$NON-NLS-1$
				+ billingSystem + "/standardausgabe", "?"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ret;
	}
	
	/**
	 * 
	 * @param abrechnungsSystem
	 * @return
	 * @since 3.6 moved from {@link Fall}
	 */
	public static String getRequirementsBySystem(String abrechnungsSystem){
		String req = BillingSystem.getRequirements(abrechnungsSystem);
		return req == null ? "" : req;
	}
	
	/**
	 * Move the Kostentraeger value from ExtInfo to the db row (#6105). This is required only once
	 * on transition to 3.6
	 * 
	 * @param billingSystem
	 * @param fieldName
	 * @since 3.6
	 */
	public static void moveCostBearerFromExtinfoToDBRow(String billingSystem, String fieldName){
		Query<Fall> qre = new Query<Fall>(Fall.class, Fall.FLD_BILLINGSYSTEM, billingSystem);
		qre.clear(true);
		List<Fall> qbe = qre.execute();
		for (Fall fall : qbe) {
			String contactId = (String) fall.getExtInfoStoredObjectByKey(fieldName);
			if (contactId != null) {
				log.info("Moving Fall [{}] ExtInfo key [{}] value [{}] to db table", fall.getId(),
					fieldName, contactId);
				Kontakt costBearer = Kontakt.load(contactId);
				fall.setCostBearer(costBearer);
				fall.setExtInfoStoredObjectByKey(fieldName, null);
			}
		}
	}
	
	/**
	 * Remove a value stored in ExtInfo for all Faelle of a BillingSystem.
	 * 
	 * @param billingSystem
	 * @param fieldName
	 * @since 3.6
	 */
	public static void removeExtInfoValueForAllFaelleOfBillingSystem(String billingSystem,
		String fieldName){
		Query<Fall> qre = new Query<Fall>(Fall.class, Fall.FLD_BILLINGSYSTEM, billingSystem);
		qre.clear(true);
		List<Fall> qbe = qre.execute();
		for (Fall fall : qbe) {
			log.info("Removing Fall [{}] ExtInfo key [{}]", fall.getId(), fieldName);
			fall.setExtInfoStoredObjectByKey(fieldName, null);
		}
	}
}
