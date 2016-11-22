package ch.elexis.core.findings.codes;

public enum CodingSystem {
	ELEXIS_COVERAGE_TYPE("www.elexis.info/coverage/type"), ELEXIS_DIAGNOSE_TESSINERCODE(
			"www.elexis.info/diagnose/tessinercode"), ELEXIS_PRACTITIONER_ROLE(
					"www.elexis.info/practitioner/role"), ELEXIS_LOCAL_CODESYSTEM(
							"www.elexis.info/coding/local"), ELEXIS_ENCOUNTER_TYPE(
									"www.elexis.info/encounter/type"), ELEXIS_ICD_CODESYSTEM(
											"www.elexis.info/coding/icd"), ELEXIS_ICPC_CODESYSTEM(
													"www.elexis.info/coding/icpc"), ELEXIS_TARMED_CODESYSTEM(
															"www.elexis.info/billing/tarmed");

	private String system;
	
	private CodingSystem(String system) {
		this.system = system;
	}

	public String getSystem() {
		return system;
	}
}
