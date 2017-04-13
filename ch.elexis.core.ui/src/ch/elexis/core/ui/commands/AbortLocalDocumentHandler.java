package ch.elexis.core.ui.commands;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.services.LocalDocumentServiceHolder;

public class AbortLocalDocumentHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection && !selection.isEmpty()) {
			List<?> selected = ((StructuredSelection) selection).toList();
			Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			for (Object object : selected) {
				LocalDocumentServiceHolder.getService().ifPresent(service -> {
					if (service.contains(object)) {
						service.remove(object);
					} else {
						MessageDialog.openInformation(parentShell, Messages.AbortLocalDocumentHandler_infotitle,
							Messages.AbortLocalDocumentHandler_infomessage);
					}
				});
			}
		}
		return null;
	}
}
