package ch.elexis.core.mail.messages;

import org.eclipse.osgi.util.NLS;

/** Localized prefixes and original-message headers used when preparing mail. */
public final class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.mail.messages.messages";
	/** Reply subject prefix. */
	public static String ReplyPrefix;
	/** Forward subject prefix. */
	public static String ForwardPrefix;
	/** Failed loopback sign-in response. */
	public static String OAuthFailure;
	/** Successful loopback sign-in response. */
	public static String OAuthSuccess;
	/** Reply quote header with date and sender placeholders. */
	public static String QuoteHeader;
	/** Forward sender label. */
	public static String From;
	/** Forward date label. */
	public static String Date;
	/** Forward recipient label. */
	public static String To;
	/** Forward subject label. */
	public static String Subject;

	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
