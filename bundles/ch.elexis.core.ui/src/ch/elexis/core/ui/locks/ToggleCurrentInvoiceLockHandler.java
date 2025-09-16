package ch.elexis.core.ui.locks;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ch.elexis.core.model.IInvoice;

public class ToggleCurrentInvoiceLockHandler extends AbstractToggleCurrentLockHandler {

	public static final String COMMAND_ID = "ch.elexis.core.ui.command.ToggleCurrentInvoiceLockCommand"; //$NON-NLS-1$

	@Override
	public String getCommandId() {
		return COMMAND_ID;
	}

	@Override
	public Class<?> getTemplateClass() {
		return IInvoice.class;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		return super.execute(event);
	}
}
