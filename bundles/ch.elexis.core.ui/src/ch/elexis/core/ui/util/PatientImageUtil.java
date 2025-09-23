package ch.elexis.core.ui.util;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.MimeType;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

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