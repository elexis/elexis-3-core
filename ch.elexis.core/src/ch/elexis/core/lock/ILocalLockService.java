package ch.elexis.core.lock;

import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.server.ILockService;

/**
 * Additional convinience methods for locking with PersistentObject.
 * 
 * @author thomas
 *
 */
public interface ILocalLockService extends ILockService {

	public LockResponse acquireLock(IPersistentObject po);
	
	public LockResponse releaseLock(IPersistentObject po);
	
	public boolean isLocked(IPersistentObject po);
	
	public LockResponse releaseAllLocks();
}
