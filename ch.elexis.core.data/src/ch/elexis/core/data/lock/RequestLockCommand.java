package ch.elexis.core.data.lock;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.data.Patient;
import ch.elexis.data.User;
import info.elexis.server.elexis.common.types.LockInfo;


public class RequestLockCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		if (patient == null) {
			return null;
		}

		User user = (User) ElexisEventDispatcher.getSelected(User.class);
		List<LockInfo> lockList = LockByPatientStrategy.createLockInfoList(patient, user.getId());

		boolean success = CoreHub.ls.acquireLock(lockList);

		if (!success) {
			ElexisEventDispatcher.fireElexisStatusEvent(new ElexisStatus(Status.WARNING, CoreHub.PLUGIN_ID,
					ElexisStatus.CODE_NONE, "Lock could not be granted", null));
		}

		return null;
	}

}
