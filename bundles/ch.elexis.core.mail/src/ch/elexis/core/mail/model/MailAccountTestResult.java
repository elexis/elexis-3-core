package ch.elexis.core.mail.model;

/**
 * Results of independent login probes without sending mail or opening folders.
 * @param imap the IMAP outcome, or null when no IMAP endpoint is configured
 * @param smtp the SMTP outcome, or null when no SMTP endpoint is configured
 */
public record MailAccountTestResult(MailAccountTestOutcome imap, MailAccountTestOutcome smtp) {
}
