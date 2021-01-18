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
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.util.LocalLock;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.util.DocumentLetterUtil;
import ch.elexis.core.services.IConflictHandler;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.ILocalDocumentService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ElexisServerServiceHolder;
import ch.elexis.core.ui.UiDesk;
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
				Optional<ILocalDocumentService> localDocumentService =
					LocalDocumentServiceHolder.getService();
				if (localDocumentService.isPresent()) {
					ILocalDocumentService service = localDocumentService.get();
					if (ElexisServerServiceHolder.get()
						.getConnectionStatus() == ConnectionStatus.REMOTE) {
						if (object instanceof IPersistentObject) {
							IPersistentObject lockObject = (IPersistentObject) object;
							boolean isHandledByExternalOpen = tryHandleExternalIfApplicable(lockObject);
							if(!isHandledByExternalOpen) {
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

						} else if (object instanceof Identifiable) {
							Identifiable lockObject = (Identifiable) object;
							boolean isHandledExternalOpen = tryHandleExternalIfApplicable(lockObject);
							if(!isHandledExternalOpen) {
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
		return null;
	}
	
	/**
	 * Opens the document with an external Program if {@link Preferences#P_TEXT_EXTERN_FILE} is
	 * <code>true</code> and a valid path was set
	 * 
	 * @param lockObject
	 * @return <code>true</code> if was handled by external program, else <code>false</code>
	 */
	private boolean tryHandleExternalIfApplicable(Object lockObject){
		if (ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE, false)
			&& lockObject != null) {
			IDocumentLetter document = null;
			if (lockObject instanceof Brief) {
				document = ((Brief) lockObject).toIDocument();
			} else if (lockObject instanceof IDocumentLetter) {
				document = (IDocumentLetter) lockObject;
			} else {
				LoggerFactory.getLogger(getClass()).error("Invalid argument [{}]",
					lockObject.getClass());
				return false;
			}
			
			IVirtualFilesystemHandle handle =
				DocumentLetterUtil.getExternalHandleIfApplicable(document);
			Optional<File> file = handle.toFile();
			if (file.isPresent()) {
				Program.launch(file.get().getAbsolutePath());
			} else {
				MessageDialog.openError(UiDesk.getTopShell(),
					Messages.StartEditLocalDocumentHandler_errortitle,
					Messages.StartEditLocalDocumentHandler_errormessage);
			}
			return true;
		}
		return false;
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
