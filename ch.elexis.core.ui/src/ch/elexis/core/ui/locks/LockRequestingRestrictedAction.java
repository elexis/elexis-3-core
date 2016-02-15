package ch.elexis.core.ui.locks;

import ch.elexis.admin.ACE;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.data.PersistentObject;

public abstract class LockRequestingRestrictedAction<T extends PersistentObject> extends RestrictedAction {

	private T object;

	public LockRequestingRestrictedAction(ACE necessaryRight, String text) {
		super(necessaryRight, text);
	}

	public void doRun() {
		if (!CoreHub.acl.request(necessaryRight)) {
			return;
		}
		
		object = getTargetedObject();
		if (object == null) {
			return;
		}
		
		boolean lock = CoreHub.ls.acquireLock(object.storeToString());
		if(lock) {
			doRun(object);
			CoreHub.ls.releaseLock(object.storeToString());
		} else {
			log.warn("Unable to acquire lock for "+object.storeToString());
			// we could not get the lock, what now??
			// simple ui warning showing who currently owns the lock?
		}
	};

	public abstract T getTargetedObject();

	public abstract void doRun(T element);

}
