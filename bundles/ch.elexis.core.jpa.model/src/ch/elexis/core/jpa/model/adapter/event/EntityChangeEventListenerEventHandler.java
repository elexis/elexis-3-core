package ch.elexis.core.jpa.model.adapter.event;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.jpa.entities.EntityWithId;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Component(property = EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.PERSISTENCE_EVENT_ENTITYCHANGED)
public class EntityChangeEventListenerEventHandler implements EventHandler {

	@Inject
	@Reference
	EntityChangeEventListener entityChangeEventListener;

//	@ConsumeEvent(ElexisEventTopics.EVENT_CREATE) // TODO is this correct for myelexis?
	@Override
	public void handleEvent(Event event) {
		EntityWithId entity = (EntityWithId) event.getProperty(EntityWithId.class.getName());
		entityChangeEventListener.handleEvent(entity);
	}

}
