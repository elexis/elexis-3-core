package ch.elexis.core.model.issue;

import java.util.ResourceBundle;

import org.slf4j.LoggerFactory;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;

public enum ProcessStatus implements INumericEnum, ILocalizedEnum {
	OPEN(0),
	/**
	 * @deprecated please derive via reminder due date
	 */
	DUE(1),
	/**
	 * @deprecated please derive via reminder due date
	 */
	OVERDUE(2), CLOSED(3), ON_HOLD(4), IN_PROGRESS(5);

	private final int numeric;

	private ProcessStatus(int numeric) {
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
					.getString(ProcessStatus.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}

	public static ProcessStatus byNumericSafe(String statusIn) {
		try {
			int numeric = Integer.parseInt(statusIn);
			for (ProcessStatus status : ProcessStatus.values()) {
				if (status.numericValue() == numeric) {
					return status;
				}
			}
		} catch (NumberFormatException e) {
			// ignore return default
			LoggerFactory.getLogger(ProcessStatus.class).warn("Status [" + statusIn + "] is not a number", e);
		}
		return ProcessStatus.OPEN;
	}

}
