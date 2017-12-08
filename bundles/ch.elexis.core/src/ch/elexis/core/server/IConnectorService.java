package ch.elexis.core.server;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.elexis.core.common.DBConnection;

@Path("/elexis/connector")
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public interface IConnectorService {

	@GET
	@Path("/connection")
	public DBConnection getElexisDBConnection();
	
	@PUT
	@Path("/connection")
	public Response setElexisDBConnection(DBConnection dbConnection);
	
	@GET
	@Path("/connection/status")
	public Response getDBInformation();
}
