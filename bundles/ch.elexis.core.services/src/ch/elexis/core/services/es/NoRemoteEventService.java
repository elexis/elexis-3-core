package ch.elexis.core.services.es;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.server.IEventService;
import jakarta.ws.rs.core.Response;

public class NoRemoteEventService implements IEventService {
	private final Response OK = Response.ok().build();

	@Override
	public Response postEvent(ElexisEvent elexisEvent) {
		return OK;
	}
}
