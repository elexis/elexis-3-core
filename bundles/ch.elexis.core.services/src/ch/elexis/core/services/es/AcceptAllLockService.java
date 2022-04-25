package ch.elexis.core.services.es;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.server.ILockService;

/**
 * Used in standalone mode
 */
public class AcceptAllLockService implements ILockService {

	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request) {
		return LockResponse.OK(request.getLockInfo());
	}

	@Override
	public boolean isLocked(LockRequest request) {
		return true;
	}

	@Override
	public LockInfo getLockInfo(String storeToString) {
		return null;
	}

}
