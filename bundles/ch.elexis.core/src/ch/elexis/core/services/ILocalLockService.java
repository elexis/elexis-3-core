package ch.elexis.core.services;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.server.ILockService;

/**
 * Additional convenience methods for locking with PersistentObject.
 *
 */
public interface ILocalLockService extends ILockService {

	public LockResponse acquireLock(Object object);

	/**
	 * Release a lock via a StoreToString
	 * 
	 * @param storeToString
	 * @return
	 * @since 3.10
	 */
	public LockResponse releaseLock(String storeToString);

	public LockResponse releaseLock(Object object);

	public LockResponse releaseLock(LockInfo lockInfo);

	public boolean isLocked(Object object);
	
	/**
	 * A variant of {@link #isLocked(Object)} where the StoreToString is used for
	 * comparison
	 * 
	 * @param storeToString
	 * @return
	 * @since 3.10
	 */
	public boolean isLocked(String storeToString);

	/**
	 * Use this only for performance optimization, it is not guaranteed that the
	 * server has the same locks as the client
	 *
	 * @param po
	 * @return
	 */
	public boolean isLockedLocal(Object po);

	public LockResponse releaseAllLocks();

	public List<LockInfo> getCopyOfAllHeldLocks();

	public String getSystemUuid();

	public LockResponse acquireLockBlocking(Object po, int msTimeout, IProgressMonitor monitor);

	public void shutdown();
}
