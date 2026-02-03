package ch.elexis.core.server;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/elexis/lockservice")
public interface ILockService {
	/**
	 * All or none
	 *
	 * @param objectIds
	 * @param userId
	 * @return
	 */
	@POST
	@Path("/acquireOrReleaseLocks")
	@Consumes(MediaType.APPLICATION_XML)
	public LockResponse acquireOrReleaseLocks(LockRequest request);

	@POST
	@Path("/isLocked")
	@Consumes(MediaType.APPLICATION_XML)
	public boolean isLocked(LockRequest request);

	@GET
	@Path("/lockInfo")
	@Consumes(MediaType.APPLICATION_XML)
	public LockInfo getLockInfo(@QueryParam("objectId") String storeToString);
}
