
package ch.elexis.core.ui.e4.handlers;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jfr.JavaFlightRecorderService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.OsgiServiceUtil;

public class JavaFlightRecorderDumpFile {

	@Execute
	public void execute(JavaFlightRecorderService flightRecordingService, Display display) {
		if (!flightRecordingService.isRecording()) {
			return;
		}

		IConfigService configService = OsgiServiceUtil.getService(IConfigService.class).orElse(null);
		String stationIdentifier = (configService != null)
				? configService.getLocal(Preferences.STATION_IDENT_ID, "idunk")
				: "idunk";

		FileDialog fileDialog = new FileDialog(display.getActiveShell(), SWT.SAVE);
		fileDialog.setFilterPath(CoreUtil.getWritableUserDir().toString());
		fileDialog.setFileName("elexis_" + stationIdentifier + "_" + System.currentTimeMillis() + ".jfr");
		String filePath = fileDialog.open();
		if (filePath != null) {
			Path path = new File(filePath).toPath();
			try {
				flightRecordingService.dumpFile(path);
				MessageDialog.openInformation(display.getActiveShell(), "Success", "Dump stored in " + path);
			} catch (IOException e) {
				MessageDialog.openWarning(display.getActiveShell(), "Error saving file " + path, e.getMessage());
			}
		}
	}

	@CanExecute
	public boolean canExecute(JavaFlightRecorderService flightRecordingService) {
		return flightRecordingService.isRecording();
	}

}