package ch.elexis.core.mail;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.eclipse.angus.mail.imap.IMAPMessage;
import org.slf4j.LoggerFactory;

import jakarta.mail.BodyPart;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.InternetAddress;

/**
 * A self-sustainable representation of an IMAP message. It does not need a live
 * IMAP connection, as all its properties have been loaded. The {@link #message}
 * object is the original IMAP connection object.
 */
public class IMAPMailMessage {

	private final IMAPMessage message;

	private String sender;
	private String subject;
	private String text;
	private List<Attachment> attachments;
	private Date sentDate;

	public IMAPMailMessage(IMAPMessage message) {
		this.message = message;
		this.attachments = new ArrayList<>();
	}

	public static IMAPMailMessage of(IMAPMessage message) throws MessagingException {
		IMAPMailMessage imapMailMessage = new IMAPMailMessage(message);
		imapMailMessage.extractContent();
		return imapMailMessage;
	}

	private void extractContent() throws MessagingException {
		InternetAddress _sender = (InternetAddress) message.getSender();
		sender = _sender.getAddress();
		sentDate = message.getSentDate();
		subject = message.getSubject();

		String contentType = message.getContentType();

		try {
			Object content = message.getContent();
			if (content instanceof Multipart) {
				extractMultipartContent((Multipart) content);
			} else if (content instanceof BodyPart) {
				extractBodyPartContent((BodyPart) content);
			} else {
				extractOtherContent(contentType, content);
			}

		} catch (IOException e) {
			throw new MessagingException("Error reading attachments", e);
		}
	}

	private void extractOtherContent(String contentType, Object content) {
		if (contentType.toLowerCase().contains("text/plain")) {
			if (content instanceof String) {
				text = (String) content;
			} else if (content instanceof InputStream) {
				try {
					text = IOUtils.toString((InputStream) content, "ISO-8859-1");
				} catch (IOException e) {
					LoggerFactory.getLogger(getClass()).warn("Error extraction other content", e);
				}
			} else {
				LoggerFactory.getLogger(getClass()).warn("Unknown other content [" + content + "]");
			}
		} else {
			LoggerFactory.getLogger(getClass()).warn("Unknown other content type [" + contentType + "]");
		}
	}

	private void extractMultipartContent(Multipart multiPart) throws MessagingException, IOException {
		int numberOfParts = multiPart.getCount();
		for (int partCount = 0; partCount < numberOfParts; partCount++) {
			BodyPart part = multiPart.getBodyPart(partCount);
			if (part.getContentType().contains("multipart")) {
				extractMultipartContent((Multipart) part.getContent());
			} else {
				extractBodyPartContent(part);
			}
		}
	}

	private void extractBodyPartContent(BodyPart part) throws MessagingException, IOException {
		if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())) {
			// this part is attachment
			String fileName = part.getFileName();
			byte[] content = IOUtils.toByteArray(part.getInputStream());
			Attachment attachment = new Attachment(fileName, part.getSize(), content);
			attachments.add(attachment);
		} else {
			extractOtherContent(part.getContentType(), part.getContent());
		}
	}

	public Message toIMAPMessage() {
		return message;
	}

	public String getSubject() {
		return subject;
	}

	public Date getSentDate() {
		return sentDate;
	}

	public String getSender() {
		return sender;
	}

	public String getText() {
		return text;
	}

	public List<Attachment> getAttachments() {
		return attachments;
	}

	public static class Attachment {

		private String filename;
		private int size;
		private byte[] content;

		public Attachment(String filename, int size, byte[] content) {
			this.filename = filename;
			this.size = size;
			this.content = content;
		}

		public String getFilename() {
			return filename;
		}

		public byte[] getContent() {
			return content;
		}

		public int getSize() {
			return size;
		}
	}

}
