package ch.elexis.core.mail.internal.mailbox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import ch.elexis.core.model.IDocument;
import ch.elexis.core.mail.model.MailAttachment;
import ch.elexis.core.mail.model.MailMessageView;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.service.MailFiledTooLargeException;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

/** Uses the mailbox MIME implementation for bounded in-memory document messages. */
public final class MailFiledMimeReader {
	private static final int MAX_BYTES = 30 * 1024 * 1024;

	private MailFiledMimeReader() {
	}

	/**
	 * Parses a document stream with a strict byte limit, closing it after parsing.
	 * @param document readable document
	 * @return parsed message
	 * @throws MessagingException if content is missing, invalid or too large
	 * @throws IOException if content cannot be streamed
	 */
	public static MimeMessage parse(IDocument document) throws MessagingException, IOException {
		if (document.getContentLength() > MAX_BYTES) {
			throw new MailFiledTooLargeException();
		}
		try (InputStream content = document.getContent()) {
			if (content == null) {
				throw new MessagingException("Filed message content is unavailable");
			}
			byte[] bytes = content.readNBytes(MAX_BYTES + 1);
			if (bytes.length > MAX_BYTES) {
				throw new MailFiledTooLargeException();
			}
			MimeMessage message = new MimeMessage(Session.getInstance(new Properties()),
					new ByteArrayInputStream(bytes));
			if (!message.getAllHeaders().hasMoreElements()
					|| message.getHeader("From") == null && message.getHeader("Subject") == null
							&& message.getHeader("Date") == null && message.getHeader("Message-ID") == null) {
				throw new MessagingException("Document is not a mail message");
			}
			return message;
		}
	}

	/**
	 * Prepares the same sanitized display as a mailbox message.
	 * @param message parsed source
	 * @return display with an inert reference that contains no document ID
	 * @throws MessagingException if MIME headers are invalid
	 * @throws IOException if MIME content cannot be decoded
	 */
	public static MailMessageView read(MimeMessage message) throws MessagingException, IOException {
		MailMimeReader.Display display = new MailMimeReader().read(message);
		return new MailMessageView(MailHeaderMapper.header(message, new MailRef("", "", 0, 0), true),
				MailHeaderMapper.addresses(message.getRecipients(Message.RecipientType.TO)),
				MailHeaderMapper.addresses(message.getRecipients(Message.RecipientType.CC)),
				display.html(), display.plain(), display.attachments());
	}

	/**
	 * Writes one decoded part using the mailbox MIME path rules.
	 * @param message parsed message
	 * @param path requested path
	 * @param target destination, not closed
	 * @return exact metadata
	 * @throws MessagingException if the path is unknown
	 * @throws IOException if decoding or writing fails
	 */
	public static MailAttachment write(MimeMessage message, String path, OutputStream target)
			throws MessagingException, IOException {
		MailMimeReader reader = new MailMimeReader();
		Part part = reader.attachment(message, path);
		MailAttachment metadata = reader.metadata(part, path);
		try (InputStream content = part.getInputStream()) {
			content.transferTo(target);
		}
		return metadata;
	}
}
