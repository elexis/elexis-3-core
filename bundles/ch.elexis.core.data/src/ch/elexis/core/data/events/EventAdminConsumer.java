package ch.elexis.core.data.events;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.User;

@Component
public class EventAdminConsumer {
	
	protected Logger log = LoggerFactory.getLogger(EventAdminConsumer.class.getName());
	
	private static EventAdmin eventAdmin;
	
	@Reference(service = EventAdmin.class, cardinality = ReferenceCardinality.MANDATORY, policy = ReferencePolicy.STATIC, unbind = "unsetEventAdmin")
	protected synchronized void setEventAdmin(EventAdmin ea){
		EventAdminConsumer.eventAdmin = ea;
	}
	
	protected synchronized void unsetEventAdmin(EventAdmin ea){
		EventAdminConsumer.eventAdmin = new EventAdmin() {
			
			@Override
			public void sendEvent(Event event){
				log.info("Catching sendEvent [{}]", event);
			}
			
			@Override
			public void postEvent(Event event){
				log.info("Catching postEvent [{}]", event);
			}
		};
	}
	
	public static EventAdmin getEventAdmin(){
		return eventAdmin;
	}
	
	public static void sendElexisEvent(ElexisEvent ee){
		Event event = mapElexisEvent(ee);
		eventAdmin.sendEvent(event);
	}
	
	public static void postElexisEvent(ElexisEvent ee){
		Event event = mapElexisEvent(ee);
		eventAdmin.postEvent(event);
	}
	
	private static Event mapElexisEvent(ElexisEvent ee){
		
		String topic = "";
		Map<String, Object> properties = new HashMap<String, Object>();
		
		boolean addObjectContent = false;
		
		switch (ee.type) {
		case ElexisEvent.EVENT_CREATE:
			topic = ElexisEventTopics.PERSISTENCE_EVENT_CREATE;
			break;
		case ElexisEvent.EVENT_SELECTED:
			topic = ElexisEventTopics.CONTEXT_EVENT_SELECTION;
			topic += "/" + ee.getObjectClass().getSimpleName().toLowerCase();
			addObjectContent = true;
			break;
		case ElexisEvent.EVENT_DESELECTED:
			topic = ElexisEventTopics.CONTEXT_EVENT_SELECTION;
			topic += "/" + ee.getObjectClass().getSimpleName().toLowerCase();
			break;
		default:
			topic = ElexisEventTopics.CONTEXT_EVENT + "eventid/" + Integer.toString(ee.type);
			break;
		}
		
		IPersistentObject object = ee.getObject();
		if (object != null) {
			properties.put(ElexisEventTopics.PROPKEY_CLASS, object.getClass());
			if(addObjectContent) {
				properties.put(ElexisEventTopics.PROPKEY_ID, object.getId());
				properties.put(ElexisEventTopics.PROPKEY_OBJECT, object);
			}
		}
		
		IPersistentObject user = ElexisEventDispatcher.getSelected(User.class);
		if (user != null) {
			properties.put(ElexisEventTopics.PROPKEY_USER, user.getId());
		}
		
		return new Event(topic, properties);
	}
}
