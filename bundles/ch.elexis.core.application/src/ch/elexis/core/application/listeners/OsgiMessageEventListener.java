package ch.elexis.core.application.listeners;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.ui.UiDesk;

@Component(property = EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.BASE_NOTIFICATION + "*")
public class OsgiMessageEventListener implements EventHandler {
	
	@Override
	public void handleEvent(Event event){
		final String topic =
			event.getTopic().substring(ElexisEventTopics.BASE_NOTIFICATION.length());
		final String title =
			(String) event.getProperty(ElexisEventTopics.NOTIFICATION_PROPKEY_TITLE);
		final String message =
			(String) event.getProperty(ElexisEventTopics.NOTIFICATION_PROPKEY_MESSAGE);
		
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run(){
				switch (topic) {
				case "error":
					MessageDialog.openError(UiDesk.getTopShell(), title, message);
					break;
				case "warn":
					MessageDialog.openWarning(UiDesk.getTopShell(), title, message);
					break;
				case "info":
					MessageDialog.openInformation(UiDesk.getTopShell(), title, message);
					break;
				default:
					LoggerFactory.getLogger(getClass()).error(
						"invalid topic [" + topic + "] for message: " + title + "/" + message);
					break;
				}
			}
		});
		
	}
	
}
