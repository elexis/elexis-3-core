package info.elexis.server.elexis.common.jaxrs;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import info.elexis.server.elexis.common.types.LockInfo;
import info.elexis.server.elexis.common.types.LockRequest;

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
	public boolean acquireOrReleaseLocks(LockRequest request);

	@GET
	@Path("/isLocked")
	@Consumes(MediaType.APPLICATION_XML)
	public boolean isLocked(@QueryParam("objectId") String storeToString);

	@GET
	@Path("/lockInfo")
	@Consumes(MediaType.APPLICATION_XML)
	public LockInfo getLockInfo(@QueryParam("objectId") String storeToString);
}