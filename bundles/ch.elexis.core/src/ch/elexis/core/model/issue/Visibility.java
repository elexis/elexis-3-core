package ch.elexis.core.model.issue;

import java.util.ResourceBundle;

import org.slf4j.LoggerFactory;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;

public enum Visibility implements INumericEnum, ILocalizedEnum {
	ON_PATIENT_SELECTION(0), ALWAYS(1), POPUP_ON_PATIENT_SELECTION(2), POPUP_ON_LOGIN(3);

	private final int numeric;

	private Visibility(int numeric) {
		this.numeric = numeric;
	}

	@Override
	public int numericValue() {
		return numeric;
	}

	@Override
	public String getLocaleText() {
		try {
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(Visibility.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

	public static Visibility byNumericSafe(String visibilityIn) {
		try {
			int numeric = Integer.parseInt(visibilityIn);
			for (Visibility visibility : Visibility.values()) {
				if (visibility.numericValue() == numeric) {
					return visibility;
				}
			}
		} catch (NumberFormatException e) {
			// ignore return default
			LoggerFactory.getLogger(ProcessStatus.class).warn("Visibility [" + visibilityIn + "] is not a number", e);

		}
		return Visibility.ALWAYS;
	}
}
