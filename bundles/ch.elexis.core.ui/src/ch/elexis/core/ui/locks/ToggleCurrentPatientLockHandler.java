package ch.elexis.core.ui.locks;

import ch.elexis.data.Patient;

public class ToggleCurrentPatientLockHandler extends AbstractToggleCurrentLockHandler {

	public static final String COMMAND_ID = "ch.elexis.core.ui.command.ToggleCurrentPatientLockCommand"; //$NON-NLS-1$

	@Override
	public String getCommandId() {
		return COMMAND_ID;
	}

	@Override
	public Class<?> getTemplateClass() {
		return Patient.class;
	}
}
