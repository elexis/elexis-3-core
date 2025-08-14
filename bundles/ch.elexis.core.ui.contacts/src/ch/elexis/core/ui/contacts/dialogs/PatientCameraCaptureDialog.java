package ch.elexis.core.ui.contacts.dialogs;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.javacv.VideoInputFrameGrabber;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.MimeType;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.ui.contacts.views.util.FaceDetectionUtil;
import ch.elexis.core.ui.contacts.views.util.ImageDataFactory;

/**
 * Dialog for capturing a patient photo from a webcam device. Displays live
 * preview, detects faces, and returns the cropped PNG image as a byte array.
 * <p>
 * Uses JavaCV for video capture and SWT for UI.
 */
public class PatientCameraCaptureDialog {

	private static final Logger logger = LoggerFactory.getLogger(PatientCameraCaptureDialog.class);

	private VideoInputFrameGrabber grabber;
	private Frame currentFrame;
	private Java2DFrameConverter converter = new Java2DFrameConverter();
	private BufferedImage capturedImage;
	private static final String CAMERA_DEFAULT_KEY = "camera.default.index";
	private static Integer cachedCameraCount = null;
	private static String[] cachedCameraNames = null;
	private static final AtomicBoolean cameraCountingInProgress = new AtomicBoolean(false);

	private volatile Rectangle liveFaceRect = null;

	/**
	 * Opens the camera capture dialog, lets the user take a photo, and returns the
	 * cropped image as PNG bytes.
	 *
	 * @param parentShell the parent SWT shell
	 * @return PNG byte array of the cropped face image, or null if canceled or no
	 *         face detected
	 */
	public byte[] openAndCaptureImage(Shell parentShell) {
		Shell shell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL | SWT.RESIZE);
		shell.setText(Messages.PatientCameraCaptureDialog_Title);
		shell.setLayout(new GridLayout(1, false));
		Composite top = new Composite(shell, SWT.NONE);
		top.setLayout(new GridLayout(3, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		Label camLabel = new Label(top, SWT.NONE);
		camLabel.setText(Messages.PatientCameraCaptureDialog_CameraLabel);
		Combo camCombo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
		camCombo.setEnabled(false);
		GridData comboGD = new GridData(SWT.FILL, SWT.CENTER, true, false);
		comboGD.widthHint = 250;
		camCombo.setLayoutData(comboGD);
		Button chkDefault = new Button(top, SWT.BUTTON3);
		chkDefault.setText(Messages.PatientCameraCaptureDialog_UseAsDefaultCamera);
		chkDefault.setEnabled(false);
		Canvas canvas = new Canvas(shell, SWT.BORDER | SWT.DOUBLE_BUFFERED);
		GridData canvasData = new GridData(SWT.FILL, SWT.FILL, true, true);
		canvasData.minimumWidth = 320;
		canvasData.minimumHeight = 240;
		canvas.setLayoutData(canvasData);
		final Image[] previewImage = new Image[1];
		canvas.addPaintListener(e -> {
			if (previewImage[0] != null) {
				Rectangle bounds = canvas.getClientArea();
				e.gc.drawImage(previewImage[0], 0, 0, previewImage[0].getBounds().width,
						previewImage[0].getBounds().height, 0, 0, bounds.width, bounds.height);
				if (liveFaceRect != null) {
					e.gc.setLineWidth(3);
					e.gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
					double scaleX = (double) bounds.width / previewImage[0].getBounds().width;
					double scaleY = (double) bounds.height / previewImage[0].getBounds().height;
					int rx = (int) (liveFaceRect.x * scaleX);
					int ry = (int) (liveFaceRect.y * scaleY);
					int rw = (int) (liveFaceRect.width * scaleX);
					int rh = (int) (liveFaceRect.height * scaleY);
					e.gc.drawRectangle(rx, ry, rw, rh);
				}
			} else {
				e.gc.drawText(Messages.PatientCameraCaptureDialog_CameraStartingText, 10, 20);
			}
		});

		Composite buttonArea = new Composite(shell, SWT.NONE);
		buttonArea.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));
		buttonArea.setLayout(new GridLayout(1, false));

