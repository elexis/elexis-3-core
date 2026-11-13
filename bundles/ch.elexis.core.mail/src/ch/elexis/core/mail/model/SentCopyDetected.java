package ch.elexis.core.mail.model;

/** Records whether an AUTO account stores its own server-side sent copies. */
public enum SentCopyDetected {
	/** No successful detection has completed. */
	UNKNOWN,
	/** The server stores sent copies. */
	SERVER_COPIES,
	/** The client must store sent copies. */
	NO_SERVER_COPY
}
