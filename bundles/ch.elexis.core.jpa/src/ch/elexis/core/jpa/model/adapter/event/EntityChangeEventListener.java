package ch.elexis.core.jpa.model.adapter.event;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;

@Component(service = {
	EventHandler.class, EntityChangeEventListener.class
}, property = EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.PERSISTENCE_EVENT_ENTITYCHANGED)
public class EntityChangeEventListener implements EventHandler {
	
	private static Logger logger = LoggerFactory.getLogger(EntityChangeEventListener.class);
	
	private List<WeakReference<AbstractIdModelAdapter<?>>> listeners;
	
	public EntityChangeEventListener(){
		listeners = new ArrayList<>();
	}
	
	public void add(AbstractIdModelAdapter<?> adapter){
		listeners.add(new WeakReference<AbstractIdModelAdapter<?>>(adapter));
	}
	
	@Override
	public void handleEvent(Event event){
		EntityWithId entity = (EntityWithId) event.getProperty(EntityWithId.class.getName());
		List<WeakReference<AbstractIdModelAdapter<?>>> copyListeners = new ArrayList<>(listeners);
		
		logger.info("Refesh entity [" + entity + "] in " + copyListeners.size() + " listeners");
		copyListeners.forEach(reference -> {
			if (reference != null) {
				AbstractIdModelAdapter<?> adapter = reference.get();
				if (adapter != null) {
					if (adapter.getEntity().equals(entity)) {
						adapter.setEntity(entity);
					}
				} else {
					listeners.remove(reference);
				}
			}
		});
		logger.info("Refesh done ...");
	}
}
