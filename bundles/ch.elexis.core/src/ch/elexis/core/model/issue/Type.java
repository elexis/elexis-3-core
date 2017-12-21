package ch.elexis.core.model.issue;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;
import ch.elexis.core.model.MaritalStatus;

public enum Type implements INumericEnum, ILocalizedEnum {

	// @formatter:off
	COMMON(0), 
	PRINT(1), 
	PRINT_DRUG_STICKER(2), 
	SEND_DOCUMENT(3), 
	READ_DOCUMENT(4), 
	CHECK_LAB_RESULT(5), 
	MAKE_APPOINTMENT(6), 
	DISPENSE_MEDICATION(7), 
	PROCESS_SERVICE_RECORDING(8);
	// @formatter:on

	private final int numeric;

	private Type(int numeric) {
		this.numeric = numeric;
	}

	public int numericValue() {
		return numeric;
	}

	public String getLocaleText() {
		try {
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(Type.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

	public static Type byNumericSafe(String actionTypeIn) {
		for (Type actionType : Type.values()) {
			if (Integer.toString(actionType.numericValue()).equalsIgnoreCase(actionTypeIn)) {
				return actionType;
			}
		}
		return Type.COMMON;
	}
}
