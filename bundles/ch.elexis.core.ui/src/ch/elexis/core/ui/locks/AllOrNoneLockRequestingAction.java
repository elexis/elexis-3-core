package ch.elexis.core.ui.locks;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.data.PersistentObject;

/**
 * The lock is acquired before calling doRun. If the lock can not be acquired doRun is not called,
 * and a message is displayed. Action will always be active.
 * 
 * @author thomas
 *
 * @param <T>
 */
public abstract class AllOrNoneLockRequestingAction<T extends PersistentObject> extends Action {

	private Logger log = LoggerFactory.getLogger(AllOrNoneLockRequestingAction.class);
	
	private List<T> objects;

	public AllOrNoneLockRequestingAction(String text) {
		super(text);
		setEnabled(true);
	}

	public void run(){
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
	
	private void releaseAllAcquiredLocks(List<LockInfo> acquiredLocks){
		for (LockInfo lockInfo : acquiredLocks) {
			LockResponse lockResponse = LocalLockServiceHolder.get().releaseLock(lockInfo);
			if (!lockResponse.isOk()) {
				log.warn("Could not release lock for [{}] with lock response [{}]",
					lockInfo.getElementType() + "::" + lockInfo.getElementId(),
					lockResponse.getStatus());
			}
		}
	}
	
	/**
	 * 
	 * @return the object the lock is requested for, or <code>null</code> to return without action
	 */
	public abstract List<T> getTargetedObjects();

	public abstract void doRun(List<T> lockedElements);

}
