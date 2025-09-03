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
	 * Identifier holding a number for ZSR (Zahlstellenregister), RCC (Registre des
	 * codes-créanciers), RCC (Registro dei codici creditori)
	 * 
	 * OID of the ZSR/RCC
	 * 
	 * @see https://fhir.ch/ig/ch-core/StructureDefinition-ch-core-zsr-identifier.profile.json.html
	 */
	public static final String OID_ZSR = "2.16.756.5.30.1.123.100.2.1.1";

	/**
	 * The full system String
	 */
	public static final String OID_ZSR_SYSTEM = PREFIX_URN_OID + OID_ZSR;

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

	/**
	 * @see http://fhir.ch/ig/ch-epr-term/ValueSet/DocumentEntry.healthcareFacilityTypeCode
	 */
	public static final String HEALTHCARE_FACILITY_TYPE_CODE_SYSTEM = "http://fhir.ch/ig/ch-epr-term/ValueSet/DocumentEntry.healthcareFacilityTypeCode";

	/**
	 * SNOMED CT Code for a Laboratory environment
	 * 
	 * @see https://browser.ihtsdotools.org/?perspective=full&conceptId1=261904005&edition=MAIN/SNOMEDCT-CH/2022-12-07&release=&languages=en,fr,de,it
	 */
	public static final String SCTID_LABORATORY_ENVIRONMENT = "261904005";
}
