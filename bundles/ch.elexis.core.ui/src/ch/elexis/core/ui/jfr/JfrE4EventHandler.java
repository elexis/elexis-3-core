package ch.elexis.core.ui.jfr;

import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

@Component(property = { EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE,
		EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED }, immediate = true)
public class JfrE4EventHandler implements EventHandler {

	private ThreadLocal<E4Event> _event;

	@Override
	public void handleEvent(Event event) {
		_event = E4Event.EVENT;
		if (_event.get().isEnabled()) {
			_event.get().topic = event.getTopic();
			_event.get().commit();
		}
	}

}
