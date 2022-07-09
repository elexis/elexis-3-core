package ch.elexis.core.ui.locks;

import ch.elexis.data.Fall;

public class ToggleCurrentCaseLockHandler extends AbstractToggleCurrentLockHandler {

	public static final String COMMAND_ID = "ch.elexis.core.ui.command.ToggleCurrentCaseLockCommand"; //$NON-NLS-1$

	@Override
	public String getCommandId() {
		return COMMAND_ID;
	}

	@Override
	public Class<?> getTemplateClass() {
		return Fall.class;
	}

}
