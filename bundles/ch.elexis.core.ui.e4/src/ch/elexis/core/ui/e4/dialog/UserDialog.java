package ch.elexis.core.ui.e4.dialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A dialog to present messages to the user.
 *
 * @since 3.8
 */
public class UserDialog {

	private static Logger logger = LoggerFactory.getLogger(UserDialog.class);

	public enum MessageType {
		INFO, WARN, ERROR, QUESTION
	}

	final public String title;
	final public String message;
	final public MessageType mt;
	final public IStatus status;

	public UserDialog(MessageType mt, String title, String message) {
		this(mt, title, message, null);
	}

	public UserDialog(MessageType mt, String title, String message, IStatus status) {
		this.title = title;
		this.message = message;
		this.mt = mt;
		this.status = status;
	}

	/**
	 * Fire this message
	 */
	public boolean open() {
		final Display display = Display.getDefault();
		UserDialogRunnable runnable = new UserDialogRunnable(display);
		display.syncExec(runnable);
		return runnable.getResult();
	}

	private class UserDialogRunnable implements Runnable {

		private boolean result = false;
		private final Display display;

		public UserDialogRunnable(Display display) {
			this.display = display;
		}

		@Override
		public void run() {
			logger.debug("MessageEvent [" + mt + "]  [" + title + "] [" + message + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			switch (mt) {
			case ERROR:
				org.eclipse.jface.dialogs.MessageDialog.openError(display.getActiveShell(), title, message);
				result = true;
				break;
			case WARN:
				org.eclipse.jface.dialogs.MessageDialog.openWarning(display.getActiveShell(), title, message);
				result = true;
				break;
			case QUESTION:
				result = MessageDialog.openQuestion(display.getActiveShell(), title, message);
				break;
			default:
				org.eclipse.jface.dialogs.MessageDialog.openInformation(display.getActiveShell(), title, message);
				result = true;
				break;
			}
		}

		public boolean getResult() {
			return result;
		}

	}

	public static void error(String title, String message) {
		open(MessageType.ERROR, title, message, null, null, false);
	}

	public static void error(String title, String message, Exception ex) {
		open(MessageType.ERROR, title, message, null, ex, false);
	}

	public static void loggedError(String title, String message) {
		open(MessageType.ERROR, title, message, null, null, true);
	}

	public static void loggedError(String title, String message, Exception ex) {
		open(MessageType.ERROR, title, message, null, ex, true);
	}

	public static void information(String title, String message) {
		open(MessageType.INFO, title, message, null, null, true);
	}

	public static boolean question(String title, String message) {
		return open(MessageType.QUESTION, title, message, null, null, true);
	}

	private static boolean open(MessageType mt, String title, String message, IStatus status, Exception ex,
			boolean log) {
		if (log) {
			String logMsg = title + " - " + message; //$NON-NLS-1$
			switch (mt) {
			case ERROR:
				if (ex == null) {
					logger.error(logMsg);
				} else {
					logger.error(logMsg, ex);
				}
				break;
			case WARN:
				if (ex == null) {
					logger.warn(logMsg);
				} else {
					logger.warn(logMsg, ex);
				}
				break;
			case INFO:
			case QUESTION:
				if (ex == null) {
					logger.info(logMsg);
				} else {
					logger.info(logMsg, ex);
				}
				break;
			}
		}
		return new UserDialog(mt, title, message, status).open();
	}

}
