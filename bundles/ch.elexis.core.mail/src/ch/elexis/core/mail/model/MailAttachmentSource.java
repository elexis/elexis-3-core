package ch.elexis.core.mail.model;

/**
 * Origin of the content of an outgoing attachment.
 */
public enum MailAttachmentSource {
	/** A local file chosen by the user. */
	FILE,
	/** A part of an existing message, used when forwarding. */
	ORIGINAL_PART,
	/** A complete existing message, attached as message/rfc822. */
	ORIGINAL_MESSAGE,
	/** A decoded part of a filed document, loaded with the active user at build time. */
	FILED_PART
}
