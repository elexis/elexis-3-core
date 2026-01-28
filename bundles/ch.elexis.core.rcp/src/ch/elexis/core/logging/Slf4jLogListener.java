package ch.elexis.core.logging;

import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapts platform logs to the slf4j logging service
 */
public class Slf4jLogListener implements ILogListener {

	private Logger logger = LoggerFactory.getLogger("PLATFORM");

	@Override
	public void logging(IStatus status, String plugin) {
		String message = "[" + plugin + "] " + status.getMessage();
		Throwable exception = status.getException();

		switch (status.getSeverity()) {
		case IStatus.ERROR:
			logger.error(message, exception);
			break;
		case IStatus.WARNING:
			logger.warn(message, exception);
			break;
		case IStatus.INFO:
			logger.info(message, exception);
			break;
		default:
			logger.debug(message, exception);
			break;
		}

	}

}
