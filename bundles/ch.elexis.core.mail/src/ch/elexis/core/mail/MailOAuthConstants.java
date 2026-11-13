package ch.elexis.core.mail;

/** Public provider endpoints and delegated scopes for desktop OAuth sign-in. */
public final class MailOAuthConstants {
	/** Microsoft authorization endpoint, for the common tenant. */
	public static final String MICROSOFT_AUTHORIZE = "https://login.microsoftonline.com/common/oauth2/v2.0/authorize";
	/** Microsoft token endpoint, for the common tenant. */
	public static final String MICROSOFT_TOKEN = "https://login.microsoftonline.com/common/oauth2/v2.0/token";
	/** Microsoft delegated IMAP and SMTP permissions and offline identity scopes. */
	public static final String MICROSOFT_SCOPE = "https://outlook.office.com/IMAP.AccessAsUser.All "
			+ "https://outlook.office.com/SMTP.Send offline_access openid email";
	/** Google authorization endpoint. */
	public static final String GOOGLE_AUTHORIZE = "https://accounts.google.com/o/oauth2/v2/auth";
	/** Google token endpoint. */
	public static final String GOOGLE_TOKEN = "https://oauth2.googleapis.com/token";
	/** Google IMAP/SMTP and identity scopes. */
	public static final String GOOGLE_SCOPE = "https://mail.google.com/ openid email";

	private MailOAuthConstants() {
	}
}
