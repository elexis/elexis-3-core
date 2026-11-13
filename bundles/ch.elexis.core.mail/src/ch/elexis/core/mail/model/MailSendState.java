package ch.elexis.core.mail.model;

/**
 * State of one sending attempt.
 */
public enum MailSendState {
	QUEUED, SENDING, SENT, UNCERTAIN, FAILED
}
