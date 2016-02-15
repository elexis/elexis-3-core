package info.elexis.server.elexis.common.jaxrs;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/elexis/connector")
public interface IConnectorService {

	@GET
	@Path("/connection")
	public Response getElexisDBConnectionStatus();
	
	@GET
	@Path("/")
	public Response getDBInformation();
}
