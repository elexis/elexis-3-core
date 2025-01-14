package ch.elexis.core.services.es;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.server.IInstanceService;
import jakarta.ws.rs.core.Response;

public class NoRemoteInstanceService implements IInstanceService {

	private final Response OK = Response.ok().build();

	@Override
	public Response updateStatus(InstanceStatus request) {
		return OK;
	}

	@Override
	public Response getStatus() {
		return OK;
	}

}
