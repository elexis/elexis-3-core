package ch.elexis.core.findings.util.commands;

import ch.elexis.core.lock.types.LockResponse;

/**
 * Provide an interface for differen locking implementations. Needed because Elexis RCP and Elexis
 * server have different implementations.
 * 
 * @author thomas
 *
 */
public interface ILockingProvider {
	
	public LockResponse acquireLock(Object object);
	
	public LockResponse releaseLock(Object object);
}
