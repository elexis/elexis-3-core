package ch.elexis.core.ui.locks;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.PersistentObject;
import info.elexis.server.elexis.common.types.LockResponse;

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
		
		LockResponse lr = CoreHub.ls.acquireLock(object.storeToString());
		if(lr.isOk()) {
			doRun(object);
			CoreHub.ls.releaseLock(object.storeToString());
		} else {
			log.warn("Unable to acquire lock for "+object.storeToString());
			// TODO show message
			// we could not get the lock, what now??
			// simple ui warning showing who currently owns the lock?
			SWTHelper.showError("Lock acquisition error.", "Can't acquire lock for " + object.storeToString()
			+ ". Lock currently held by " + lr.getLockInfos().getUser());
		}
	};

	public abstract T getTargetedObject();

	public abstract void doRun(T element);

}
