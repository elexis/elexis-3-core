package ch.elexis.core.data.lock;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;

public class ReleaseAllLocksCommand extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean success = CoreHub.ls.releaseAllLocks();

		if (!success) {
			ElexisEventDispatcher.fireElexisStatusEvent(new ElexisStatus(Status.WARNING, CoreHub.PLUGIN_ID,
					ElexisStatus.CODE_NONE, "Locks could not be released", null));
		}

		return null;
	}

}
