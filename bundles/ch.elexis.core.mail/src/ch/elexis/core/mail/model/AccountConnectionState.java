package ch.elexis.core.mail.model;

/** Connection state of an account's lazily created work connection. */
public enum AccountConnectionState {
	CONNECTED, CONNECTING, OFFLINE, AUTH_REQUIRED, DISABLED
}
