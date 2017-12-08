package ch.elexis.core.model.issue;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;

public enum Visibility implements INumericEnum, ILocalizedEnum {
	ON_PATIENT_SELECTION(0), 
	ALWAYS(1), 
	POPUP_ON_PATIENT_SELECTION(2), 
	POPUP_ON_LOGIN(3);

	private final int numeric;

	private Visibility(int numeric) {
		this.numeric = numeric;
	}

	public int numericValue() {
		return numeric;
	}

	public String getLocaleText() {
		try {
			return ResourceBundle.getBundle("ch.elexis.core.model.issue.messages")
					.getString(Visibility.class.getSimpleName() + "." + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

	public static Visibility byNumericSafe(String visibilityIn) {
		for (Visibility visibility : Visibility.values()) {
			if (Integer.toString(visibility.numericValue()).equalsIgnoreCase(visibilityIn)) {
				return visibility;
			}
		}
		return Visibility.ALWAYS;
	}
}
