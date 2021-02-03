package ch.elexis.core.model.message;

public class MessageCode {

	public final class Key {

		private Key() {
		}

		public static final String Severity = "severity";

		/**
		 * A sub-identificator for the sender. E.g. the task system of the sender
		 * "Server".
		 */
		public static final String SenderSubId = "senderSubId";

		/**
		 * A formal unique for a message, may be used to enhance the message text. See
		 * {@link MessageCodeMessageId}
		 */
		public static final String MessageId = "messageId";
	
		/**
		 * A parameter that may be used within {@link #MessageId}
		 */
		public static final String MessageIdParam = "messageIdParam";
	}

	public final class Value {

		private Value() {
		}

		public static final String Severity_INFO = "info";

		public static final String Severity_WARN = "warn";
		
		public static final String Severity_ERROR = "error";
	}

}
