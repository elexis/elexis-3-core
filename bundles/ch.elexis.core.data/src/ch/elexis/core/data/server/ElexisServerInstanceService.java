package ch.elexis.core.data.server;

import javax.ws.rs.core.Response;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.common.InstanceStatus;
import ch.elexis.core.server.IInstanceService;

public class ElexisServerInstanceService implements IInstanceService {
	
	private final IInstanceService iis;
	
	public ElexisServerInstanceService(String restUrl){
		iis = ConsumerFactory.createConsumer(restUrl, new ElexisServerClientConfig(),
			IInstanceService.class);
	}
	
	@Override
	public Response updateStatus(InstanceStatus request){
		return iis.updateStatus(request);
	}
	
	@Override
	public Response getStatus(){
		return iis.getStatus();
	}
	
}
