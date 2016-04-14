package ch.elexis.core.ui.locks;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.PersistentObject;

public abstract class LockRequestingAction<T extends PersistentObject> extends RestrictedAction {

	private T object;

	public LockRequestingAction(String text) {
		super(null, text);
		setEnabled(true);
	}

	@Override
	public void reflectRight() {
		// we always pretend to be allowed
		// as we determine lock later on
	}

	public void doRun() {
		object = getTargetedObject();
		if (object == null) {
			return;
		}
		
		LockResponse lr = CoreHub.getLocalLockService().acquireLock(object);
		if(lr.isOk()) {
			doRun(object);
			CoreHub.getLocalLockService().releaseLock(object);
		} else {
			LockResponseHelper.showInfo(lr, object, log);
		}
	};

	public abstract T getTargetedObject();

	public abstract void doRun(T element);

}
