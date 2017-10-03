package ch.elexis.core.ui.locks;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ch.elexis.data.Rechnung;

public class ToggleCurrentInvoiceLockHandler extends AbstractToggleCurrentLockHandler {
	
	public static final String COMMAND_ID =
		"ch.elexis.core.ui.command.ToggleCurrentInvoiceLockCommand";
	
	@Override
	public String getCommandId(){
		return COMMAND_ID;
	}
	
	@Override
	public Class<?> getTemplateClass(){
		return Rechnung.class;
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		return super.execute(event);
	}
}
