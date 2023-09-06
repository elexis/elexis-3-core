package ch.elexis.core.ui.locks;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.data.PersistentObject;

public abstract class AllOrNoneLockRequestingRestrictedAction<T extends PersistentObject> extends RestrictedAction {

	private List<T> objects;

	public AllOrNoneLockRequestingRestrictedAction(EvaluatableACE evaluatableAce, String text) {
		super(evaluatableAce, text);
	}

	public AllOrNoneLockRequestingRestrictedAction(EvaluatableACE evaluatableAce, String text, int val) {
		super(evaluatableAce, text, val);
	}

	@Override
	public void doRun() {
		if (!AccessControlServiceHolder.get().evaluate(evaluatableAce)) {
			return;
		}

		objects = getTargetedObjects();
		if (objects == null || objects.size() == 0) {
			return;
		}

		List<LockInfo> acquiredLocks = new ArrayList<>();

		for (T object : objects) {
			LockResponse lr = LocalLockServiceHolder.get().acquireLock(object);
			if (lr.isOk()) {
				acquiredLocks.add(lr.getLockInfo());
			} else {
				LockResponseHelper.showInfo(lr, object, log);
				releaseAllAcquiredLocks(acquiredLocks);
				return;
			}
		}

		doRun(objects);

		releaseAllAcquiredLocks(acquiredLocks);
	};

	private void releaseAllAcquiredLocks(List<LockInfo> acquiredLocks) {
		for (LockInfo lockInfo : acquiredLocks) {
			LockResponse lockResponse = LocalLockServiceHolder.get().releaseLock(lockInfo);
			if (!lockResponse.isOk()) {
				log.warn("Could not release lock for [{}] with lock response [{}]", //$NON-NLS-1$
						lockInfo.getElementType() + "::" + lockInfo.getElementId(), lockResponse.getStatus()); //$NON-NLS-1$
			}
		}
	}

	public abstract List<T> getTargetedObjects();

	public abstract void doRun(List<T> lockedElements);

}
