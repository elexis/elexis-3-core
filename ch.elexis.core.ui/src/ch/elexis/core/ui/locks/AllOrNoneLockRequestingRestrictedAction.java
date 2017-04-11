package ch.elexis.core.ui.locks;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.admin.ACE;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.data.PersistentObject;

public abstract class AllOrNoneLockRequestingRestrictedAction<T extends PersistentObject>
		extends RestrictedAction {
	
	private List<T> objects;
	
	public AllOrNoneLockRequestingRestrictedAction(ACE necessaryRight, String text){
		super(necessaryRight, text);
	}
	
	public AllOrNoneLockRequestingRestrictedAction(ACE necessaryRight, String text, int val){
		super(necessaryRight, text, val);
	}
	
	public void doRun(){
		if (!CoreHub.acl.request(necessaryRight)) {
			return;
		}
		
		objects = getTargetedObjects();
		if (objects == null || objects.size() == 0) {
			return;
		}
		
		List<LockInfo> acquiredLocks = new ArrayList<>();
		
		for (T object : objects) {
			LockResponse lr = CoreHub.getLocalLockService().acquireLock(object);
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
			LockResponse lockResponse = CoreHub.getLocalLockService().releaseLock(lockInfo);
			if (!lockResponse.isOk()) {
				log.warn("Could not release lock for [{}] with lock response [{}]",
					lockInfo.getElementType() + "::" + lockInfo.getElementId(),
					lockResponse.getStatus());
			}
		}
	}
	
	public abstract List<T> getTargetedObjects();
	
	public abstract void doRun(List<T> lockedElements);
	
}
