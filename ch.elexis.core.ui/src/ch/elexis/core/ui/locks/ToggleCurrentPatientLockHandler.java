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
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.Patient;

public class ToggleCurrentPatientLockHandler extends AbstractHandler implements IElementUpdater {

	public static final String COMMAND_ID = "ch.elexis.core.ui.command.ToggleCurrentPatientLockCommand";

	private ICommandService commandService;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		if (commandService == null) {
			commandService = (ICommandService) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(ICommandService.class);
		}

		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		if (patient == null) {
			commandService.refreshElements(COMMAND_ID, null);
			return null;
		}

		String sts = patient.storeToString();

		if (CoreHub.ls.ownsLock(sts)) {
			CoreHub.ls.releaseLock(sts);
		} else {

			boolean success = CoreHub.ls.acquireLock(sts);
			if (!success) {
				ElexisEventDispatcher.fireElexisStatusEvent(new ElexisStatus(Status.WARNING, CoreHub.PLUGIN_ID,
						ElexisStatus.CODE_NONE, "Lock could not be granted", null));
			}
		}

		commandService.refreshElements(COMMAND_ID, null);

		return null;
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		if (patient == null) {
			element.setIcon(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			element.setChecked(false);
			return;
		}

		if (CoreHub.ls.ownsLock(patient.storeToString())) {
			element.setIcon(Images.IMG_LOCK_OPEN.getImageDescriptor());
			element.setChecked(true);
		} else {
			element.setIcon(Images.IMG_LOCK_CLOSED.getImageDescriptor());
			element.setChecked(false);
		}
	}

}
