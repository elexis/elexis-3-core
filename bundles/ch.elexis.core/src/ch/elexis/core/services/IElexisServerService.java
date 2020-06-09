package ch.elexis.core.services;

import java.util.UUID;

import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;

public interface IElexisServerService {
	
	/**
	 * Reconfigure the connection to the server
	 * 
	 * @return the elexis-server URL this service is operating against, or <code>null</code>
	 */
	public String reconfigure();
	
	/**
	 * A unique id for this instance of Elexis. Changes on every restart.
	 * 
	 * @return
	 */
	public UUID getSystemUuid();
	
	/**
	 * Whether this station operates in standalone mode, that is - it is not connected to an
	 * elexis-server.
	 * 
	 * @return
	 */
	public boolean isStandalone();
	
	/**
	 * EventService: Post an event to the server
	 * 
	 * @param elexisEvent
	 * @return
	 */
	public IStatus postEvent(ElexisEvent elexisEvent);
	
	/**
	 * EventService: Are remote events delivered
	 * 
	 * @return if this service currently delivers events to the server
	 */
	public boolean deliversRemoteEvents();
	
	/**
	 * Create a fresh, populated {@link InstanceStatus} object
	 * @return
	 */
	public InstanceStatus createInstanceStatus();
	
	/**
	 * InstanceService: Update the status of this elexis instance to the server
	 * 
	 * @param request
	 * @return
	 */
	public Response updateInstanceStatus(InstanceStatus request);
	
	/**
	 * InstanceService: Get the status of this instance from the server
	 * 
	 * @return
	 */
	public Response getInstanceStatus();
	
	/**
	 * LockService:
	 * 
	 * @param request
	 * @return
	 */
	public LockResponse acquireOrReleaseLocks(LockRequest request);
	
	/**
	 * LockService:
	 * 
	 * @param request
	 * @return
	 */
	public boolean isLocked(LockRequest request);
	
	/**
	 * LockService:
	 * 
	 * @param storeToString
	 * @return
	 */
	public LockInfo getLockInfo(String storeToString);
}
