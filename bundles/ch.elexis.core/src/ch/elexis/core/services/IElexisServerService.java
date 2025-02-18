package ch.elexis.core.services;

import java.util.UUID;

import jakarta.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;

public interface IElexisServerService {

	public enum ConnectionStatus {
		/** Not connected to an ES */
		STANDALONE,
		/** Connected to an ES, and connection is live */
		REMOTE,
		/** Connected to an ES, but connection is lost */
		LOCAL
	}

	/**
	 * A unique id for this instance of Elexis. Changes on every restart.
	 *
	 * @return
	 */
	public UUID getSystemUuid();

	/**
	 * Whether this station operates in standalone mode, that is - it is not
	 * connected to an elexis-server.
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
	 *
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

	/**
	 * Validate the connection to the server (applicable in non-standalone mode
	 * only). If the connection to the server fails, switches to Local Mode
	 *
	 *
	 * @return <code>true</code> if the connection to ES is live, else
	 *         <code>false</code>
	 */
	public boolean validateElexisServerConnection();

	/**
	 * @return the current connection status
	 */
	public ConnectionStatus getConnectionStatus();

	/**
	 * @return the url of the server connected to (only if
	 *         {@link ConnectionStatus#LOCAL} or {@link ConnectionStatus#REMOTE}
	 *         else <code>null</code>)
	 */
	public String getConnectionUrl();
}
