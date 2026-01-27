
package ch.elexis.core.ui.e4.handlers;

import java.util.Collections;

import org.eclipse.e4.core.di.annotations.Execute;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

public class ClearHttpCacheHandler {

	@Execute
	public void execute(EventAdmin eventAdmin) {
		eventAdmin.sendEvent(new Event("info/elexis/system/clear-cache", Collections.emptyMap()));
	}

}