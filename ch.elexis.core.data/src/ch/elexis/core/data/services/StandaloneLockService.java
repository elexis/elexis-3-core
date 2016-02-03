package ch.elexis.core.data.services;

import java.util.Collection;
import java.util.Optional;

import info.elexis.server.elexis.common.ILockService;
import info.elexis.server.elexis.common.LockInfo;

public class StandaloneLockService implements ILockService {
	
	@Override
	public boolean acquireLocks(Collection<String> objectIds, String userId){
		return true;
	}
	
	@Override
	public boolean releaseLocks(Collection<String> objectIds, String userId){
		return true;
	}
	
	@Override
	public boolean isLocked(String objectId){
		return true;
	}
	
	@Override
	public Optional<LockInfo> getLockInfo(String objectId){
		return Optional.empty();
	}
	
}
