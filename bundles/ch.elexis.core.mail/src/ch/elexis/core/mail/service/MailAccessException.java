package ch.elexis.core.mail.service;

/**
 * Thrown if a user accesses an account that is not visible to the user or that the user may not manage.
 */
public class MailAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates the exception.
	 *
	 * @param message
	 *            the message, without mail addresses or other personal data
	 */
	public MailAccessException(String message) {
		super(message);
	}
}
