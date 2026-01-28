package ch.elexis.core.server;

import ch.elexis.core.common.InstanceStatus;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/elexis/instances")
public interface IInstanceService {

	@POST
	@Path("/updateStatus")
	@Consumes(MediaType.APPLICATION_XML)
	public Response updateStatus(InstanceStatus request);

	@GET
	@Path("/status")
	public Response getStatus();
}
