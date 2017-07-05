package ch.elexis.core.ui.commands;

import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.util.LocalLock;
import ch.elexis.core.services.IConflictHandler;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;

public class EndLocalDocumentHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		IEclipseContext iEclipseContext =
			PlatformUI.getWorkbench().getService(IEclipseContext.class);
		ISelection selection = (ISelection) iEclipseContext.get(event.getCommand().getId());
		if (selection instanceof StructuredSelection && !selection.isEmpty()) {
			List<?> selected = ((StructuredSelection) selection).toList();
			Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			for (Object object : selected) {
				LocalDocumentServiceHolder.getService().ifPresent(service -> {
					if (service.contains(object)) {
						Optional<LocalLock> lock = LocalLock.getManagedLock(object);
						lock.ifPresent(localDocumentLock -> localDocumentLock.unlock());
						
						if (!service.save(object)) {
							MessageDialog.openError(parentShell,
								Messages.EndLocalDocumentHandler_errorttitle,
								Messages.EndLocalDocumentHandler_errormessage);
						}
						
						service.remove(object, new IConflictHandler() {
							@Override
							public Result getResult(){
								if (MessageDialog.openQuestion(parentShell,
									Messages.EndLocalDocumentHandler_conflicttitle,
									Messages.EndLocalDocumentHandler_conflictmessage)) {
									return Result.OVERWRITE;
								} else {
									return Result.ABORT;
								}
							}
						});
					} else {
						MessageDialog.openInformation(parentShell,
							Messages.EndLocalDocumentHandler_infotitle,
							Messages.EndLocalDocumentHandler_infomessage);
					}
				});
			}
		}
		return null;
	}
	
}
