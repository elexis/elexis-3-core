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
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(Visibility.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

	public static Visibility byNumericSafe(String visibilityIn) {
		int numeric = Integer.parseInt(visibilityIn);
		for (Visibility visibility : Visibility.values()) {
			if (visibility.numericValue() == numeric) {
				return visibility;
			}
		}
		return Visibility.ALWAYS;
	}
}
