package ch.elexis.core.test.service;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.services.ILocalLockService;

@Component
public class TestLocalLockService implements ILocalLockService {

	@Activate
	public void activate() {
		Logger logger = LoggerFactory.getLogger(getClass());
		logger.error("Initializing test lock service");
	}

	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request) {
		return LockResponse.OK;
	}

	@Override
	public boolean isLocked(LockRequest request) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LockInfo getLockInfo(String storeToString) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockResponse acquireLock(Object object) {
		return LockResponse.OK;
	}

	@Override
	public LockResponse releaseLock(Object object) {
		return LockResponse.OK;
	}

	@Override
	public LockResponse releaseLock(LockInfo lockInfo) {
		return LockResponse.OK;
	}

	@Override
	public boolean isLocked(Object object) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLockedLocal(Object po) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public LockResponse releaseAllLocks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<LockInfo> getCopyOfAllHeldLocks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getSystemUuid() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public LockResponse acquireLockBlocking(Object po, int msTimeout, IProgressMonitor monitor) {
		return LockResponse.OK;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public LockResponse releaseLock(String storeToString) {
		return LockResponse.OK;
	}

	@Override
	public boolean isLocked(String storeToString) {
		// TODO Auto-generated method stub
		return false;
	}

}
