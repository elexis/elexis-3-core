package ch.elexis.core.mail.model;

/**
 * An attachment of a message being written. The content is read when the message is built.
 *
 * @param id
 *            the opaque id the page uses to address the attachment
 * @param fileName
 *            the file name shown and sent
 * @param contentType
 *            the base content type
 * @param size
 *            the size in bytes, <code>-1</code> if unknown
 * @param source
 *            where the content comes from
 * @param path
 *            the absolute local path for {@link MailAttachmentSource#FILE}, otherwise <code>null</code>
 * @param originalRef
 *            the message for {@link MailAttachmentSource#ORIGINAL_PART} and
 *            {@link MailAttachmentSource#ORIGINAL_MESSAGE}, otherwise <code>null</code>
 * @param partPath
 *            the part path for ORIGINAL_PART or FILED_PART, otherwise <code>null</code>
 * @param documentId
 *            the server-only document identity for FILED_PART, otherwise null
 */
public record MailOutgoingAttachment(String id, String fileName, String contentType, long size,
		MailAttachmentSource source, String path, MailRef originalRef, String partPath, String documentId) {

	/**
	 * Creates an attachment from a file or mailbox source.
	 * @param id opaque attachment identity
	 * @param fileName display file name
	 * @param contentType MIME content type
	 * @param size decoded size, or -1
	 * @param source content source
	 * @param path local path, or null
	 * @param originalRef mailbox reference, or null
	 * @param partPath MIME part path, or null
	 */
	public MailOutgoingAttachment(String id, String fileName, String contentType,
			long size, MailAttachmentSource source, String path, MailRef originalRef, String partPath) {
		this(id, fileName, contentType, size, source, path, originalRef, partPath, null);
	}
}
