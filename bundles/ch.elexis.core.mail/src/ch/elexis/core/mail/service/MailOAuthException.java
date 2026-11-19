package ch.elexis.core.mail.service;

/** OAuth failure containing only a stable reason, never a provider response or token. */
public class MailOAuthException extends Exception {
	private static final long serialVersionUID = 1L;
	private final MailOAuthError reason;

	/**
	 * Creates a sanitized sign-in failure.
	 * @param reason the stable failure reason
	 */
	public MailOAuthException(MailOAuthError reason) {
		super("Mail OAuth sign-in failed: " + reason.name());
		this.reason = reason;
	}

	/**
	 * Returns the stable failure reason.
	 * @return the reason
	 */
	public MailOAuthError reason() {
		return reason;
	}
}
