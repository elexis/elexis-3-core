package ch.elexis.core.mail;

import java.util.StringJoiner;

/** Describes failures for the log without exception messages, which may carry addresses or folder names. */
public final class MailLog {
	private MailLog() {
	}

	/**
	 * Lists the classes of a failure and its causes, never their messages.
	 * @param failure the caught exception
	 * @return for example <code>MessagingException &lt;- SocketTimeoutException</code>
	 */
	public static String causes(Throwable failure) {
		StringJoiner chain = new StringJoiner(" <- ");
		for (Throwable cause = failure; cause != null; cause = cause.getCause()) {
			chain.add(cause.getClass().getSimpleName());
		}
		return chain.toString();
	}
}
