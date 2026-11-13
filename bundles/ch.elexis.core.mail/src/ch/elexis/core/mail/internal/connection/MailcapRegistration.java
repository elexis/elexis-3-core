package ch.elexis.core.mail.internal.connection;

import jakarta.activation.CommandMap;
import jakarta.activation.MailcapCommandMap;

/**
 * Registers the Angus content handlers with Jakarta Activation, which does not find the Angus mailcap file inside
 * OSGi. Same entries as <code>ch.elexis.core.mail.internal.MailClient</code>.
 */
public final class MailcapRegistration {

	private MailcapRegistration() {
	}

	/**
	 * Adds the handlers for text, multipart and message types to the default command map.
	 */
	public static void register() {
		MailcapCommandMap map = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
		map.addMailcap("text/html;; x-java-content-handler=org.eclipse.angus.mail.handlers.text_html");
		map.addMailcap("text/xml;; x-java-content-handler=org.eclipse.angus.mail.handlers.text_xml");
		map.addMailcap("text/plain;; x-java-content-handler=org.eclipse.angus.mail.handlers.text_plain");
		map.addMailcap("multipart/*;; x-java-content-handler=org.eclipse.angus.mail.handlers.multipart_mixed");
		map.addMailcap("message/rfc822;; x-java-content-handler=org.eclipse.angus.mail.handlers.message_rfc822");
		map.addMailcap("message/disposition-notification;;x-java-content-handler=org.eclipse.angus.mail.handlers.text_plain");
		CommandMap.setDefaultCommandMap(map);
	}
}
