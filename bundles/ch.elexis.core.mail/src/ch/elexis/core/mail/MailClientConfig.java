package ch.elexis.core.mail;

/** Configuration keys used by the first phase of the mail client. */
public final class MailClientConfig {
	/** Ordered, comma-separated account ids. */
	public static final String ACCOUNTS = "mail/client/accounts";

	/** Per-user ordered sender addresses whose remote images may be displayed. */
	public static final String REMOTE_IMAGE_SENDERS = "mail/client/user/remoteImages/allowedSenders";
	/** Per-user preference controlling subject display in notifications. */
	public static final String POPUP_SHOW_SUBJECT = "mail/client/user/popup/showSubject";
	/** Per-user layout of the mail view (column widths, collapsed accounts, favourite folders) as JSON. */
	public static final String VIEW_STATE = "mail/client/user/viewState";
	/** Per-user recent recipient addresses, newest first, bounded to 200 entries. */
	public static final String RECENT_RECIPIENTS = "mail/client/user/recentRecipients";

	/**
	 * Builds the key for the last observed storage quota.
	 * @param accountId the account id
	 * @return the global quota JSON key
	 */
	public static String quota(String accountId) {
		return "mail/client/quota/" + accountId;
	}

	/**
	 * Builds the key for the observed sent-copy behavior.
	 * @param accountId the account id
	 * @return the global detection enum key
	 */
	public static String sentCopyDetected(String accountId) {
		return "mail/client/sentCopyDetected/" + accountId;
	}

	/**
	 * Builds the key for the provider identity of the signed-in user.
	 * @param accountId the account id
	 * @return the global or owner-contact identity key
	 */
	public static String signedInAs(String accountId) {
		return "mail/client/oauth/" + accountId + "/signedInAs";
	}

	/**
	 * Builds the per-user notification preference key.
	 * @param accountId the visible account id
	 * @return the user-contact configuration key
	 */
	public static String notify(String accountId) {
		return "mail/client/user/notify/" + accountId;
	}

	/**
	 * Builds the inherited-account marker key.
	 * @param accountId the account id
	 * @return the global legacy marker key
	 */
	public static String legacy(String accountId) {
		return checkedAccountKey(accountId, "/legacy");
	}

	/**
	 * Builds the mandator selection key.
	 * @param accountId the account id
	 * @return the global comma-separated mandator ids key
	 */
	public static String mandators(String accountId) {
		return checkedAccountKey(accountId, "/mandators");
	}

	private static String checkedAccountKey(String accountId, String suffix) {
		String key = "mail/client/account/" + accountId + suffix;
		if (key.length() > 80) {
			throw new IllegalArgumentException("Mail configuration key exceeds 80 characters");
		}
		return key;
	}

	private MailClientConfig() {
	}

	/**
	 * Builds an account definition key.
	 * @param accountId the account id
	 * @return the global JSON key
	 */
	public static String account(String accountId) {
		return "mail/client/account/" + accountId;
	}

	/**
	 * Builds a password key.
	 * @param accountId the account id
	 * @return the global or owner-contact secret key
	 */
	public static String password(String accountId) {
		return "mail/client/secret/" + accountId + "/password";
	}

	/**
	 * Builds a refresh-token key.
	 * @param accountId the account id
	 * @return the global or owner-contact secret key
	 */
	public static String refreshToken(String accountId) {
		return "mail/client/secret/" + accountId + "/refreshToken";
	}

	/**
	 * Builds the assignment progress key.
	 * @param accountId the account id
	 * @return the global progress key
	 */
	public static String assignProgress(String accountId) {
		return "mail/client/assign/" + accountId + "/progress";
	}

	/**
	 * Builds the assignment retry key.
	 * @param accountId the account id
	 * @return the global retry key
	 */
	public static String assignAttempts(String accountId) {
		return "mail/client/assign/" + accountId + "/attempts";
	}

	/**
	 * Builds the global cleanup rules key.
	 * @param accountId the account id
	 * @return the global configuration key
	 */
	public static String cleanupRules(String accountId) {
		return "mail/client/cleanup/" + accountId + "/rules";
	}

	/**
	 * Builds the global cleanup trashMarks key.
	 * @param accountId the account id
	 * @return the global configuration key
	 */
	public static String cleanupTrashMarks(String accountId) {
		return "mail/client/cleanup/" + accountId + "/trashMarks";
	}

	/**
	 * Builds the global cleanup lastRun key.
	 * @param accountId the account id
	 * @return the global configuration key
	 */
	public static String cleanupLastRun(String accountId) {
		return "mail/client/cleanup/" + accountId + "/lastRun";
	}
}
