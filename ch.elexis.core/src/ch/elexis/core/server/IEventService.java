package ch.elexis.core.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import ch.elexis.core.common.ElexisEvent;

@Path("/elexis/eventservice")
public interface IEventService {

	@POST
	@Path("/postEvent")
	@Consumes(MediaType.APPLICATION_XML)
	public Response postEvent(ElexisEvent elexisEvent);
	
}
