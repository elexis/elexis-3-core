package ch.elexis.core.services.es;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.server.ILockService;

/**
 * This ILockService implementation is only used during startup (while the target operation mode was
 * not yet reached).
 */
public class DenyAllLockService implements ILockService {
	
	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request){
		return LockResponse.DENIED(getLockInfo(request.getLockInfo().getElementStoreToString()));
	}
	
	@Override
	public boolean isLocked(LockRequest request){
		return false;
	}
	
	@Override
	public LockInfo getLockInfo(String storeToString){
		return new LockInfo(storeToString, "LockService", "DenyAllLockService");
	}
	
}