package ch.elexis.core.mail.service;

import jakarta.mail.MessagingException;

/** Indicates that a filed message exceeds the 30 MiB display limit. */
public final class MailFiledTooLargeException extends MessagingException {
	private static final long serialVersionUID = 1L;

	/** Creates the display-limit error without document content. */
	public MailFiledTooLargeException() {
		super("Filed message exceeds display size limit");
	}
}
