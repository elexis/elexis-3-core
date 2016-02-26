package ch.elexis.core.ui.locks;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.menus.UIElement;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Patient;
import info.elexis.server.elexis.common.types.LockResponse;

public class ToggleCurrentPatientLockHandler extends AbstractHandler implements IElementUpdater {

	public static final String COMMAND_ID = "ch.elexis.core.ui.command.ToggleCurrentPatientLockCommand";

	private ICommandService commandService;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (commandService == null) {
			commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(ICommandService.class);
		}

		IPersistentObject po = ElexisEventDispatcher.getSelected(Patient.class);
		if (po == null) {
			commandService.refreshElements(COMMAND_ID, null);
			return null;
		}

		String sts = po.storeToString();

		if (CoreHub.ls.ownsLock(sts)) {
			CoreHub.ls.releaseLock(sts);
		} else {
			LockResponse lr = CoreHub.ls.acquireLock(sts);
			if (!lr.isOk()) {
				ElexisEventDispatcher.fireElexisStatusEvent(new ElexisStatus(Status.WARNING, CoreHub.PLUGIN_ID,
						ElexisStatus.CODE_NONE, "Lock could not be granted", null));
				SWTHelper.showError("Lock acquisition error.", "Can't acquire lock for " + po.storeToString()
						+ ". Lock currently held by " + lr.getLockInfos().getUser());
			}
		}

		commandService.refreshElements(COMMAND_ID, null);

		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		IPersistentObject po = ElexisEventDispatcher.getSelected(Patient.class);
		if (po == null) {
			element.setIcon(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			element.setChecked(false);
			return;
		}

		if (CoreHub.ls.ownsLock(po.storeToString())) {
			element.setIcon(Images.IMG_LOCK_OPEN.getImageDescriptor());
			element.setChecked(true);
		} else {
			element.setIcon(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			element.setChecked(false);
		}
	}

}
