package ch.elexis.core.application.listeners;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.statushandlers.StatusManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.status.ElexisStatus;
import ch.elexis.core.ui.UiDesk;

@Component(property = { EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE,
		EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.BASE_NOTIFICATION + "*" })
public class OsgiMessageEventListener implements EventHandler {

	private transient boolean displayReady = false;

	private List<Event> fifoList = Collections.synchronizedList(new LinkedList<Event>());

	@Override
	public void handleEvent(Event _event) {

		if (UIEvents.UILifeCycle.APP_STARTUP_COMPLETE.equals(_event.getTopic())) {
			displayReady = true;
			if (!fifoList.isEmpty()) {
				displayAndCleanEvents();
			}
			return;
		}

		fifoList.add(_event);
		if (!displayReady) {
			return;
		}

		displayAndCleanEvents();
	}

	private void displayAndCleanEvents() {
		Iterator<Event> iterator = fifoList.iterator();
		while (iterator.hasNext()) {
			Event event = iterator.next();
			final String topic = event.getTopic().substring(ElexisEventTopics.BASE_NOTIFICATION.length());
			final String title = (String) event.getProperty(ElexisEventTopics.NOTIFICATION_PROPKEY_TITLE);
			final String message = (String) event.getProperty(ElexisEventTopics.NOTIFICATION_PROPKEY_MESSAGE);
			final ElexisStatus status = (ElexisStatus) event.getProperty(ElexisEventTopics.NOTIFICATION_PROPKEY_STATUS);

			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					switch (topic) {
					case "error": //$NON-NLS-1$
						MessageDialog.openError(UiDesk.getTopShell(), title, message);
						break;
					case "warn": //$NON-NLS-1$
						MessageDialog.openWarning(UiDesk.getTopShell(), title, message);
						break;
					case "info": //$NON-NLS-1$
						MessageDialog.openInformation(UiDesk.getTopShell(), title, message);
						break;
					case "status": //$NON-NLS-1$
						LoggerFactory.getLogger(getClass())
								.info("StatusEvent [PLUGIN] " + status.getPlugin() + " [MESSAGE] " + status.getMessage() //$NON-NLS-1$ //$NON-NLS-2$
										+ " [EXCEPTION] " //$NON-NLS-1$
										+ status.getException());
						StatusManager.getManager().handle(status);
						break;
					default:
						LoggerFactory.getLogger(getClass())
								.error("invalid topic [" + topic + "] for message: " + title + "/" + message); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						break;
					}
				}
			});
			iterator.remove();
		}

	}

}
