package ch.elexis.core.model;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;

public enum MaritalStatus implements INumericEnum, ILocalizedEnum {
	UNKNOWN(0, "UNK"), ANNULLED(1, "A"), DIVORCED(2, "D"), INTERLOCUTORY(3, "I"), LEGALLY_SEPARATED(4, "L"),
	MARRIED(5, "M"), POLYGAMOUS(6, "P"), NEVER_MARRIED(7, "S"), DOMESTIC_PARTNER(8, "T"), UNMARRIED(9, "U"),
	WIDOWED(10, "W");

	private final int numeric;
	private final String fhirCode;

	private MaritalStatus(int numeric, String fhirCode) {
		this.numeric = numeric;
		this.fhirCode = fhirCode;
	}

	public int numericValue() {
		return numeric;
	}

	/**
	 * @return the FHIR code
	 * @see https://www.hl7.org/fhir/valueset-marital-status.html
	 */
	public String getFhirCode() {
		return fhirCode;
	}

	@Override
	public String getLocaleText() {
		try {
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(MaritalStatus.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

	public static MaritalStatus byNumericSafe(String maritalStatus) {
		for (MaritalStatus prio : MaritalStatus.values()) {
			if (Integer.toString(prio.numericValue()).equalsIgnoreCase(maritalStatus)) {
				return prio;
			}
		}
		return MaritalStatus.UNKNOWN;
	}

	public static MaritalStatus byFhirCodeSafe(String code) {
		for (MaritalStatus element : MaritalStatus.values()) {
			if (element.getFhirCode().equalsIgnoreCase(code)) {
				return element;
			}
		}
		return MaritalStatus.UNKNOWN;
	}

}
