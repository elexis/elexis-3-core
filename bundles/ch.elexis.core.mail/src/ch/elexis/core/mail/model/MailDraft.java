package ch.elexis.core.mail.model;

import java.util.List;

/**
 * The complete state of a message being written, as kept by the compose window and stored as draft.
 *
 * @param sessionId
 *            the id of the compose window, stable for the lifetime of the window
 * @param accountId
 *            the sending account
 * @param to
 *            the recipients
 * @param cc
 *            the carbon copy recipients
 * @param bcc
 *            the blind carbon copy recipients
 * @param subject
 *            the subject
 * @param html
 *            the body as produced by the editor, sanitized again before sending
 * @param attachments
 *            the attachments
 * @param messageId
 *            the Message-ID created when the draft was prepared, kept for every save and for sending
 * @param inReplyTo
 *            the Message-ID of the answered message, <code>null</code> if none
 * @param references
 *            the References header values, empty if none
 * @param answeredRef
 *            the answered message that gets the answered flag after sending, <code>null</code> if none
 * @param savedDraftRef
 *            the last stored copy in the drafts folder, <code>null</code> if never stored
 */
public record MailDraft(String sessionId, String accountId, List<MailAddress> to, List<MailAddress> cc,
		List<MailAddress> bcc, String subject, String html, List<MailOutgoingAttachment> attachments, String messageId,
		String inReplyTo, List<String> references, MailRef answeredRef, MailRef savedDraftRef) {
}
