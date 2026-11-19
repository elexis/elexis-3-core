package ch.elexis.core.mail;

/** ACL command ids for the mail client; role grants are configured separately. */
public final class MailClientRights {
	/** Allows opening the mail client and creating personal accounts. */
	public static final String MAIL_CLIENT_USE = "MAIL_CLIENT_USE";
	/** Allows filing messages in patient records. */
	public static final String MAIL_CLIENT_FILE = "MAIL_CLIENT_FILE";
	/** Allows managing shared accounts. */
	public static final String MAIL_CLIENT_MANAGE_ACCOUNTS = "MAIL_CLIENT_MANAGE_ACCOUNTS";

	private MailClientRights() {
	}
}
