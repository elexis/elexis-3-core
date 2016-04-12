package ch.elexis.core.ui.locks;

import ch.elexis.data.Konsultation;

public class ToggleCurrentKonsultationLockHandler extends AbstractToggleCurrentLockHandler {

	public static final String COMMAND_ID =
		"ch.elexis.core.ui.command.ToggleCurrentKonsultationLockCommand";

	@Override
	public String getCommandId(){
		return COMMAND_ID;
	}
	
	@Override
	public Class<?> getTemplateClass(){
		return Konsultation.class;
	}
}
