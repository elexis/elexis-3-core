package ch.elexis.core.mail.model;

/** Orders message lists by received date and UID. */
public enum MailSortOrder {
	/** Later dates and higher UIDs first. */
	NEWEST_FIRST,
	/** Earlier dates and lower UIDs first. */
	OLDEST_FIRST
}
