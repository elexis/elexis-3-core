package ch.elexis.core.mail.model;

/** Selects messages by their current server flags. */
public enum MailListFilter {
	/** All messages. */
	ALL,
	/** Messages without the seen flag. */
	UNREAD,
	/** Messages with the flagged flag. */
	FLAGGED
}
