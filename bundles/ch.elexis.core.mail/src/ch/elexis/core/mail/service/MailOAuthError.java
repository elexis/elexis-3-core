package ch.elexis.core.mail.service;

/** Stable, non-sensitive failure reasons of interactive OAuth sign-in. */
public enum MailOAuthError {
	NOT_CONFIGURED, CANCELLED, TIMEOUT, DENIED, STATE_MISMATCH, TOKEN_FAILED, OFFLINE
}
