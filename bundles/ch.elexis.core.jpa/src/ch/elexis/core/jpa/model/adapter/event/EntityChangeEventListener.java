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

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;

@Component(service = { EventHandler.class, EntityChangeEventListener.class }, property = EventConstants.EVENT_TOPIC
		+ "=" + ElexisEventTopics.PERSISTENCE_EVENT_ENTITYCHANGED)
public class EntityChangeEventListener implements EventHandler {

	private WeakHashMap<EntityWithId, List<WeakReference<AbstractIdModelAdapter<?>>>> listenerMap;

	public EntityChangeEventListener() {
		listenerMap = new WeakHashMap<>();
	}

	private int addCount;

	public void add(AbstractIdModelAdapter<?> adapter) {
		synchronized (listenerMap) {
			EntityWithId entity = adapter.getEntity();
			List<WeakReference<AbstractIdModelAdapter<?>>> listeners = getListenersFor(entity);

			listeners.add(new WeakReference<AbstractIdModelAdapter<?>>(adapter));
			listenerMap.put(entity, listeners);

			if (addCount++ > 25000) {
				if (listenerMap.size() > 25000) {
					cleanup();
				}
				addCount = 0;
			}
		}
	}

	private void cleanup() {
		Iterator<EntityWithId> entitiesIter = listenerMap.keySet().iterator();
		while (entitiesIter.hasNext()) {
			EntityWithId entity = entitiesIter.next();
			List<WeakReference<AbstractIdModelAdapter<?>>> listeners = listenerMap.get(entity);
			if (listeners != null) {
				Iterator<WeakReference<AbstractIdModelAdapter<?>>> iter = listeners.iterator();
				while (iter.hasNext()) {
					WeakReference<AbstractIdModelAdapter<?>> reference = iter.next();
					if (reference != null) {
						AbstractIdModelAdapter<?> adapter = reference.get();
						if (adapter == null) {
							iter.remove();
						}
					}
				}
				if (listeners.isEmpty()) {
					entitiesIter.remove();
				}
			}
		}
	}

	private List<WeakReference<AbstractIdModelAdapter<?>>> getListenersFor(EntityWithId entity) {
		List<WeakReference<AbstractIdModelAdapter<?>>> listeners = listenerMap.get(entity);
		if (listeners == null) {
			listeners = new ArrayList<>();
		}
		return listeners;
	}

	@Override
	public void handleEvent(Event event) {
		EntityWithId entity = (EntityWithId) event.getProperty(EntityWithId.class.getName());
		synchronized (listenerMap) {
			List<WeakReference<AbstractIdModelAdapter<?>>> listeners = getListenersFor(entity);
			// reset map with new entity object, old object could be no longer referenced
			listenerMap.put(entity, listeners);

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
