package ch.elexis.core.model.issue;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;

public enum Priority implements INumericEnum, ILocalizedEnum {
	LOW(0), MEDIUM(1), HIGH(2);

	private final int numeric;

	private Priority(int numeric) {
		this.numeric = numeric;
	}

	public int numericValue() {
		return numeric;
	}

	@Override
	public String getLocaleText() {
		try {
			return ResourceBundle.getBundle("ch.elexis.core.model.issue.messages")
					.getString(Priority.class.getSimpleName() + "." + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

	public static Priority byNumericSafe(String priority) {
		for (Priority prio : Priority.values()) {
			if (Integer.toString(prio.numericValue()).equalsIgnoreCase(priority)) {
				return prio;
			}
		}
		return Priority.MEDIUM;
	}

}
