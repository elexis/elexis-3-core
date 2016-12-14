package ch.elexis.core.findings.codes;

public enum CodingSystem {
	ELEXIS_COVERAGE_TYPE("www.elexis.info/coverage/type"), ELEXIS_DIAGNOSE_TESSINERCODE(
			"www.elexis.info/diagnose/tessinercode"), ELEXIS_PRACTITIONER_ROLE(
					"www.elexis.info/practitioner/role"), ELEXIS_LOCAL_CODESYSTEM(
							"www.elexis.info/coding/local"), ELEXIS_LOCAL_LABORATORY(
									"www.elexis.info/laboratory/local"), ELEXIS_LOCAL_LABORATORY_VITOLABKEY(
											"www.elexis.info/laboratory/local/vitolabkey"), ELEXIS_ENCOUNTER_TYPE(
													"www.elexis.info/encounter/type"), ICD_DE_CODESYSTEM(
															"http://hl7.org/fhir/sid/icd-10-de"), ICPC2_CODESYSTEM(
																	"http://hl7.org/fhir/sid/icpc-2"), ELEXIS_TARMED_CODESYSTEM(
																			"www.elexis.info/billing/tarmed");

	private String system;

	private CodingSystem(String system) {
		this.system = system;
	}

	public String getSystem() {
		return system;
	}
}
