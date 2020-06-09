package ch.elexis.core.services.es;

import javax.ws.rs.core.Response;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.server.IEventService;

public class NoRemoteEventService implements IEventService {
	private final Response OK = Response.ok().build();
	
	@Override
	public Response postEvent(ElexisEvent elexisEvent){
		return OK;
	}
}
