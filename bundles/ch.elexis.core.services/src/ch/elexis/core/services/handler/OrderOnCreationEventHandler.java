package ch.elexis.core.services.handler;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IOrder;
import ch.elexis.core.services.OrderHistoryService;

/**
 * Event handler that listens to the creation of {@link IOrder} entities and
 * triggers the history logging.
 */
@Component(property = { EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.PERSISTENCE_EVENT_COMPATIBILITY_CREATE,
		EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.EVENT_CREATE })
public class OrderOnCreationEventHandler implements EventHandler {

	/**
	 * Handles incoming creation events and logs the creation if the payload is an
	 * IOrder.
	 *
	 * @param event the event payload containing the newly created object
	 */
	@Override
	public void handleEvent(Event event) {
		Object object = event.getProperty(ElexisEventTopics.ECLIPSE_E4_DATA);

		if (object instanceof IOrder order) {
			new OrderHistoryService().logCreateOrder(order);
		}
	}
}