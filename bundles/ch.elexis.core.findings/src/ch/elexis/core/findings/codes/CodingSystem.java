package ch.elexis.core.findings.codes;

import ch.elexis.core.fhir.FhirConstants;

public enum CodingSystem {
	ELEXIS_TARMED_CODESYSTEM("www.elexis.info/billing/tarmed"), 
	ELEXIS_LOCAL_CODESYSTEM("www.elexis.info/coding/local"),
	ELEXIS_COVERAGE_TYPE("www.elexis.info/coverage/type"), 
	ELEXIS_COVERAGE_REASON("www.elexis.info/coverage/reason"),
	ELEXIS_COVERAGE_UVG_ACCIDENTDATE("www.elexis.info/coverage/uvg/accidentdate"),
	ELEXIS_DIAGNOSE_TESSINERCODE("www.elexis.info/diagnose/tessinercode"),
	ELEXIS_ENCOUNTER_TYPE("www.elexis.info/encounter/type"),
	ELEXIS_PRACTITIONER_ROLE("www.elexis.info/practitioner/role"),
	ELEXIS_LOCAL_LABORATORY("www.elexis.info/laboratory/local"),
	ELEXIS_LOCAL_LABORATORY_GROUP("www.elexis.info/laboratory/local/group"),
	ELEXIS_LOCAL_LABORATORY_VITOLABKEY("www.elexis.info/laboratory/local/vitolabkey"),
	ELEXIS_DOCUMENT_CATEGORY("www.elexis.info/document/category"),
	ELEXIS_DOCUMENT_STOREID("www.elexis.info/document/storeid"),
	ELEXIS_DOCUMENT_TEMPLATE_TYP("www.elexis.info/document/template/typ"),
	/**
	 * The degree in percent of the work incapacity, see {@link FhirConstants#DE_EAU_SYSTEM}
	 */
	ELEXIS_AUF_DEGREE("www.elexis.info/auf/degree"),
	/**
	 * The reason for the work incapacity, see {@link FhirConstants#DE_EAU_SYSTEM}
	 */
	ELEXIS_AUF_REASON("www.elexis.info/auf/reason"),
	
	ICD_DE_CODESYSTEM("http://hl7.org/fhir/sid/icd-10-de"), ICPC2_CODESYSTEM("http://hl7.org/fhir/sid/icpc-2"),

	LOINC_CODESYSTEM("http://loinc.org");

	private String system;

	private CodingSystem(String system) {
		this.system = system;
	}

	public String getSystem() {
		return system;
	}
}
