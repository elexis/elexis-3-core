package ch.elexis.core.mail.model;

/** Independent result of an IMAP or SMTP login probe. */
public enum MailAccountTestOutcome {
	OK, AUTH_FAILED, UNREACHABLE, TLS_FAILED, NOT_SIGNED_IN, FAILED
}
