package ch.elexis.core.ui.usage;

import org.eclipse.core.commands.Command;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class StartupHandler implements EventHandler {
	
	@Override
	public void handleEvent(Event event){
		registerNotifications();
	}
	
	public void registerNotifications(){
		
		Command.DEBUG_COMMAND_EXECUTION = true;
		IEventBroker b = PlatformUI.getWorkbench().getService(IEventBroker.class);
		b.subscribe(UIEvents.UILifeCycle.BRINGTOTOP, new EventHandler() {
			@Override
			public void handleEvent(Event event){
				
				Object part = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (part instanceof MPart) {
					MPart mPart = (MPart) part;
					if (mPart.isToBeRendered()) {
						handleEventtt("-->-ACTIVE--", event, mPart.getElementId());
					}
					
				}
			}
		});
		
		b.subscribe("org/eclipse/e4/ui/model/ui/UIElement/toBeRendered/SET", new EventHandler() {
			@Override
			public void handleEvent(Event event){
				Object placeholder = event.getProperty(UIEvents.EventTags.ELEMENT);
				if (placeholder instanceof MPlaceholder) {
					MPlaceholder mPlaceholder = (MPlaceholder) placeholder;
					if (!mPlaceholder.isToBeRendered()) {
						handleEventtt("-->-CLOSED--", event, mPlaceholder.getElementId());
					}
					
				}
			}
		});
		
		b.subscribe(UIEvents.UILifeCycle.APP_SHUTDOWN_STARTED, new EventHandler() {
			@Override
			public void handleEvent(Event event){
				handleEventtt("-->SHUTDOWN", event, event.getProperty("org.eclipse.e4.data"));
				
			}
		});
		
		// not working if perspective is in stack
		b.subscribe(UIEvents.UILifeCycle.PERSPECTIVE_OPENED, new EventHandler() {
			@Override
			public void handleEvent(Event event){
				handleEventtt("-->PERSPECTIVEOPENED", event,
					event.getProperty("org.eclipse.e4.data"));
				
			}
		});
		
	}
	
	private void handleEventtt(String prefix, Event event, Object data){
		LoggerFactory.getLogger(StartupHandler.class).debug(prefix + ":" + data);
	}
}
