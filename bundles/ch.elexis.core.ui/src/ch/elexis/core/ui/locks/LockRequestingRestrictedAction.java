package ch.elexis.core.ui.locks;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.ui.actions.RestrictedAction;

public abstract class LockRequestingRestrictedAction<T> extends RestrictedAction {

	private T object;

	public LockRequestingRestrictedAction(EvaluatableACE necessaryRight, String text) {
		super(necessaryRight, text);
	}

	@Override
	public void doRun() {
		if (!AccessControlServiceHolder.get().evaluate(evaluatableAce)) {
			return;
		}

		object = getTargetedObject();
		if (object == null) {
			return;
		}

		LockResponse lr = LocalLockServiceHolder.get().acquireLock(object);
		if (lr.isOk()) {
			doRun(object);
			LocalLockServiceHolder.get().releaseLock(object);
		} else {
			LockResponseHelper.showInfo(lr, object, log);
		}
	};

	public abstract T getTargetedObject();

	public abstract void doRun(T element);

}
