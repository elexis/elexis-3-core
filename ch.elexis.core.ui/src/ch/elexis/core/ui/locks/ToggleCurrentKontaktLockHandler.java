package ch.elexis.core.ui.locks;

import ch.elexis.data.Kontakt;

public class ToggleCurrentKontaktLockHandler extends AbstractToggleCurrentLockHandler {

	public static final String COMMAND_ID =
		"ch.elexis.core.ui.command.ToggleCurrentKontaktLockCommand";

	@Override
	public String getCommandId(){
		return COMMAND_ID;
	}

	@Override
	public Class<?> getTemplateClass(){
		return Kontakt.class;
	}
}
