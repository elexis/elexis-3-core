package ch.elexis.core.ui.locks;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;

/**
 * The lock is acquired before calling doRun. If the lock can not be acquired doRun is not called,
 * and a message is displayed. Action will always be active.
 * 
 * @author thomas
 *
 * @param <T>
 */
public abstract class LockRequestingAction<T> extends Action {

	private T object;

	public LockRequestingAction(String text) {
		this(text, SWT.NONE);
	}

	public LockRequestingAction(String text, int style){
		super(text, style);
		setEnabled(true);
	}

	public void run(){
		object = getTargetedObject();
		if (object == null) {
			return;
		}
		
		LockResponse lr = LocalLockServiceHolder.get().acquireLock(object);
		if(lr.isOk()) {
			doRun(object);
			LocalLockServiceHolder.get().releaseLock(object);
		} else {
			LockResponseHelper.showInfo(lr, object, null);
		}
	};
	
	/**
	 * 
	 * @return the object the lock is requested for, or <code>null</code> to return without action
	 */
	public abstract T getTargetedObject();

	public abstract void doRun(T element);

}
