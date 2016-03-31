package ch.elexis.core.lock;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.lock.types.LockInfo;
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

	public enum Status {
			LOCAL, REMOTE, STANDALONE
	}
	
	public LockResponse acquireLock(IPersistentObject po);
	
	public LockResponse releaseLock(IPersistentObject po);
	
	public boolean isLocked(IPersistentObject po);
	
	public boolean isLockedLocal(IPersistentObject po);
	
	public LockResponse releaseAllLocks();
	
	public List<LockInfo> getCopyOfAllHeldLocks();
	
	public String getSystemUuid();
	
	public LockResponse acquireLockBlocking(IPersistentObject po, int msTimeout,
		IProgressMonitor monitor);
	
	public Status getStatus();
}
