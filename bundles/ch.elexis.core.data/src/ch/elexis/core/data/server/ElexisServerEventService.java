package ch.elexis.core.data.server;

import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.server.IEventService;

public class ElexisServerEventService {
	
	private Logger log = LoggerFactory.getLogger(ElexisServerEventService.class);
	
	private IEventService eventService = new NoRemoteEventService();
	
	public void reconfigure(){
		final String restUrl =
			System.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
		if (restUrl != null && restUrl.length() > 0) {
			log.info("Operating against elexis-server instance on " + restUrl);
			eventService = ConsumerFactory.createConsumer(restUrl, new ElexisServerClientConfig(),
				IEventService.class);
		} else {
			eventService = new NoRemoteEventService();
		}
	}
	
	public IStatus postEvent(ElexisEvent elexisEvent){
		if (eventService != null) {
			try {
				eventService.postEvent(elexisEvent);
				return Status.OK_STATUS;
			} catch (Exception e) {
				return new Status(Status.ERROR, CoreHub.PLUGIN_ID, e.getMessage(), e);
			}
		}
		return new Status(Status.ERROR, CoreHub.PLUGIN_ID, "No EventService available");
	}
	
	/**
	 * @return <code>true</code> if connected to an Elexis-Server, else <code>false</code>
	 */
	public boolean deliversRemoteEvents(){
		return !(eventService instanceof NoRemoteEventService);
	}
	
	private class NoRemoteEventService implements IEventService {
		
		private final Response OK = Response.ok().build();
		
		@Override
		public Response postEvent(ElexisEvent elexisEvent){
			return OK;
		}
		
	}
}
