package ch.elexis.core.mail.model;

/**
 * Special purpose of a folder, detected from the LIST attributes or, as a fallback, from the folder name.
 */
public enum MailFolderRole {
	INBOX, SENT, DRAFTS, TRASH, JUNK, ARCHIVE, NONE
}
