package ch.elexis.core.mail.model;

/** Outcome of filing one complete message or attachment. */
public enum MailFilingStatus {
	/** Content was saved, verified and linked. */
	FILED,
	/** A live document already exists at the selected patient. */
	ALREADY_FILED,
	/** Filing at another patient requires confirmation. */
	NEEDS_CONFIRMATION,
	/** The new duplicate was removed and the surviving document is returned. */
	MERGED,
	/** Content was verified but the XID could not be added. */
	FILED_WITHOUT_LINK,
	/** The decoded content exceeds the independent 25 MiB filing limit. */
	TOO_LARGE,
	/** Reading or saving failed and the incomplete document was removed. */
	FAILED
}
