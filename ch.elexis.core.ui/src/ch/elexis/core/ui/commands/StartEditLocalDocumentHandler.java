package ch.elexis.core.ui.commands;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.services.IConflictHandler;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;

public class StartEditLocalDocumentHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection && !selection.isEmpty()) {
			List<?> selected = ((StructuredSelection) selection).toList();
			Shell parentShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
			for (Object object : selected) {
				LocalDocumentServiceHolder.getService().ifPresent(service -> {
					Optional<File> file = service.add(object, new IConflictHandler() {
						@Override
						public Result getResult(){
							if (MessageDialog.openQuestion(parentShell,
								Messages.StartEditLocalDocumentHandler_conflicttitle,
								Messages.StartEditLocalDocumentHandler_conflictmessage)) {
								return Result.KEEP;
							} else {
								return Result.OVERWRITE;
							}
						}
					});
					if (file.isPresent()) {
						Program.launch(file.get().getAbsolutePath());
					} else {
						MessageDialog.openError(parentShell,
							Messages.StartEditLocalDocumentHandler_errortitle,
							Messages.StartEditLocalDocumentHandler_errormessage);
					}
				});
			}
		}
		return null;
	}
}
