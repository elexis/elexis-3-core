package ch.elexis.core.ui.contacts.views.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.eclipse.swt.graphics.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for face detection using OpenCV Haar cascades. Supports
 * detection of both frontal and profile faces in AWT BufferedImages.
 */
public class FaceDetectionUtil {

	private static final Logger logger = LoggerFactory.getLogger(FaceDetectionUtil.class);

	private static CascadeClassifier faceCascade;
	private static CascadeClassifier profileCascade;

	private static final String CASCADE_RESOURCE = "/rsc/cascade/haarcascade_frontalface_default.xml";
	private static final String PROFILE_CASCADE = "/rsc/cascade/haarcascade_profileface.xml";

	/**
	 * Initializes the face and profile Haar cascades if not already loaded. Loads
	 * the classifier XML files from the resource folder.
	 */
	public static synchronized void init() {
		if (faceCascade == null) {
			faceCascade = loadCascade(CASCADE_RESOURCE);
			profileCascade = loadCascade(PROFILE_CASCADE);
		}
	}

	/**
	 * Loads an OpenCV CascadeClassifier from a resource path.
	 *
	 * @param resourcePath path to the cascade XML resource
	 * @return CascadeClassifier instance
	 * @throws RuntimeException if the resource cannot be found or loaded
	 */
	private static CascadeClassifier loadCascade(String resourcePath) {
		try (InputStream is = FaceDetectionUtil.class.getResourceAsStream(resourcePath)) {
			if (is == null) {
				logger.error("Cascade XML not found at resource path: {}", resourcePath);
				throw new RuntimeException("Cascade XML not found: " + resourcePath);
			}
			File tmp = File.createTempFile("cascade", ".xml");
			tmp.deleteOnExit();
			try (FileOutputStream fos = new FileOutputStream(tmp)) {
				byte[] buf = new byte[4096];
				int len;
				while ((len = is.read(buf)) > 0) {
					fos.write(buf, 0, len);
				}
			}
			return new CascadeClassifier(tmp.getAbsolutePath());
		} catch (IOException ex) {
			logger.error("Error loading cascade XML '{}': {}", resourcePath, ex.getMessage(), ex);
			throw new RuntimeException("Error loading cascade XML: " + resourcePath, ex);
		}
	}

	/**
	 * Detects a face in the given BufferedImage. Tries frontal, profile (original
	 * and mirrored). Returns the largest detected rectangle, or null if none found.
	 *
	 * @param img input BufferedImage (AWT)
	 * @return Rectangle containing the detected face, or null if no face detected
	 */
	public static Rectangle detectFace(BufferedImage img) {
		init();

		Mat mat;
		try {
			mat = bufferedImageToMat(img);
		} catch (Exception e) {
			logger.error("Error converting BufferedImage to Mat: {}", e.getMessage(), e);
			return null;
		}
		Mat gray = new Mat();
		opencv_imgproc.cvtColor(mat, gray, opencv_imgproc.COLOR_BGR2GRAY);
		opencv_imgproc.equalizeHist(gray, gray);

		// Frontal face detection
		RectVector fr = new RectVector();
		faceCascade.detectMultiScale(gray, fr);
		Rectangle r = pickLargest(fr);
		if (r != null) {
			return r;
		}

		// Profile face detection (original)
		RectVector pr = new RectVector();
		profileCascade.detectMultiScale(gray, pr);
		r = pickLargest(pr);
		if (r != null) {
			return r;
		}

		// Profile face detection (mirrored)
		Mat flipped = new Mat();
		opencv_core.flip(gray, flipped, +1);
		RectVector prF = new RectVector();
		profileCascade.detectMultiScale(flipped, prF);
		Rectangle best = null;
		long maxA = 0;
		for (int i = 0; i < prF.size(); i++) {
			Rect rr = prF.get(i);
			long area = (long) rr.width() * rr.height();
			if (area > maxA) {
				// Calculate mirrored X coordinate
				int x = gray.cols() - rr.x() - rr.width();
				best = new Rectangle(x, rr.y(), rr.width(), rr.height());
				maxA = area;
			}
		}
		if (best == null) {
			logger.info("No face detected in image.");
		}
		return best;
	}

	/**
	 * Selects the largest rectangle from a RectVector (helper for face selection).
	 *
	 * @param vec the vector of detected rectangles
	 * @return the largest Rectangle, or null if empty
	 */
	private static Rectangle pickLargest(RectVector vec) {
		Rect best = null;
		long maxArea = 0;
		for (int i = 0; i < vec.size(); i++) {
			Rect r = vec.get(i);
			long area = (long) r.width() * r.height();
			if (area > maxArea) {
				maxArea = area;
				best = r;
			}
		}
		if (best != null) {
			return new Rectangle(best.x(), best.y(), best.width(), best.height());
		}
		return null;
	}

	/**
	 * Converts a BufferedImage (AWT) to an OpenCV Mat object.
	 *
	 * @param bi the input BufferedImage (must be 3 channels)
	 * @return OpenCV Mat representing the image
	 */
	private static Mat bufferedImageToMat(BufferedImage bi) {
		BufferedImage converted = bi;
		if (bi.getType() != BufferedImage.TYPE_3BYTE_BGR) {
			BufferedImage tmp = new BufferedImage(bi.getWidth(), bi.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
			tmp.getGraphics().drawImage(bi, 0, 0, null);
			converted = tmp;
		}
		byte[] pixels = ((java.awt.image.DataBufferByte) converted.getRaster().getDataBuffer()).getData();
		Mat mat = new Mat(converted.getHeight(), converted.getWidth(), opencv_core.CV_8UC3);
		mat.data().put(pixels);
		return mat;
	}
}
