package ch.elexis.core.fhir;

import ch.elexis.core.constants.XidConstants;

public class FhirChConstants {

	/**
	 * Prefix to represent OID as system
	 */
	public static final String PREFIX_URN_OID = "urn:oid:";

	/**
	 * @see https://oidref.com/2.51.1.3
	 */
	public static final String OID_GLN = "2.51.1.3";

	/**
	 * The full system String
	 */
	public static final String OID_GLN_SYSTEM = PREFIX_URN_OID + OID_GLN;

	/**
	 * OID for Swiss AHV Number
	 * 
	 * @see XidConstants#CH_AHV
	 * @see http://fhir.ch/ig/ch-core/NamingSystem-ahvn13-navs13.json.html
	 */
	public static final String OID_AHV13 = "2.16.756.5.32";

	/**
	 * The full system String
	 */
	public static final String OID_AHV13_SYSTEM = PREFIX_URN_OID + OID_AHV13;

	/**
	 * OID for Swiss Versichertennummer
	 * 
	 * @see http://fhir.ch/ig/ch-core/StructureDefinition-ch-core-coverage.html
	 */
	public static final String OID_VERSICHERTENNUMMER = "2.16.756.5.30.1.123.100.1.1.1";

	/**
	 * The full system String
	 */
	public static final String OID_VERSICHERTENNUMMER_SYSTEM = PREFIX_URN_OID + OID_VERSICHERTENNUMMER;

	/**
	 * @see https://www.bag.admin.ch/bag/de/home/versicherungen/krankenversicherung/krankenversicherung-versicherer-aufsicht/verzeichnisse-krankenundrueckversicherer.html
	 */
	public static final String BSV_NUMMER_SYSTEM = "https://www.bag.admin.ch/bag/bsv-nummer";

}
