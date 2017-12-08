package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.dialogs.LocalDocumentsDialog;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;

/**
 * Open the {@link LocalDocumentsDialog}.
 * 
 * @author thomas
 *
 */
public class OpenLocalDocumentsHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		if (LocalDocumentServiceHolder.getService().isPresent()) {
			LocalDocumentsDialog dialog =
				new LocalDocumentsDialog(HandlerUtil.getActiveShell(event),
					LocalDocumentServiceHolder.getService().get());
			dialog.open();
		}
		return null;
	}
}
