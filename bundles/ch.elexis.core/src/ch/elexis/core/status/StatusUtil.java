package ch.elexis.core.status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;

import ch.elexis.core.jdt.NonNull;

// TODO refactor ...
public class StatusUtil {
	public static void printStatus(Logger log, IStatus status) {
		print(log, StringUtils.EMPTY, status);
	}

	public static void print(Logger log, String indent, IStatus status) {
		if (status.isMultiStatus()) {
			log.warn(indent + status.getMessage().replace('\n', ' '));
			String childIndent = indent + "  ";
			for (IStatus c : status.getChildren()) {
				print(log, childIndent, c);
			}
		} else {
			log.warn(indent + status.getMessage().replace('\n', ' '));
		}
	}

	public static void printStatus(PrintStream out, IStatus status) {
		print(out, StringUtils.EMPTY, status);
	}

	public static String printStatus(IStatus status) {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			PrintStream ps = new PrintStream(baos);
			printStatus(ps, status);
			return baos.toString(Charset.defaultCharset().toString());
		} catch (IOException e) {
			return e.getMessage();
		}
	}

	private static void print(PrintStream printStream, String indent, IStatus status) {
		if (status.isMultiStatus()) {
			String severityString = getSeverityString(status.getSeverity());
			printStream.print("[" + severityString + "] " + indent + status.getMessage());
			String childIndent = indent + "  ";
			for (IStatus c : status.getChildren()) {
				print(printStream, childIndent, c);
			}
		} else if (status instanceof ObjectStatus) {
			ObjectStatus os = (ObjectStatus) status;
			printStream.println(os.getObject().toString());
		} else {
			printStream.println(indent + status.getMessage());
		}
	}

	public static String getSeverityString(int severity) {
		switch (severity) {
		case Status.OK:
			return "OK";
		case Status.WARNING:
			return "WARNING";
		case Status.ERROR:
			return "ERROR";
		case Status.INFO:
			return "INFO";
		case Status.CANCEL:
			return "CANCEL";
		default:
			return "? " + severity + " ?";
		}
	}

	/**
	 * Create a Status.ERROR telling the user to search the logfile for explanation.
	 *
	 * @param pluginId
	 * @return
	 */
	public static IStatus errorSeeLog(String pluginId) {
		return new Status(Status.ERROR, pluginId, "Execution error, see log.");
	}

	/**
	 * Log a status to the corresponding log-level; does nothing if
	 * {@link Status#isOK()}
	 *
	 * @param prependMessage              an optional message to prepend the status
	 *                                    message
	 * @param log
	 * @param status
	 * @param includeExceptionIfAvailable
	 * @param logDebugIfOk                log to level debug if the status is ok
	 */
	public static void logStatus(String prependMessage, @NonNull Logger log, @NonNull IStatus status,
			boolean includeExceptionIfAvailable, boolean logDebugIfOk) {
		if (status.isOK() && !logDebugIfOk) {
			return;
		}

		StringBuilder sb = new StringBuilder();
		if (status.isMultiStatus()) {
			sb.append("[MULTISTATUS] ");
		}
		if (prependMessage != null) {
			sb.append(prependMessage + StringUtils.SPACE);
		}
		sb.append("(c" + status.getCode() + "/s" + status.getSeverity() + ") ");
		sb.append(status.getMessage());
		String message = sb.toString();

		boolean includeException = (includeExceptionIfAvailable && status.getException() != null);

		int severity = status.getSeverity();
		switch (severity) {
		case Status.ERROR:
			if (includeException) {
				log.error(message, status.getException());
			} else {
				log.error(message);
			}
			break;
		case Status.WARNING:
			if (includeException) {
				log.warn(message, status.getException());
			} else {
				log.warn(message);
			}
			break;
		case Status.INFO:
		case Status.CANCEL:
			if (includeException) {
				log.info(message, status.getException());
			} else {
				log.info(message);
			}
			break;
		case Status.OK:
			log.debug(message);
			break;
		default:
			break;
		}

		if (status.isMultiStatus()) {
			Arrays.asList(status.getChildren()).stream().forEach(c -> logStatus(prependMessage, log, c, true, false));
		}
	}

	/**
	 * convenience method, includes exception if available
	 *
	 * @param log
	 * @param status
	 * @see #logStatus(String, Logger, IStatus, boolean, boolean)
	 */
	public static void logStatus(Logger log, IStatus status) {
		logStatus(null, log, status, true, false);
	}

	/**
	 * convenience method, includes exception if available
	 *
	 * @param prependMessage
	 * @param log
	 * @param status
	 * @see #logStatus(String, Logger, IStatus, boolean, boolean)
	 */
	public static void logStatus(String prependMessage, Logger log, IStatus status) {
		logStatus(prependMessage, log, status, true, false);
	}

	/**
	 * convenience method, includes exception if available
	 *
	 * @param log
	 * @param status
	 * @param includeExceptionIfAvailable
	 * @see #logStatus(String, Logger, IStatus, boolean, boolean)
	 */
	public static void logStatus(Logger log, IStatus status, boolean includeExceptionIfAvailable) {
		logStatus(null, log, status, includeExceptionIfAvailable, false);
	}

}
