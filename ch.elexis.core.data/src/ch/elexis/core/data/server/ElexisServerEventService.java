package ch.elexis.core.data.server;

import java.util.Optional;

import javax.ws.rs.core.Response;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.eclipsesource.jaxrs.consumer.ConsumerFactory;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.server.IEventService;

public class ElexisServerEventService {
	
	private Logger log = LoggerFactory.getLogger(ElexisServerEventService.class);
	
	private Optional<IEventService> eventService;
	
	public ElexisServerEventService(){
		reconfigure();
	}
	
	public void reconfigure(){
		final String restUrl =
			System.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
		if (restUrl != null && restUrl.length() > 0) {
			log.info("Operating against elexis-server instance on " + restUrl);
			eventService =
				Optional.of(ConsumerFactory.createConsumer(restUrl, IEventService.class));
		} else {
			eventService = Optional.empty();
			log.info("Operating in stand-alone mode.");
		}
	}
	
	public IStatus postEvent(ElexisEvent elexisEvent){
		if (eventService.isPresent()) {
			try {
				Response postEvent = eventService.get().postEvent(elexisEvent);
				if (postEvent.getStatusInfo() == Response.Status.OK) {
					return Status.OK_STATUS;
				}
				return new Status(Status.ERROR, CoreHub.PLUGIN_ID, postEvent.getStatus()+"");
			} catch (Exception e) {
				return new Status(Status.ERROR, CoreHub.PLUGIN_ID, e.getMessage());
			}
		}
		return new Status(Status.ERROR, CoreHub.PLUGIN_ID, "No EventService available");
	}
}
