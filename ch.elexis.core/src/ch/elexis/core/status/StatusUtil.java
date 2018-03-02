package ch.elexis.core.status;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.slf4j.Logger;

import ch.elexis.core.jdt.NonNull;

// TODO refactor ...
public class StatusUtil {
	public static void printStatus(Logger log, IStatus status) {
		print(log, "", status);
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
		print(out, "", status);
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
			printStream.print(indent + status.getMessage());
			String childIndent = indent + "  ";
			for (IStatus c : status.getChildren()) {
				print(printStream, childIndent, c);
			}
		} else if (status instanceof ObjectStatus) {
			ObjectStatus os = (ObjectStatus) status;
			printStream.println((os != null) ? os.getObject().toString() : null);
		} else {
			printStream.println(indent + status.getMessage());
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
	 * @param log
	 * @param status
	 * @param includeExceptionIfAvailable
	 */
	public static void logStatus(@NonNull Logger log, @NonNull IStatus status, boolean includeExceptionIfAvailable) {
		if (status.isOK()) {
			return;
		}

		String message = (status.isMultiStatus()) ? "[MULTISTATUS] " + status.getMessage() : status.getMessage();
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
		default:
			break;
		}

		if (status.isMultiStatus()) {
			Arrays.asList(status.getChildren()).stream().forEach(c -> logStatus(log, c, true));
		}
	}

	/**
	 * convenience method
	 * 
	 * @param log
	 * @param status
	 */
	public static void logStatus(Logger log, IStatus status) {
		logStatus(log, status, true);
	}

}
