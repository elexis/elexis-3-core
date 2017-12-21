package ch.elexis.core.model.issue;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;
import ch.elexis.core.model.MaritalStatus;

public enum ProcessStatus implements INumericEnum, ILocalizedEnum {
	OPEN(0),
	/**
	 * @deprecated please derive via reminder due date
	 */
	DUE(1), 
	/**
	 * @deprecated please derive via reminder due date
	 */
	OVERDUE(2), 
	CLOSED(3), 
	ON_HOLD(4), 
	IN_PROGRESS(5);

	private final int numeric;

	private ProcessStatus(int numeric) {
		this.numeric = numeric;
	}

	public int numericValue() {
		return numeric;
	}

	public String getLocaleText() {
		try {
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(ProcessStatus.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

	public static ProcessStatus byNumericSafe(String statusIn) {
		for (ProcessStatus status : ProcessStatus.values()) {
			if (Integer.toString(status.numericValue()).equalsIgnoreCase(statusIn)) {
				return status;
			}
		}
		return ProcessStatus.OPEN;
	}

}
