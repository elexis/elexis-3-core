package ch.elexis.core.ui.commands;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.util.LocalLock;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;

public class AbortLocalDocumentHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		
		IEclipseContext iEclipseContext =
			PlatformUI.getWorkbench().getService(IEclipseContext.class);
		StructuredSelection selection = (StructuredSelection) iEclipseContext
			.get(event.getCommand().getId().concat(".selection"));
		iEclipseContext.remove(event.getCommand().getId().concat(".selection"));
		if (selection != null && !selection.isEmpty()) {
			List<?> selected = selection.toList();
			Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			for (Object object : selected) {
				LocalDocumentServiceHolder.getService().ifPresent(service -> {
					if (service.contains(object)) {
						Optional<LocalLock> lock = LocalLock.getManagedLock(object);
						lock.ifPresent(localDocumentLock -> localDocumentLock.unlock());
						
						service.remove(object);
					} else {
						MessageDialog.openInformation(parentShell,
							Messages.AbortLocalDocumentHandler_infotitle,
							Messages.AbortLocalDocumentHandler_infomessage);
					}
				});
			}
		}
		return null;
	}
}
