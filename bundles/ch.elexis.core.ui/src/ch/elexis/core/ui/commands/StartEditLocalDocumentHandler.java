package ch.elexis.core.ui.commands;

import java.io.File;
import java.util.List;
import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.BriefExternUtil;
import ch.elexis.core.data.util.LocalLock;
import ch.elexis.core.lock.ILocalLockService.Status;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.services.IConflictHandler;
import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;
import ch.elexis.data.Brief;

public class StartEditLocalDocumentHandler extends AbstractHandler implements IHandler {
	
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
				// direct extern open if Brief on file system
				if (object instanceof Brief && BriefExternUtil.isExternFile()) {
					Optional<File> file = BriefExternUtil.getExternFile((Brief) object);
					if (file.isPresent()) {
						Program.launch(file.get().getAbsolutePath());
					} else {
						MessageDialog.openError(parentShell, Messages.StartEditLocalDocumentHandler_errortitle,
							Messages.StartEditLocalDocumentHandler_errormessage);
					}
				} else {
					LocalDocumentServiceHolder.getService().ifPresent(service -> {
						if (CoreHub.getLocalLockService().getStatus() == Status.REMOTE) {
							AcquireLockUi.aquireAndRun((IPersistentObject) object,
								new ILockHandler() {
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
								if ((service.contains(object)
									&& lock.hasLock(CoreHub.actUser.getLabel()))
									|| MessageDialog.openQuestion(parentShell,
										Messages.StartEditLocalDocumentHandler_warning,
										Messages.StartEditLocalDocumentHandler_alreadyOpenStart
											+ lock.getLockMessage()
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
