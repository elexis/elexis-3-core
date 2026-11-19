package ch.elexis.core.mail.model;

/**
 * Transport security of a server connection. <code>STARTTLS</code> is always required, there is no fallback to
 * plain text.
 */
public enum MailSecurity {
	SSL, STARTTLS, NONE
}
