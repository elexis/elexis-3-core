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

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.LocalLock;
import ch.elexis.core.lock.ILocalLockService.Status;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.services.IConflictHandler;
import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
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
					if(CoreHub.getLocalLockService().getStatus() == Status.REMOTE) {
						AcquireLockUi.aquireAndRun((IPersistentObject) object, new ILockHandler() {
							@Override
							public void lockFailed(){
								// no action required ...
							}
							
							@Override
							public void lockAcquired(){
								startEditLocal(object, service, parentShell);
							}
						});						
					} else {
						LocalLock lock = new LocalLock(object);
						if (!lock.tryLock()) {
							if (MessageDialog.openQuestion(parentShell, Messages.StartEditLocalDocumentHandler_warning,
								Messages.StartEditLocalDocumentHandler_alreadyOpenStart + lock.getLockMessage()
									+ Messages.StartEditLocalDocumentHandler_alreadyOpenEnd)) {
								lock.unlock();
								if (!lock.tryLock()) {
									MessageDialog.openError(parentShell,
										Messages.StartEditLocalDocumentHandler_errortitle,
										Messages.StartEditLocalDocumentHandler_errormessage);
									return;
								}
							} else {
								return;
							}
						}
						startEditLocal(object, service, parentShell);
					}
				});
			}
		}
		return null;
	}
	
	private void startEditLocal(Object object, ILocalDocumentService service, Shell parentShell){
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
			MessageDialog.openError(parentShell, Messages.StartEditLocalDocumentHandler_errortitle,
				Messages.StartEditLocalDocumentHandler_errormessage);
		}
	}
}
