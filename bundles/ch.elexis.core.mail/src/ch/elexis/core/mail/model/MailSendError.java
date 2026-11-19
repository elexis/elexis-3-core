package ch.elexis.core.mail.model;

/**
 * Reason of a failed or uncertain sending attempt, shown to the user in plain words.
 */
public enum MailSendError {
	UNREACHABLE, AUTH_FAILED, RECIPIENT_REJECTED, TOO_LARGE, THROTTLED, TIMEOUT_AFTER_DATA, FAILED
}
