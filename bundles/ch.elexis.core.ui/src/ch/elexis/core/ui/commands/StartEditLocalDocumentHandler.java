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
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.data.util.BriefExternUtil;
import ch.elexis.core.data.util.LocalLock;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IConflictHandler;
import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.services.ILocalLockService.Status;
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
				object = getAsPersistentObject(object);
				// direct extern open if Brief on file system
				if (object instanceof Brief && BriefExternUtil.isExternFile()) {
					Optional<File> file = BriefExternUtil.getExternFile((Brief) object);
					if (file.isPresent()) {
						Program.launch(file.get().getAbsolutePath());
					} else {
						MessageDialog.openError(parentShell,
							Messages.StartEditLocalDocumentHandler_errortitle,
							Messages.StartEditLocalDocumentHandler_errormessage);
					}
				} else {
					Optional<ILocalDocumentService> localDocumentService =
						LocalDocumentServiceHolder.getService();
					if (localDocumentService.isPresent()) {
						ILocalDocumentService service = localDocumentService.get();
						if (LocalLockServiceHolder.get().getStatus() == Status.REMOTE) {
							if (object instanceof IPersistentObject) {
								IPersistentObject lockObject = (IPersistentObject) object;
								AcquireLockUi.aquireAndRun(lockObject, new ILockHandler() {
									@Override
									public void lockFailed(){
										// no action required ...
									}
									
									@Override
									public void lockAcquired(){
										startEditLocal(lockObject, service, parentShell);
									}
								});
							} else if (object instanceof Identifiable) {
								Identifiable lockObject = (Identifiable) object;
								AcquireLockUi.aquireAndRun(lockObject, new ILockHandler() {
									@Override
									public void lockFailed(){
										// no action required ...
									}
									
									@Override
									public void lockAcquired(){
										startEditLocal(lockObject, service, parentShell);
									}
								});
							}
						} else {
							LocalLock lock = new LocalLock(object);
							if (!lock.tryLock()) {
								if ((service.contains(object)
									&& lock.hasLock(CoreHub.getLoggedInContact().getLabel()))
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
										return null;
									}
								} else {
									return null;
								}
							}
							startEditLocal(object, service, parentShell);
						}
					}
				}
			}
		}
		return null;
	}
	
	private Object getAsPersistentObject(Object object){
		if (object instanceof IDocumentLetter) {
			return NoPoUtil.loadAsPersistentObject((IDocumentLetter) object);
		}
		return object;
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
