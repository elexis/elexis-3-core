package ch.elexis.core.mail.model;

/**
 * Metadata of one attachment or inline part of a message. The content is streamed separately.
 *
 * @param partPath
 *            the path of the MIME part inside the message, used to stream the content
 * @param fileName
 *            the decoded file name, may be empty for inline parts
 * @param contentType
 *            the base content type, for example <code>application/pdf</code>
 * @param size
 *            the encoded size reported by the server, <code>-1</code> if unknown
 * @param inline
 *            <code>true</code> if the part is referenced from the HTML body
 * @param contentId
 *            the Content-ID without angle brackets, <code>null</code> if the part has none
 */
public record MailAttachment(String partPath, String fileName, String contentType, long size, boolean inline,
		String contentId) {
}
