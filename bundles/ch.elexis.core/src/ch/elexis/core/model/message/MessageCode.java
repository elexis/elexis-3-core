package ch.elexis.core.model.message;

public class MessageCode {
	
	public final class Key {
		
		private Key(){}
		
		public static final String Severity = "severity";
		
		/**
		 * A sub-identificator for the sender. E.g. the task system of the sender "Server".
		 */
		public static final String SenderSubId = "senderSubId";
		
	}
	
	public final class Value {
		
		private Value(){}
		
		public static final String Severity_INFO = "info";
		
		public static final String Severity_WARN = "warn";
	}
	
}
