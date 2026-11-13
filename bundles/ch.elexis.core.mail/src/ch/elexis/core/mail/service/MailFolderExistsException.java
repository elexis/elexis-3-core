package ch.elexis.core.mail.service;

import jakarta.mail.MessagingException;

/**
 * Thrown if a folder creation or rename would overwrite an existing folder.
 */
public class MailFolderExistsException extends MessagingException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates the exception.
	 *
	 * @param message
	 *            the message, without subjects or other personal data
	 */
	public MailFolderExistsException(String message) {
		super(message);
	}
}
