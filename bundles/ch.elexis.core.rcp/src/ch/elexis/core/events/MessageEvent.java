package ch.elexis.core.events;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.utils.OsgiServiceUtil;

/**
 * Send a synchronous message event to the user via the Osgi event service
 *
 * @since 3.8
 */
public class MessageEvent {

	public enum LEVEL {
		ERROR, WARNING, INFO
	};

	public static void fireError(String title, String message) {
		fire(LEVEL.ERROR, title, message, null, false);
	}

	public static void fireError(String title, String message, Exception ex) {
		fire(LEVEL.ERROR, title, message, ex, false);
	}

	public static void fireLoggedError(String title, String message) {
		fire(LEVEL.ERROR, title, message, null, true);
	}

	public static void fireLoggedError(String title, String message, Exception ex) {
		fire(LEVEL.ERROR, title, message, ex, true);
	}

	public static void fireInformation(String title, String message) {
		fire(LEVEL.INFO, title, message, null, true);
	}

	public static void fireInformation(String title, String message, boolean log) {
		fire(LEVEL.INFO, title, message, null, log);
	}

	public static void fireWarninig(String title, String message) {
		fire(LEVEL.WARNING, title, message, null, true);
	}

	public static void fireWarninig(String title, String message, boolean log) {
		fire(LEVEL.WARNING, title, message, null, log);
	}

	private static void fire(LEVEL level, String title, String message, Exception ex, boolean log) {

		Logger logger = null;
		String logMsg = title + " - " + message;
		if (log) {
			logger = LoggerFactory.getLogger(MessageEvent.class);
		}

		String topic;
		if (level == LEVEL.ERROR) {
			topic = ElexisEventTopics.NOTIFICATION_ERROR;
			if (log && logger != null) {
				if (ex == null) {
					logger.error(logMsg);
				} else {
					logger.error(logMsg, ex);
				}
			}

		} else if (level == LEVEL.WARNING) {
			topic = ElexisEventTopics.NOTIFICATION_WARN;
			if (log && logger != null) {
				if (ex == null) {
					logger.warn(logMsg);
				} else {
					logger.warn(logMsg, ex);
				}
			}

		} else {
			topic = ElexisEventTopics.NOTIFICATION_INFO;
			if (log && logger != null) {
				if (ex == null) {
					logger.info(logMsg);
				} else {
					logger.info(logMsg, ex);
				}
			}

		}

		Map<String, String> properties = new HashMap<>();
		properties.put(ElexisEventTopics.NOTIFICATION_PROPKEY_TITLE, title);
		properties.put(ElexisEventTopics.NOTIFICATION_PROPKEY_MESSAGE, message);

		Event event = new Event(topic, properties);
		Optional<EventAdmin> eventAdmin = OsgiServiceUtil.getService(EventAdmin.class);
		if (eventAdmin.isPresent()) {
			eventAdmin.get().sendEvent(event);
		} else {
			LoggerFactory.getLogger(MessageEvent.class)
					.error("EventAdmin not available. Message not delivered: [{} / {} / {}]", level, title, message);
		}

	}

}
