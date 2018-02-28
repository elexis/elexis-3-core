package ch.elexis.core.data.server;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.server.ILockService;

public class ElexisServerLockService implements ILockService {
	
	private final ILockService ils;
	
	public ElexisServerLockService(String restUrl){
		ils = ConsumerFactory.createConsumer(restUrl, new ElexisServerClientConfig(),
			ILockService.class);
	}
	
	@Override
	public LockResponse acquireOrReleaseLocks(LockRequest request){
		return ils.acquireOrReleaseLocks(request);
	}
	
	@Override
	public boolean isLocked(LockRequest request){
		return ils.isLocked(request);
	}
	
	@Override
	public LockInfo getLockInfo(String storeToString){
		return ils.getLockInfo(storeToString);
	}
	
}
