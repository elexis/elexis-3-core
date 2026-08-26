package ch.elexis.core.ui.startup;

import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.widgets.Display;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.ui.views.controls.RichTextEditorComposite;

/**
 * Starts the Chromium engine shortly after startup via
 * {@link RichTextEditorComposite#warmUp()}, so the first editor the user opens takes about
 * 400 ms instead of 1600 ms. Only runs with the alternative diagnose formatting switched on,
 * since without it the diagnose dialog uses a plain text widget.
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
		if (!LocalConfigService.get(Preferences.P_TEXT_DIAGNOSE_EXPORT_WORD_FORMAT, false)) {
			return;
		}
		try {
			RichTextEditorComposite.warmUp();
		} catch (Throwable t) {
			LoggerFactory.getLogger(ChromiumWarmupHandler.class).warn("Could not warm up the rich text editor", t); //$NON-NLS-1$
		}
	}
}
