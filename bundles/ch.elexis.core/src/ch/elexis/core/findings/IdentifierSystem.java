package ch.elexis.core.findings;

public enum IdentifierSystem {
	ELEXIS_OBJID("www.elexis.info/objid"), ELEXIS_PATNR("www.elexis.info/patnr"),
	ELEXIS_CONSID("www.elexis.info/consultationid"), ELEXIS_SOAP("www.elexis.info/soap"),
	ELEXIS_ANAMNESE("www.elexis.info/anamnese"), ELEXIS_RECIPE("www.elexis.info/recipe");

	private String system;

	private IdentifierSystem(String system) {
		this.system = system;
	}

	public String getSystem() {
		return system;
	}
}
