package ch.elexis.core.mail.service;

import jakarta.mail.MessagingException;

/** Signals that an account has no selectable folder with the trash role. */
public class MailNoTrashFolderException extends MessagingException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates a failure without account or folder details.
	 * @param message the description without personal data
	 */
	public MailNoTrashFolderException(String message) {
		super(message);
	}
}
