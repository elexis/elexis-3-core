package ch.elexis.core.jpa.model.adapter.event;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;

import ch.rgw.tools.StringTool;
@Component(service = {
	EventHandler.class, EntityChangeEventListener.class
}, property = EventConstants.EVENT_TOPIC + StringTool.equals + ElexisEventTopics.PERSISTENCE_EVENT_ENTITYCHANGED)
public class EntityChangeEventListener implements EventHandler {
	
	private static Logger logger = LoggerFactory.getLogger(EntityChangeEventListener.class);
	
	private WeakHashMap<EntityWithId, List<WeakReference<AbstractIdModelAdapter<?>>>> listenerMap;
	
	public EntityChangeEventListener(){
		listenerMap =
			new WeakHashMap<EntityWithId, List<WeakReference<AbstractIdModelAdapter<?>>>>();
	}
	
	public void add(AbstractIdModelAdapter<?> adapter){
		synchronized (listenerMap) {
			EntityWithId entity = adapter.getEntity();
			List<WeakReference<AbstractIdModelAdapter<?>>> listeners = getListenersFor(entity);
			
			listeners.add(new WeakReference<AbstractIdModelAdapter<?>>(adapter));
			listenerMap.put(entity, listeners);
		}
	}
	
	private List<WeakReference<AbstractIdModelAdapter<?>>> getListenersFor(EntityWithId entity){
		List<WeakReference<AbstractIdModelAdapter<?>>> listeners = listenerMap.get(entity);
		if (listeners == null) {
			listeners = new ArrayList<WeakReference<AbstractIdModelAdapter<?>>>();
		}
		return listeners;
	}
	
	@Override
	public void handleEvent(Event event){
		EntityWithId entity = (EntityWithId) event.getProperty(EntityWithId.class.getName());
		synchronized (listenerMap) {
			List<WeakReference<AbstractIdModelAdapter<?>>> listeners = getListenersFor(entity);
			
			Iterator<WeakReference<AbstractIdModelAdapter<?>>> iter = listeners.iterator();
			while (iter.hasNext()) {
				WeakReference<AbstractIdModelAdapter<?>> reference = iter.next();
				if (reference != null) {
					AbstractIdModelAdapter<?> adapter = reference.get();
					if (adapter != null) {
						adapter.setEntity(entity, false);
					} else {
						iter.remove();
					}
				}
			}
		}
	}
}
