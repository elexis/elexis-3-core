package ch.elexis.core.ui.locks;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.data.PersistentObject;

/**
 * Lock multiple elements. By default operation is handled with {@link Strategy#SEQUENTIAL}. The
 * action itself is done in either {@link #doRun(PersistentObject)} or {@link #doRun(List)}
 * depending on strategy.
 *
 * @param <T>
 * @since 3.6
 * @since 3.7 implements {@link Strategy#ALL_OR_NOTHING}, add
 *        {@link #acquireLockFailed(LockResponse, IPersistentObject)}
 */
public abstract class MultiLockRequestingAction<T extends List<? extends PersistentObject>>
		extends Action {
	
	private List<? extends PersistentObject> objects;
	
	public enum Strategy {
			/**
			 * Try each element in the list. If one fails, show info, continue with next. This is
			 * the default strategy.
			 */
			SEQUENTIAL,
			/**
			 * Lock all elements, calls {@link MultiLockRequestingAction#doRun(List)} if successful.
			 */
			ALL_OR_NOTHING
	};
	
	private final Strategy strategy;
	
	public MultiLockRequestingAction(String text){
		this(text, SWT.NONE, Strategy.SEQUENTIAL);
	}
	
	public MultiLockRequestingAction(String text, int style, Strategy strategy){
		super(text, style);
		setEnabled(true);
		this.strategy = strategy;
	}
	
	public void run(){
		objects = getTargetedObjects();
		if (objects == null || objects.size() == 0) {
			return;
		}
		
		if (Strategy.SEQUENTIAL == strategy) {
			performSequentialStrategy();
		} else if (Strategy.ALL_OR_NOTHING == strategy) {
			performAllOrNothingStrategy();
		}
	};
	
	private void performAllOrNothingStrategy(){
		boolean allLocksAquired = true;
		List<LockResponse> acquiredLocks = new ArrayList<>();
		LockResponse failedLockResponse = null;
		PersistentObject failedPersistentObject = null;
		
		// try to acquire all
		for (PersistentObject persistentObject : objects) {
			LockResponse lockResponse = LocalLockServiceHolder.get().acquireLock(persistentObject);
			if (lockResponse.isOk()) {
				acquiredLocks.add(lockResponse);
			} else {
				allLocksAquired = false;
				failedLockResponse = lockResponse;
				failedPersistentObject = persistentObject;
				break;
			}
		}
		
		if (allLocksAquired) {
			doRun(objects);
		} else {
			acquireLockFailed(failedLockResponse, failedPersistentObject);
		}
		
		for (LockResponse lockResponse : acquiredLocks) {
			LockResponse releaseLockResponse = LocalLockServiceHolder.get().releaseLock(lockResponse.getLockInfo());
			if(!releaseLockResponse.isOk()) {
				LockResponseHelper.showInfo(releaseLockResponse, null, LoggerFactory.getLogger(getClass()));
			}
		}
		
	}
	
	private void performSequentialStrategy(){
		for (PersistentObject persistentObject : objects) {
			LockResponse lr = LocalLockServiceHolder.get().acquireLock(persistentObject);
			if (lr.isOk()) {
				doRun(persistentObject);
			} else {
				acquireLockFailed(lr, persistentObject);
			}
		}
		
	}
	
	/**
	 * Override if required.
	 * 
	 * @param failedLockResponse
	 * @param failedPersistentObject
	 */
	public void acquireLockFailed(LockResponse failedLockResponse,
		PersistentObject failedPersistentObject){
		
		LockResponseHelper.showInfo(failedLockResponse, failedPersistentObject, null);
	}
	
	/**
	 * 
	 * @return the objects the lock is requested for, or <code>null</code> to return without action
	 */
	public abstract List<? extends PersistentObject> getTargetedObjects();
	
	/**
	 * The locked object to perform the action on. Called for each object on
	 * {@link Strategy#SEQUENTIAL}
	 * 
	 * @param collection
	 */
	public void doRun(PersistentObject lockedObject){
		// override to implement
	}
	
	/**
	 * The locked objects to perform the action on. Called once on {@link Strategy#ALL_OR_NOTHING}
	 * 
	 * @param lockedObjects
	 */
	public void doRun(List<? extends PersistentObject> lockedObjects){
		// override to implement
	}
	
}
