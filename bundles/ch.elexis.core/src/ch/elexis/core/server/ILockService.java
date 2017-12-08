package ch.elexis.core.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import ch.elexis.core.lock.types.LockInfo;
import ch.elexis.core.lock.types.LockRequest;
import ch.elexis.core.lock.types.LockResponse;

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
