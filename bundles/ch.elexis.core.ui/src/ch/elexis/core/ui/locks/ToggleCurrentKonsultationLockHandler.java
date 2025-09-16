package ch.elexis.core.ui.locks;

import ch.elexis.core.model.IEncounter;

public class ToggleCurrentKonsultationLockHandler extends AbstractToggleCurrentLockHandler {

	public static final String COMMAND_ID = "ch.elexis.core.ui.command.ToggleCurrentKonsultationLockCommand"; //$NON-NLS-1$

	@Override
	public String getCommandId() {
		return COMMAND_ID;
	}

	@Override
	public Class<?> getTemplateClass() {
		return IEncounter.class;
	}
}
