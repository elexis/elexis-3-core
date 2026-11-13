package ch.elexis.core.mail.model;

/** Describes whether a server supplied a usable storage quota. */
public enum MailQuotaStatus {
	/** A STORAGE resource was read successfully. */
	OK,
	/** The server does not advertise QUOTA. */
	UNSUPPORTED,
	/** No usable quota is known. */
	UNKNOWN
}
