package ch.elexis.core.ui.startup;

import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import com.equo.chromium.ChromiumBrowser;

/**
 * Loads the Equo Chromium engine shortly after startup, so the first widget that
 * needs a browser does not have to pay for it. Without this the cost shows up
 * wherever a browser is opened first - the rich text editor of the diagnose
 * dialog, the Kompendium, ODDB, Wiki or Bookstack view - as a delay before
 * anything is rendered.
 *
 * <p>
 * {@link ChromiumBrowser#earlyInit()} only loads the native libraries and calls
 * {@code CefApp.startup}. The engine instance itself and the render process are
 * still created by the first browser widget, so this does not keep a browser
 * alive.
 */
@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class ChromiumWarmupHandler implements EventHandler {

	private static final String PROPERTY_ENABLED = "elexis.chromium.warmup"; //$NON-NLS-1$

	private static final int DELAY_MILLIS = 3000;

	private static boolean warmedUp;

	@Override
	public void handleEvent(Event event) {
		if (!Boolean.parseBoolean(System.getProperty(PROPERTY_ENABLED, Boolean.TRUE.toString()))) {
			return;
		}
		Display display = Display.getDefault();
		if (display == null || display.isDisposed()) {
			return;
		}
		display.asyncExec(() -> {
			if (!display.isDisposed()) {
				display.timerExec(DELAY_MILLIS, ChromiumWarmupHandler::warmUp);
			}
		});
	}

	private static void warmUp() {
		if (warmedUp) {
			return;
		}
		warmedUp = true;
		long start = System.currentTimeMillis();
		try {
			ChromiumBrowser.earlyInit();
			LoggerFactory.getLogger(ChromiumWarmupHandler.class).info("Chromium engine warmed up in {} ms", //$NON-NLS-1$
					System.currentTimeMillis() - start);
		} catch (Throwable t) {
			LoggerFactory.getLogger(ChromiumWarmupHandler.class).warn("Could not warm up the Chromium engine", t); //$NON-NLS-1$
		}
	}
}
