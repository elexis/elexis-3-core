package ch.elexis.core.ui.contacts.views.util;

import java.io.ByteArrayInputStream;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.MimeType;
import ch.elexis.core.ui.contacts.dialogs.PatientCameraCaptureDialog;
import ch.elexis.core.ui.util.PatientImageUtilCore;
import ch.elexis.data.Patient;

/**
 * Utility class for handling patient image loading, saving, conversion, and
 * scaling in SWT and AWT environments.
 */
public class PatientImageUtil {

	private static Logger logger = LoggerFactory.getLogger(PatientImageUtil.class);

	/**
	 * Opens the camera dialog for capturing a new patient photo, saves the photo,
	 * and updates the {@link Label} with the scaled image.
	 *
	 * @param patientId   the patient ID
	 * @param patientName the patient name (for metadata)
	 * @param photoLabel  the SWT label to update with the new photo
	 * @param shell       the parent shell
	 */
	public static void openCameraAndSavePhoto(Patient patient, String patientName, Label photoLabel, Shell shell) {
		byte[] imageBytes = new PatientCameraCaptureDialog().openAndCaptureImage(shell);
		if (imageBytes == null || imageBytes.length == 0) {
			logger.warn("No photo captured or photo data is empty for patientId {}", patient);
			return;
		}
		PatientImageUtilCore.savePatientImage(patient.getId(), imageBytes, patientName, MimeType.png);
		Display.getDefault().asyncExec(() -> {
			try {
				Image scaledImage = PatientImageUtilCore.scaleSwtImage(
						new Image(Display.getDefault(), new ImageData(new ByteArrayInputStream(imageBytes))), 130, 130,
						photoLabel.getDisplay());
				photoLabel.setImage(scaledImage);
			} catch (Exception e) {
				logger.error("Error displaying captured photo for patientId {}: {}", patient, e.getMessage(), e);
			}
		});
	}
}