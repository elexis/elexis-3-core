package ch.elexis.core.mail.service;

import jakarta.mail.MessagingException;

/**
 * Thrown if a referenced message or part no longer exists, for example because another user moved it.
 */
public class MailNotFoundException extends MessagingException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates the exception.
	 *
	 * @param message
	 *            the message, without subjects or other personal data
	 */
	public MailNotFoundException(String message) {
		super(message);
	}
}
