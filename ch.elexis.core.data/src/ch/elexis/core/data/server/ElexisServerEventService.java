package ch.elexis.core.data.server;

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
	
	private IEventService eventService;
	
	public void reconfigure(){
		final String restUrl =
			System.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL);
		if (restUrl != null && restUrl.length() > 0) {
			log.info("Operating against elexis-server instance on " + restUrl);
			eventService = ConsumerFactory.createConsumer(restUrl, IEventService.class);
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
}
