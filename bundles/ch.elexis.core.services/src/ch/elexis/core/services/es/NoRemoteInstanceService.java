package ch.elexis.core.services.es;

import javax.ws.rs.core.Response;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.server.IInstanceService;

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
