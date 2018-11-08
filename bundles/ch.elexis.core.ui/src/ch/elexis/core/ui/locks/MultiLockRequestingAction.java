package ch.elexis.core.ui.locks;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.data.PersistentObject;

/**
 * Lock multiple elements and perform the {@link #doRun(PersistentObject)} operation on them. By
 * default operation is handled with {@link Strategy#SEQUENTIAL}
 *
 * @param <T>
 * @since 3.6
 */
public abstract class MultiLockRequestingAction<T extends List<? extends PersistentObject>>
		extends Action {
	
	private List<? extends PersistentObject> objects;
	
	public enum Strategy {
			/**
			 * Try each element in the list. If one fails, show info, continue with next.
			 */
			SEQUENTIAL,
			/**
			 * Lock all elements, only call doRun for each elements, if all the locks could be
			 * gathered. (not yet implemented)
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
			for (PersistentObject persistentObject : objects) {
				LockResponse lr = CoreHub.getLocalLockService().acquireLock(persistentObject);
				if (lr.isOk()) {
					doRun(persistentObject);
				} else {
					LockResponseHelper.showInfo(lr, persistentObject, null);
				}
			}
		} else {
			throw new UnsupportedOperationException();
		}
	};
	
	/**
	 * 
	 * @return the objects the lock is requested for, or <code>null</code> to return without action
	 */
	public abstract List<? extends PersistentObject> getTargetedObjects();
	
	/**
	 * The locked object to perform the action on.
	 * 
	 * @param collection
	 */
	public abstract void doRun(PersistentObject collection);
}
