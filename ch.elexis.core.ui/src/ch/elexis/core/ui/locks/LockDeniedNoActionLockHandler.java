package ch.elexis.core.ui.locks;

public abstract class LockDeniedNoActionLockHandler implements ILockHandler {
	
	@Override
	public abstract void lockAcquired();
	
	@Override
	public void lockFailed(){
		// override if required
	}
	
}
