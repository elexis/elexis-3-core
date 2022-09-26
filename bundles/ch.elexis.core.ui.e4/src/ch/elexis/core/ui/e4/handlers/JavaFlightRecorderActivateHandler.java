
package ch.elexis.core.ui.e4.handlers;

import java.io.IOException;
import java.text.ParseException;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuItem;
import org.slf4j.LoggerFactory;

import ch.elexis.core.jfr.JavaFlightRecorderService;

public class JavaFlightRecorderActivateHandler {

	@Execute
	public void execute(JavaFlightRecorderService javaFlightRecorderService, MMenuItem item) {

		if (javaFlightRecorderService.isRecording()) {
			javaFlightRecorderService.stopRecording();
			item.setSelected(false);

		} else {
			try {
				javaFlightRecorderService.startRecording();
				item.setSelected(true);
			} catch (IOException | ParseException e) {
				item.setSelected(false);
				LoggerFactory.getLogger(getClass()).warn("Error starting flight recorder", e);
			}

		}

	}

}