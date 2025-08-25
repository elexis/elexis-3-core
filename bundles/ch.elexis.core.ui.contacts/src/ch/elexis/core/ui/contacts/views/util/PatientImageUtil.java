package ch.elexis.core.ui.contacts.views.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.MimeType;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.contacts.dialogs.PatientCameraCaptureDialog;
import ch.elexis.data.Patient;

/**
 * Utility class for handling patient image loading, saving, conversion, and
 * scaling in SWT and AWT environments.
 */
public class PatientImageUtil {

	private static Logger logger = LoggerFactory.getLogger(PatientImageUtil.class);

	/**
	 * Loads and returns the patient image as SWT {@link Image}.
	 *
	 * @param patientId the patient ID
	 * @return SWT Image, or null if no image is available or an error occurs
	 */
	public static Image getPatientImage(String patientId) {
		if (patientId == null)
			return null;
		IContact person = CoreModelServiceHolder.get().load(patientId, IContact.class).orElse(null);
		if (person == null)
			return null;
		IImage image = person.getImage();
		if (image == null)
			return null;
		byte[] imgBytes = image.getImage();
		if (imgBytes == null || imgBytes.length == 0)
			return null;

		try (ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes)) {
			ImageData imgData = new ImageData(bais);
			return new Image(Display.getDefault(), imgData);
		} catch (Exception e) {
			logger.error("Error loading patient image for patientId {}: {}", patientId, e.getMessage(), e);
			return null;
		}
	}

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
		savePatientImage(patient.getId(), imageBytes, patientName, MimeType.png);
		Display.getDefault().asyncExec(() -> {
			try {
				Image scaledImage = scaleSwtImage(
						new Image(Display.getDefault(), new ImageData(new ByteArrayInputStream(imageBytes))), 130, 130,
						photoLabel.getDisplay());
				photoLabel.setImage(scaledImage);
			} catch (Exception e) {
				logger.error("Error displaying captured photo for patientId {}: {}", patient, e.getMessage(), e);
			}
		});
	}

	/**
	 * Saves the given image bytes as a patient image entry in the database.
	 *
	 * @param patientId   the patient ID
	 * @param imageBytes  image data (PNG/JPG/etc.)
	 * @param patientName the patient name (for metadata)
	 * @param mimeType    image mime type
	 */
	public static void savePatientImage(String patientId, byte[] imageBytes, String patientName, MimeType mimeType) {
		try {
			IContact patient = CoreModelServiceHolder.get().load(patientId, IContact.class).orElse(null);
			if (patient != null) {
				IImage patientImage = CoreModelServiceHolder.get().create(IImage.class);
				patientImage.setId(patientId);
				patientImage.setImage(imageBytes);
				patientImage.setDate(LocalDate.now());
				patientImage.setPrefix("ch.elexis.data.Kontakt");
				patientImage.setTitle(patient.getDescription1() + StringUtils.SPACE + patient.getDescription2());
				patientImage.setMimeType(mimeType);
				CoreModelServiceHolder.get().save(patientImage);
			} else {
				logger.warn("No patient found for ID {} while saving patient image.", patientId);
			}
		} catch (Exception e) {
			logger.error("Error saving patient image for patientId {}: {}", patientId, e.getMessage(), e);
		}
	}

	/**
	 * Loads the patient image as SWT Image and converts it to a
	 * {@link BufferedImage}.
	 *
	 * @param patId the patient/contact ID
	 * @return BufferedImage or null if no image is available or an error occurs
	 */
	public static BufferedImage swtImageToBufferedImage(String patId) {
		if (patId == null) {
			return null;
		}

		IContact contact = CoreModelServiceHolder.get().load(patId, IContact.class).orElse(null);
		if (contact == null) {
			return null;
		}
		IImage image = contact.getImage();

		if (image == null) {
			return null;
		}
		byte[] imgBytes = image.getImage();
		if (imgBytes == null || imgBytes.length == 0) {
			return null;
		}

		Image swtImg = null;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(imgBytes)) {
			swtImg = new Image(Display.getDefault(), bais);
			ImageData data = swtImg.getImageData();
			PaletteData palette = data.palette;
			BufferedImage buf = new BufferedImage(data.width, data.height, BufferedImage.TYPE_INT_RGB);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					int rgbInt = (rgb.red << 16) | (rgb.green << 8) | rgb.blue;
					buf.setRGB(x, y, rgbInt);
				}
			}
			return buf;
		} catch (Exception ex) {
			logger.error("Error converting SWT image to BufferedImage for patientId {}: {}", patId, ex.getMessage(),
					ex);
			return null;
		} finally {
			if (swtImg != null && !swtImg.isDisposed()) {
				swtImg.dispose();
			}
		}
	}

	/**
	 * Scales an SWT {@link Image} to the specified width and height.
	 *
	 * @param original     the original image
	 * @param targetWidth  desired width
	 * @param targetHeight desired height
	 * @param display      the SWT display
	 * @return scaled image
	 */
	public static Image scaleSwtImage(Image original, int targetWidth, int targetHeight, Display display) {
		Image scaled = new Image(display, targetWidth, targetHeight);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(original, 0, 0, original.getBounds().width, original.getBounds().height, // src
				0, 0, targetWidth, targetHeight // dst
		);
		gc.dispose();
		return scaled;
	}

	public static void deletePatientImage(String patientId) {
		if (patientId == null)
			return;
		IContact contact = CoreModelServiceHolder.get().load(patientId, IContact.class).orElse(null);
		if (contact == null)
			return;
		IImage img = contact.getImage();
		if (img != null) {
			CoreModelServiceHolder.get().delete(img);
		}
	}

}