package ch.elexis.core.findings.util.fhir.transformer.helper;

import java.util.HashMap;
import java.util.Map;

import ch.elexis.core.constants.XidConstants;
import ch.elexis.core.fhir.FhirChConstants;

/**
 * Maps between FHIR Identifier system and internal XID domain. If an entry does
 * not exist, or return <code>null</code> communication in the respective
 * direction should not happen.
 */
public class IdentifierDomainMapper {

	private static Map<String, String> MAP_ELEXIS_TO_FHIR;
	private static Map<String, String> MAP_FHIR_TO_ELEXIS;

	static {
		MAP_ELEXIS_TO_FHIR = new HashMap<>();
		MAP_FHIR_TO_ELEXIS = new HashMap<>();

		// -- outwards

		MAP_ELEXIS_TO_FHIR.put(XidConstants.EAN, FhirChConstants.OID_GLN_SYSTEM);
		MAP_ELEXIS_TO_FHIR.put(XidConstants.CH_AHV, FhirChConstants.OID_AHV13_SYSTEM);
		MAP_ELEXIS_TO_FHIR.put(XidConstants.DOMAIN_BSVNUM, FhirChConstants.BSV_NUMMER_SYSTEM);
		MAP_ELEXIS_TO_FHIR.put(XidConstants.DOMAIN_KSK, FhirChConstants.OID_ZSR_SYSTEM);
		MAP_ELEXIS_TO_FHIR.put(XidConstants.DOMAIN_NIF, XidConstants.DOMAIN_NIF);
		MAP_ELEXIS_TO_FHIR.put(XidConstants.DOMAIN_SUVA, XidConstants.DOMAIN_SUVA);
		MAP_ELEXIS_TO_FHIR.put(XidConstants.DOMAIN_RECIPIENT_EAN, FhirChConstants.BSV_NUMMER_SYSTEM);

		// -- inwards

		MAP_FHIR_TO_ELEXIS.put(XidConstants.EAN, XidConstants.EAN);
		MAP_FHIR_TO_ELEXIS.put(FhirChConstants.OID_GLN_SYSTEM, XidConstants.EAN);

		MAP_FHIR_TO_ELEXIS.put(XidConstants.CH_AHV, XidConstants.CH_AHV);
		MAP_FHIR_TO_ELEXIS.put(FhirChConstants.OID_AHV13_SYSTEM, XidConstants.CH_AHV);

		MAP_FHIR_TO_ELEXIS.put(FhirChConstants.BSV_NUMMER_SYSTEM, XidConstants.DOMAIN_BSVNUM);
		MAP_FHIR_TO_ELEXIS.put(XidConstants.DOMAIN_BSVNUM, XidConstants.DOMAIN_BSVNUM);
		MAP_FHIR_TO_ELEXIS.put(XidConstants.DOMAIN_RECIPIENT_EAN, XidConstants.DOMAIN_RECIPIENT_EAN);

		MAP_FHIR_TO_ELEXIS.put(FhirChConstants.OID_ZSR_SYSTEM, XidConstants.DOMAIN_KSK);
		MAP_FHIR_TO_ELEXIS.put(XidConstants.DOMAIN_KSK, XidConstants.DOMAIN_KSK);

		MAP_FHIR_TO_ELEXIS.put(XidConstants.DOMAIN_NIF, XidConstants.DOMAIN_NIF);

		MAP_FHIR_TO_ELEXIS.put(XidConstants.DOMAIN_SUVA, XidConstants.DOMAIN_SUVA);
	}

	public static String ELEXIS_TO_FHIR(String domain) {
		return MAP_ELEXIS_TO_FHIR.get(domain);
	}

	public static String FHIR_TO_ELEXIS(String domain) {
		return MAP_FHIR_TO_ELEXIS.get(domain);
	}

}
