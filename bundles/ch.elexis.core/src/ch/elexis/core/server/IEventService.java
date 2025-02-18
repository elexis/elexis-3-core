package ch.elexis.core.server;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import ch.elexis.core.common.ElexisEvent;

@Path("/elexis/eventservice")
public interface IEventService {

	@POST
	@Path("/postEvent")
	@Consumes(MediaType.APPLICATION_XML)
	public Response postEvent(ElexisEvent elexisEvent);

}
