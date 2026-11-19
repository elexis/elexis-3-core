package ch.elexis.core.mail;

/** Asynchronous in-process mail event topics and property names; properties must never be logged. */
public final class MailEventTopics {
	/** Carries accountId after account settings or credentials change. */
	public static final String ACCOUNTS = "ch/medelexis/mail/ACCOUNTS";
	/** Carries accountId after a notification mode changes. */
	public static final String NOTIFY = "ch/medelexis/mail/NOTIFY";
	/** Carries accountId, folder, count and headers (in-process sender, subject and opaque ref). */
	public static final String NEW = "ch/medelexis/mail/NEW";
	/** Carries accountId, folder and uids. */
	public static final String CHANGED = "ch/medelexis/mail/CHANGED";
	/** Carries accountId, folder, unread and total. */
	public static final String COUNTS = "ch/medelexis/mail/COUNTS";
	/** Carries accountId, state (enum name) and reason (connect, auth, closed or disabled). */
	public static final String ACCOUNT_STATE = "ch/medelexis/mail/ACCOUNT_STATE";
	/** Carries messageKey, patientId and documentIds. */
	public static final String FILED = "ch/medelexis/mail/FILED";
	/** Subscribes to every mail topic. */
	public static final String ALL = "ch/medelexis/mail/*";
	/** Property accountId. */
	public static final String ACCOUNT_ID = "accountId";
	/** Property folder. */
	public static final String FOLDER = "folder";
	/** Property count. */
	public static final String COUNT = "count";
	/** Property headers. */
	public static final String HEADERS = "headers";
	/** Property page change correlation identifier. */
	public static final String CHANGE_ID = "changeId";
	/** Property uids. */
	public static final String UIDS = "uids";
	/** Property unread. */
	public static final String UNREAD = "unread";
	/** Property total. */
	public static final String TOTAL = "total";
	/** Property state. */
	public static final String STATE = "state";
	/** Property reason. */
	public static final String REASON = "reason";
	/** Property messageKey. */
	public static final String MESSAGE_KEY = "messageKey";
	/** Property patientId. */
	public static final String PATIENT_ID = "patientId";
	/** Property documentIds. */
	public static final String DOCUMENT_IDS = "documentIds";

	private MailEventTopics() {
	}
}
