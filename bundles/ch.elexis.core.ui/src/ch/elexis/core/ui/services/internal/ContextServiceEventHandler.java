package ch.elexis.core.ui.services.internal;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IContextService;

/**
 * {@link EventHandler} for application startup, setting the
 * {@link MApplication} to use with the action {@link IContextService}.
 */
@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class ContextServiceEventHandler implements EventHandler {

	private static Logger logger = LoggerFactory.getLogger(ContextServiceEventHandler.class);

	@Reference
	private IContextService contextService;
	
	@Override
	public void handleEvent(Event event) {
		Object property = event.getProperty("org.eclipse.e4.data"); //$NON-NLS-1$
		if (property instanceof MApplication) {
			logger.info("APPLICATION STARTUP COMPLETE " + property); //$NON-NLS-1$
			MApplication application = (MApplication) property;
			if (contextService instanceof ContextService) {
				((ContextService) contextService).setApplication(application);
			}
		}
	}
}
