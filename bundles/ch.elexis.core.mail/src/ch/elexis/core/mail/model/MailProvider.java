package ch.elexis.core.mail.model;

/**
 * Provider of a mailbox. Only the default values of an account depend on it, never the behavior of the connector.
 */
public enum MailProvider {
	IMAP, HIN, M365, GOOGLE
}
