package ch.elexis.core.ui.contacts.views.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jna.Callback;
import com.sun.jna.CallbackReference;
import com.sun.jna.Memory;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 * macOS specific glue for the camera privacy handling (TCC).
 *
 * <p>
 * Without a granted camera permission AVFoundation reports no devices at all
 * and stays silent, which surfaces as "no camera found". The permission has to
 * be asked for explicitly, see {@link #ensureCameraAccess(Display)}. It is only
 * ever granted if {@code NSCameraUsageDescription} is present in the Info.plist
 * of the application bundle.
 * </p>
 */
public final class MacOSCameraSupport {

	private static final Logger logger = LoggerFactory.getLogger(MacOSCameraSupport.class);

	private static final boolean MAC_OS = System.getProperty("os.name", "").toLowerCase().contains("mac");

	private static final String OPENCV_SKIP_AUTH_ENV = "OPENCV_AVFOUNDATION_SKIP_AUTH";

	private static final int STATUS_NOT_DETERMINED = 0;
	private static final int STATUS_AUTHORIZED = 3;

	private static final long ANSWER_TIMEOUT_MS = 120000L;

	private static Boolean openCvAuthDisabled;

	/** Everything handed to the native side; the garbage collector must not take it. */
	private static final List<Object> nativeKeepAlive = new ArrayList<>();

	/** Descriptor of the block literal, allocated once and deliberately never freed. */
	private static Memory blockDescriptor;

	private MacOSCameraSupport() {
	}

	public static boolean isMacOS() {
		return MAC_OS;
	}

	/**
	 * Tells OpenCV not to request the camera permission itself; from a worker
	 * thread that request fails and takes the JVM down with a SIGSEGV. Has to run
	 * before the native videoio library is loaded, that is before the first
	 * {@code OpenCVFrameGrabber} is created. OpenCV reads the flag with
	 * {@code getenv()}, so it has to go into the real process environment.
	 *
	 * @return <code>true</code> if the flag is set, or if this is not macOS
	 */
	public static synchronized boolean disableOpenCvAuthRequest() {
		if (!MAC_OS) {
			return true;
		}
		if (openCvAuthDisabled != null) {
			return openCvAuthDisabled.booleanValue();
		}
		boolean done = false;
		try {
			int rc = NativeLibrary.getInstance("c").getFunction("setenv")
					.invokeInt(new Object[] { OPENCV_SKIP_AUTH_ENV, "1", Integer.valueOf(1) });
			done = (rc == 0);
			if (!done) {
				logger.warn("setenv({}) returned {}.", OPENCV_SKIP_AUTH_ENV, Integer.valueOf(rc));
			}
		} catch (Throwable t) {
			logger.warn("Could not set {}: {}", OPENCV_SKIP_AUTH_ENV, t.toString());
		}
		openCvAuthDisabled = Boolean.valueOf(done);
		return done;
	}

	/**
	 * Makes sure the camera permission has been asked for, and waits for the
	 * answer. Call this from the UI thread before probing for devices: the request
	 * only works from the main thread, and the run loop has to keep turning while
	 * the dialog is up.
	 *
	 * @param display the display whose event loop is turned while waiting
	 * @return <code>true</code> if the camera may be used
	 */
	public static boolean ensureCameraAccess(Display display) {
		if (!MAC_OS) {
			return true;
		}
		try {
			long status = authorizationStatus();
			if (status == STATUS_AUTHORIZED) {
				return true;
			}
			if (status != STATUS_NOT_DETERMINED) {
				logger.warn("Camera access is not granted (authorizationStatus={}); it has to be enabled in the"
						+ " system settings under Privacy & Security / Camera.", Long.valueOf(status));
				return false;
			}
			return requestAccess(display);
		} catch (Throwable t) {
			logger.warn("Could not request camera access: {}", t.toString());
			return false;
		}
	}

	/**
	 * @return <code>true</code> if the camera permission has been refused, by the
	 *         user or by policy. It then has to be granted in the system settings,
	 *         asking again has no effect.
	 */
	public static boolean isCameraAccessDenied() {
		if (!MAC_OS) {
			return false;
		}
		try {
			long status = authorizationStatus();
			return status != STATUS_AUTHORIZED && status != STATUS_NOT_DETERMINED;
		} catch (Throwable t) {
			logger.warn("Could not read the camera authorization status: {}", t.toString());
			return false;
		}
	}

	/**
	 * @return the TCC camera authorization status of this process: 0 not
	 *         determined, 1 restricted, 2 denied, 3 authorized
	 */
	private static long authorizationStatus() {
		NativeLibrary.getInstance("/System/Library/Frameworks/AVFoundation.framework/AVFoundation");
		NativeLibrary objc = NativeLibrary.getInstance("objc");

		Pointer captureDeviceClass = objc.getFunction("objc_getClass")
				.invokePointer(new Object[] { "AVCaptureDevice" });
		Pointer selStatus = objc.getFunction("sel_registerName")
				.invokePointer(new Object[] { "authorizationStatusForMediaType:" });
		return objc.getFunction("objc_msgSend")
				.invokeLong(new Object[] { captureDeviceClass, selStatus, mediaTypeVideo(objc) });
	}

	/** @return the {@code AVMediaTypeVideo} constant as an NSString */
	private static Pointer mediaTypeVideo(NativeLibrary objc) {
		Pointer nsStringClass = objc.getFunction("objc_getClass").invokePointer(new Object[] { "NSString" });
		Pointer selStringWithUTF8 = objc.getFunction("sel_registerName")
				.invokePointer(new Object[] { "stringWithUTF8String:" });
		return objc.getFunction("objc_msgSend")
				.invokePointer(new Object[] { nsStringClass, selStringWithUTF8, "vide" });
	}

	private static boolean requestAccess(Display display) {
		final AtomicBoolean answered = new AtomicBoolean(false);
		final AtomicBoolean granted = new AtomicBoolean(false);

		AccessCallback callback = (block, grantedFlag) -> {
			granted.set(grantedFlag != 0);
			answered.set(true);
			if (display != null && !display.isDisposed()) {
				display.wake();
			}
		};
		nativeKeepAlive.add(callback);

		NativeLibrary objc = NativeLibrary.getInstance("objc");
		Pointer captureDeviceClass = objc.getFunction("objc_getClass")
				.invokePointer(new Object[] { "AVCaptureDevice" });
		Pointer selRequest = objc.getFunction("sel_registerName")
				.invokePointer(new Object[] { "requestAccessForMediaType:completionHandler:" });

		BlockLiteral block = new BlockLiteral();
		block.isa = NativeLibrary.getInstance("System").getGlobalVariableAddress("_NSConcreteGlobalBlock");
		block.flags = 1 << 28; // BLOCK_IS_GLOBAL
		block.reserved = 0;
		block.invoke = CallbackReference.getFunctionPointer(callback);
		block.descriptor = blockDescriptor(block.size());
		block.write();
		nativeKeepAlive.add(block);

		logger.info("Requesting camera permission from macOS.");
		objc.getFunction("objc_msgSend").invokeVoid(
				new Object[] { captureDeviceClass, selRequest, mediaTypeVideo(objc), block.getPointer() });

		long deadline = System.currentTimeMillis() + ANSWER_TIMEOUT_MS;
		while (!answered.get() && System.currentTimeMillis() < deadline) {
			if (display == null || display.isDisposed()) {
				break;
			}
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		if (!answered.get()) {
			logger.warn("No answer to the camera permission request.");
			return false;
		}
		logger.info("Camera permission {}.", granted.get() ? "granted" : "denied");
		return granted.get();
	}

	/**
	 * The descriptor is the same for every block literal, so one allocation serves
	 * them all. It has to stay valid as long as any block is alive, hence it is
	 * held in a field and never closed.
	 *
	 * @param blockSize size of the block literal in bytes
	 */
	private static synchronized Memory blockDescriptor(int blockSize) {
		if (blockDescriptor == null) {
			blockDescriptor = new Memory(16);
			blockDescriptor.setLong(0, 0L);
			blockDescriptor.setLong(8, blockSize);
		}
		return blockDescriptor;
	}

	/** The memory layout of an Objective-C block literal, as the ABI defines it. */
	public static class BlockLiteral extends Structure {
		public Pointer isa;
		public int flags;
		public int reserved;
		public Pointer invoke;
		public Pointer descriptor;

		@Override
		protected List<String> getFieldOrder() {
			return Arrays.asList("isa", "flags", "reserved", "invoke", "descriptor");
		}
	}

	/** Completion handler of {@code requestAccessForMediaType:completionHandler:}. */
	public interface AccessCallback extends Callback {
		void invoke(Pointer block, byte granted);
	}
}
