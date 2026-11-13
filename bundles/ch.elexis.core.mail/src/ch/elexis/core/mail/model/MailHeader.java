package ch.elexis.core.mail.model;

import java.time.Instant;
import java.util.Set;

/**
 * Header data shown in the message list.
 *
 * @param ref
 *            the reference to the message
 * @param messageKey
 *            the normalized Message-ID, or a content hash if the message has none
 * @param fromName
 *            the personal name of the sender, may be empty
 * @param fromAddress
 *            the address of the sender
 * @param subject
 *            the decoded subject
 * @param sentAt
 *            the sent date, <code>null</code> if the message has none
 * @param receivedAt
 *            the received date, <code>null</code> if the server has none
 * @param flags
 *            the system flags of the message
 * @param hasAttachments
 *            list estimate: top-level multipart/mixed; exact after open
 */
public record MailHeader(MailRef ref, String messageKey, String fromName, String fromAddress, String subject,
		Instant sentAt, Instant receivedAt, Set<MailFlag> flags, boolean hasAttachments) {

	/**
	 * Creates a header without a received date for existing callers.
	 * @param ref the message identity
	 * @param messageKey the normalized Message-ID or content hash
	 * @param fromName the sender name
	 * @param fromAddress the sender address
	 * @param subject the decoded subject
	 * @param sentAt the nullable sent date
	 * @param flags the system flags
	 * @param hasAttachments whether attachments are present
	 */
	public MailHeader(MailRef ref, String messageKey, String fromName, String fromAddress, String subject,
			Instant sentAt, Set<MailFlag> flags, boolean hasAttachments) {
		this(ref, messageKey, fromName, fromAddress, subject, sentAt, null, flags, hasAttachments);
	}
}
