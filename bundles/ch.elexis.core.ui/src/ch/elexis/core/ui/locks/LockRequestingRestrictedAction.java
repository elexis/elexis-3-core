package ch.elexis.core.ui.locks;

import ch.elexis.admin.ACE;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.ui.actions.RestrictedAction;

public abstract class LockRequestingRestrictedAction<T>
		extends RestrictedAction {
	
	private T object;
	
	public LockRequestingRestrictedAction(ACE necessaryRight, String text){
		super(necessaryRight, text);
	}
	
	public LockRequestingRestrictedAction(ACE necessaryRight, String text, int val){
		super(necessaryRight, text, val);
	}
	
	public void doRun(){
		if (!CoreHub.acl.request(necessaryRight)) {
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