		Canvas btnCapture = new Canvas(buttonArea, SWT.NONE);
		btnCapture.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
		GridData buttonData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
		buttonData.widthHint = 220;
		buttonData.heightHint = 50;
		btnCapture.setLayoutData(buttonData);

		FontData[] fD = btnCapture.getFont().getFontData();
		for (FontData fd : fD) {
			fd.setHeight(18);
		}
		Font customFont = new Font(shell.getDisplay(), fD);
		btnCapture.setFont(customFont);
		btnCapture.addPaintListener(e -> {
			e.gc.setAntialias(SWT.ON);
			org.eclipse.swt.graphics.Rectangle r = btnCapture.getClientArea();
			e.gc.setBackground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
			e.gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
			e.gc.fillRoundRectangle(r.x, r.y, r.width - 1, r.height - 1, r.height, r.height);
			e.gc.drawRoundRectangle(r.x, r.y, r.width - 1, r.height - 1, r.height, r.height);

			e.gc.setFont(customFont);
			String txt = Messages.PatientCameraCaptureDialog_CaptureButtonText;
			org.eclipse.swt.graphics.Point ts = e.gc.textExtent(txt);
			e.gc.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_WHITE));
			e.gc.drawText(txt, (r.width - ts.x) / 2, (r.height - ts.y) / 2, true);
		});
		shell.setSize(660, 610);
		shell.setLocation(parentShell.getLocation().x + 40, parentShell.getLocation().y + 40);

		final boolean[] running = { true };
		final Thread[] grabberThread = { null };
		final Display display = shell.getDisplay();

		Runnable startGrabber = () -> {
			final int cameraIndex = camCombo.getSelectionIndex();

			running[0] = false;
			if (grabberThread[0] != null && grabberThread[0].isAlive()) {
				try {
					grabberThread[0].join(400);
				} catch (InterruptedException ignored) {
				}
			}
			running[0] = true;

			grabberThread[0] = new Thread(() -> {

				try {
					System.setOut(new PrintStream(OutputStream.nullOutputStream()));
					System.setErr(new PrintStream(OutputStream.nullOutputStream()));
					grabber = new VideoInputFrameGrabber(cameraIndex);
					grabber.start();
				} catch (Exception ex) {
					logger.error("Error starting video grabber: {}", ex.getMessage(), ex);
					display.asyncExec(() -> {
						showError(shell, Messages.PatientCameraCaptureDialog_CameraErrorTitle + ex.getMessage());
						running[0] = false;
						if (!shell.isDisposed())
							shell.close();
					});
					return;
				}

				while (!shell.isDisposed() && running[0]) {
					try {
						currentFrame = grabber.grab();
						if (currentFrame != null) {
							BufferedImage bufImg = converter.getBufferedImage(currentFrame);
							if (bufImg != null) {
								bufImg = flipHorizontal(bufImg);
								Rectangle faceRect = FaceDetectionUtil.detectFace(bufImg);
								if (faceRect != null) {
									liveFaceRect = addPadding(faceRect, bufImg, 0.45);
								} else {
									liveFaceRect = null;
								}
								try {
									Image swtImg = new Image(display, ImageDataFactory.createFromAwt(bufImg));
									display.asyncExec(() -> {
										if (!canvas.isDisposed()) {
											Image old = previewImage[0];
											previewImage[0] = swtImg;
											canvas.redraw();
											if (old != null && !old.isDisposed())
												old.dispose();
										} else {
											swtImg.dispose();
										}
									});
								} catch (Exception e) {
									logger.error("Error updating live preview image: {}", e.getMessage(), e);
								}
							}
						}
						Thread.sleep(33);
					} catch (Exception ignored) {
					}
				}
			}, "CameraGrabber-" + cameraIndex);
			grabberThread[0].setDaemon(true);
			grabberThread[0].start();
		};

		shell.open();
		Runnable afterCount = () -> {
			if (shell.isDisposed() || camCombo.isDisposed())
				return;

			int cameraCount = cachedCameraCount;
			if (cameraCount == 0) {
				logger.warn("No camera found on this system.");
				showError(shell, Messages.PatientCameraCaptureDialog_NoCameraFound);
				shell.dispose();
				return;
			}
			try {
				String[] cameraNames = VideoInputFrameGrabber.getDeviceDescriptions();
				for (String name : cameraNames) {
					if (!camCombo.isDisposed())
						camCombo.add(name);
				}
			} catch (Exception e) {
				logger.error("Error reading camera device descriptions: {}", e.getMessage(), e);
				for (int i = 0; i < cameraCount; i++) {
					if (!camCombo.isDisposed())
						camCombo.add(Messages.PatientCameraCaptureDialog_DeviceName + (i + 1));
				}
			}
			if (camCombo.isDisposed())
				return;

			camCombo.select(Math.max(0,
					Math.min(loadDefaultCameraIndex() != null ? loadDefaultCameraIndex() : 0, cameraCount - 1)));
			camCombo.setEnabled(true);
			chkDefault.setEnabled(true);
			btnCapture.setEnabled(true);

			display.asyncExec(startGrabber);

			camCombo.addListener(SWT.Selection, e -> display.asyncExec(startGrabber));
		};

		if (cachedCameraCount != null) {
			display.asyncExec(afterCount);
		} else if (cameraCountingInProgress.get()) {
			new Thread(() -> {
				while (cachedCameraCount == null) {
					try {
						Thread.sleep(40);
					} catch (InterruptedException ignored) {
					}
				}
				display.asyncExec(afterCount);
			}).start();
		} else {
			cameraCountingInProgress.set(true);
			new Thread(() -> {
				int count = getCameraCount();
				cachedCameraCount = count;
				cameraCountingInProgress.set(false);
				display.asyncExec(afterCount);
			}).start();
		}

		btnCapture.addListener(SWT.MouseUp, e -> {
			if (e.button == 1) {
				BufferedImage original = converter.getBufferedImage(currentFrame);
				BufferedImage flipped = flipHorizontal(original);
				capturedImage = flipped;
				running[0] = false;
				if (chkDefault.getSelection()) {
					LocalConfigService.set(CAMERA_DEFAULT_KEY, String.valueOf(camCombo.getSelectionIndex()));
				}
				shell.close();
			}
		});
		btnCapture.addDisposeListener(e -> customFont.dispose());
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		if (customFont != null && !customFont.isDisposed()) {
			customFont.dispose();
		}
		running[0] = false;
		if (grabberThread[0] != null && grabberThread[0].isAlive()) {
			try {
				grabberThread[0].join(400);
			} catch (InterruptedException ignored) {
			}
		}
		try {
			if (grabber != null)
				grabber.stop();
		} catch (Exception ex) {
			logger.error("Error stopping video grabber: {}", ex.getMessage(), ex);
		}
		if (previewImage[0] != null)
			previewImage[0].dispose();

		if (capturedImage == null)
			return null;

		Rectangle faceRect = liveFaceRect;
		BufferedImage cropped = null;
		if (faceRect != null && faceRect.x >= 0 && faceRect.y >= 0
				&& faceRect.x + faceRect.width <= capturedImage.getWidth()
				&& faceRect.y + faceRect.height <= capturedImage.getHeight()) {
			cropped = capturedImage.getSubimage(faceRect.x, faceRect.y, faceRect.width, faceRect.height);
		} else {
			logger.warn("No face detected during photo capture.");
			showError(parentShell, Messages.PatientCameraCaptureDialog_NoFaceDetected);
			return null;
		}

		BufferedImage resized = resizeImage(cropped, 390, 390);
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			javax.imageio.ImageIO.write(resized, MimeType.png.name(), baos);
			return baos.toByteArray();
		} catch (IOException ex) {
			logger.error("Error saving patient photo to PNG: {}", ex.getMessage(), ex);
			showError(parentShell, Messages.PatientCameraCaptureDialog_SaveError + ex.getMessage());
		}

		return null;
	}

	/**
	 * Resizes a BufferedImage to the specified width and height using bilinear
	 * interpolation.
	 *
	 * @param src    the source BufferedImage
	 * @param width  target width in pixels
	 * @param height target height in pixels
	 * @return resized BufferedImage
	 */
	private static BufferedImage resizeImage(BufferedImage src, int width, int height) {
		BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		var g2 = resized.createGraphics();
		g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
				java.awt.RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(src, 0, 0, width, height, null);
		g2.dispose();
		return resized;
	}

	/**
	 * Horizontally flips a BufferedImage (mirror image).
	 *
	 * @param img the original BufferedImage
	 * @return the horizontally flipped image
	 */
	public static BufferedImage flipHorizontal(BufferedImage img) {
		int w = img.getWidth();
		int h = img.getHeight();
		BufferedImage d = new BufferedImage(w, h, img.getType());
		java.awt.Graphics2D g = d.createGraphics();
		g.drawImage(img, 0, 0, w, h, w, 0, 0, h, null);
		g.dispose();
		return d;
	}

	/**
	 * Adds padding to a detected face rectangle, staying within image bounds.
	 *
	 * @param faceRect the original detected face rectangle
	 * @param img      the source image
	 * @param percent  padding percentage (e.g., 0.45 = 45% of face size)
	 * @return expanded Rectangle with padding, clipped to the image
	 */
	public static Rectangle addPadding(Rectangle faceRect, BufferedImage img, double percent) {
		int paddingX = (int) (faceRect.width * percent);
		int paddingY = (int) (faceRect.height * percent);
		int x = Math.max(0, faceRect.x - paddingX);
		int y = Math.max(0, faceRect.y - paddingY);
		int width = Math.min(faceRect.width + 2 * paddingX, img.getWidth() - x);
		int height = Math.min(faceRect.height + 2 * paddingY, img.getHeight() - y);
		return new Rectangle(x, y, width, height);
	}

	/**
	 * Loads the saved default camera index from the local configuration.
	 *
	 * @return the default camera index, or null if not set or invalid
	 */
	private Integer loadDefaultCameraIndex() {
		String val = LocalConfigService.get(CAMERA_DEFAULT_KEY, null);
		try {
			return val != null ? Integer.parseInt(val) : null;
		} catch (NumberFormatException e) {
			logger.warn("Invalid default camera index in configuration: {}", val);
			return null;
		}
	}

	/**
	 * Probes available cameras using OpenCV. Returns the number of detected video
	 * devices (max 6).
	 *
	 * @return number of available camera devices
	 */
	private int getCameraCount() {
		int count = 0, maxTry = 6;
		while (count < maxTry) {
			try (var g = new OpenCVFrameGrabber(count)) {
				g.start();
				g.grab();
				g.stop();
				count++;
			} catch (Exception e) {
				break;
			}
		}
		return count;
	}

	/**
	 * Asynchronously initializes and caches the number of connected camera devices.
	 * Useful for faster startup of the camera dialog.
	 *
	 * @param display the SWT display (for thread-safety)
	 */
	public static void initCameraCacheAsync(Display display) {
		if (cachedCameraCount != null || cameraCountingInProgress.get())
			return;
		cameraCountingInProgress.set(true);
		new Thread(() -> {
			int count = 0, maxTry = 6;
			String[] names = new String[maxTry];
			while (count < maxTry) {
				try (var g = new OpenCVFrameGrabber(count)) {
					g.start();
					g.grab();
					g.stop();
					names[count] = Messages.PatientCameraCaptureDialog_DeviceName + (count + 1);
					count++;
				} catch (Exception e) {
					break;
				}
			}
			final int cameraCountResult = count;
			cachedCameraCount = cameraCountResult;
			cachedCameraNames = new String[cameraCountResult];
			System.arraycopy(names, 0, cachedCameraNames, 0, cameraCountResult);
			cameraCountingInProgress.set(false);
			if (display != null && !display.isDisposed()) {
				display.asyncExec(() -> logger.info("Camera cache loaded: {} devices.", cameraCountResult));
			}
		}, "InitCameraCache").start();
	}

	/**
	 * Shows an SWT error dialog with the given message.
	 *
	 * @param shell parent shell
	 * @param msg   error message to display
	 */
	private void showError(Shell shell, String msg) {
		logger.error("Camera dialog error: {}", msg);
		MessageBox box = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
		box.setMessage(msg);
		box.open();
	}
}
