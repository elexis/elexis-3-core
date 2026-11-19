package ch.elexis.core.mail.model;

import java.util.List;

/**
 * A message prepared for display. The HTML body is already sanitized.
 *
 * @param header
 *            the header data of the message
 * @param to
 *            the recipients
 * @param cc
 *            the carbon copy recipients
 * @param sanitizedHtml
 *            the sanitized HTML body, <code>null</code> if the message has only plain text
 * @param plainText
 *            the plain text body, <code>null</code> if the message has only HTML
 * @param attachments
 *            the attachments and inline parts
 */
public record MailMessageView(MailHeader header, List<MailAddress> to, List<MailAddress> cc, String sanitizedHtml,
		String plainText, List<MailAttachment> attachments) {
}
