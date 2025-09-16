package ch.elexis.core.ui.locks;

import ch.elexis.core.model.IContact;

public class ToggleCurrentKontaktLockHandler extends AbstractToggleCurrentLockHandler {

	public static final String COMMAND_ID = "ch.elexis.core.ui.command.ToggleCurrentKontaktLockCommand"; //$NON-NLS-1$

	@Override
	public String getCommandId() {
		return COMMAND_ID;
	}

	@Override
	public Class<?> getTemplateClass() {
		return IContact.class;
	}
}
