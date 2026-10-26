package ch.elexis.core.ui.contacts.views.util;

import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.MimeType;
import ch.elexis.core.ui.contacts.dialogs.PatientCameraCaptureDialog;
import ch.elexis.core.ui.util.PatientImageUtil;
import ch.elexis.data.Patient;

/**
 * Utility class for handling patient image loading, saving, conversion, and
 * scaling in SWT and AWT environments.
 */
public class CameraCaptureUtil {

	private static Logger logger = LoggerFactory.getLogger(CameraCaptureUtil.class);

	/**
	 * Opens the camera dialog for capturing a new patient photo and saves the
	 * photo.
	 *
	 * @param patientId   the patient ID
	 * @param patientName the patient name (for metadata)
	 * @param shell       the parent shell
	 */
	public static boolean openCameraAndSavePhoto(Patient patient, String patientName, Shell shell) {
		byte[] imageBytes = new PatientCameraCaptureDialog().openAndCaptureImage(shell);
		if (imageBytes == null || imageBytes.length == 0) {
			logger.warn("No photo captured or photo data is empty for patientId {}", patient);
			return false;
		}
		PatientImageUtil.savePatientImage(patient.getId(), imageBytes, patientName, MimeType.png);
		return true;
	}
}