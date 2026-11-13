package ch.elexis.core.mail.internal.send;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ch.elexis.core.mail.html.OutgoingHtmlFilter;
import ch.elexis.core.mail.model.MailAddress;
import ch.elexis.core.mail.model.MailAttachmentSource;
import ch.elexis.core.mail.model.MailDraft;
import ch.elexis.core.mail.model.MailOutgoingAttachment;
import ch.elexis.core.mail.model.MailboxAccount;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;

public final class MailMessageBuilder {

	public static final long MAX_MESSAGE_BYTES = 25L * 1024 * 1024;
	private static final Pattern DATA_IMAGE = Pattern.compile("data:(image/(png|jpeg|gif|webp));base64,",
			Pattern.CASE_INSENSITIVE);
	private final OriginalContent originals;

	@FunctionalInterface
	interface OriginalContent {
		byte[] read(MailOutgoingAttachment attachment) throws MessagingException, IOException;
	}

	MailMessageBuilder(OriginalContent originals) {
		this.originals = originals;
	}

	MimeMessage build(Session session, MailboxAccount account, MailDraft draft) throws MessagingException, IOException {
		OutgoingHtmlFilter filter = new OutgoingHtmlFilter();
		String html = filter.filter(draft.html());
		long total = html.getBytes(StandardCharsets.UTF_8).length;
		for (MailOutgoingAttachment attachment : draft.attachments()) {
			long size = attachment.source() == MailAttachmentSource.FILE ? Files.size(Path.of(attachment.path()))
					: Math.max(0, attachment.size());
			if (size > MAX_MESSAGE_BYTES - total) {
				throw new TooLargeException();
			}
			total += size;
		}
		MimeMessage message = new MimeMessage(session) {
			@Override
			protected void updateMessageID() throws MessagingException {
				setHeader("Message-ID", draft.messageId());
			}
		};
		message.setFrom(new InternetAddress(account.emailAddress(), account.displayName(), StandardCharsets.UTF_8.name()));
		message.setRecipients(Message.RecipientType.TO, addresses(draft.to()));
		message.setRecipients(Message.RecipientType.CC, addresses(draft.cc()));
		message.setRecipients(Message.RecipientType.BCC, addresses(draft.bcc()));
		message.setSubject(draft.subject(), StandardCharsets.UTF_8.name());
		message.setSentDate(new Date());
		if (draft.inReplyTo() != null) {
			message.setHeader("In-Reply-To", draft.inReplyTo());
		}
		if (!draft.references().isEmpty()) {
			message.setHeader("References", String.join(" ", draft.references()));
		}
		MimeMultipart alternative = new MimeMultipart("alternative");
		MimeBodyPart plain = new MimeBodyPart();
		plain.setText(filter.toPlainText(html), StandardCharsets.UTF_8.name());
		alternative.addBodyPart(plain);
		alternative.addBodyPart(rich(html, account));
		if (draft.attachments().isEmpty()) {
			message.setContent(alternative);
		} else {
			MimeMultipart mixed = new MimeMultipart("mixed");
			MimeBodyPart body = new MimeBodyPart();
			body.setContent(alternative);
			mixed.addBodyPart(body);
			for (MailOutgoingAttachment attachment : draft.attachments()) {
				MimeBodyPart part = new MimeBodyPart();
				if (attachment.source() == MailAttachmentSource.FILE) {
					FileDataSource source = new FileDataSource(attachment.path());
					source.setFileTypeMap(new FixedFileTypeMap(attachment.contentType()));
					part.setDataHandler(new DataHandler(source));
				} else {
					byte[] content = originals.read(attachment);
					total -= Math.max(0, attachment.size());
					if (content.length > MAX_MESSAGE_BYTES - total) {
						throw new TooLargeException();
					}
					total += content.length;
					part.setDataHandler(new DataHandler(new ByteArrayDataSource(content,
							attachment.source() == MailAttachmentSource.ORIGINAL_MESSAGE ? "message/rfc822"
									: attachment.contentType())));
				}
				part.setFileName(attachment.fileName());
				part.setDisposition(MimeBodyPart.ATTACHMENT);
				mixed.addBodyPart(part);
			}
			message.setContent(mixed);
		}
		message.saveChanges();
		message.writeTo(new SizeLimitOutputStream(OutputStream.nullOutputStream()));
		return message;
	}

	private MimeBodyPart rich(String html, MailboxAccount account) throws MessagingException {
		Document document = Jsoup.parseBodyFragment(html);
		document.outputSettings().prettyPrint(false);
		String domain = account.emailAddress().substring(account.emailAddress().lastIndexOf('@') + 1);
		Map<String, String> ids = new LinkedHashMap<>();
		List<MimeBodyPart> images = new ArrayList<>();
		for (Element image : document.select("img[src]")) {
			String source = image.attr("src").trim();
			Matcher data = DATA_IMAGE.matcher(source);
			if (!data.lookingAt()) {
				continue;
			}
			String id = ids.get(source);
			if (id == null) {
				id = UUID.randomUUID() + "@" + domain;
				ids.put(source, id);
				String type = data.group(1).toLowerCase(Locale.ROOT);
				MimeBodyPart part = new MimeBodyPart();
				part.setDataHandler(new DataHandler(new ByteArrayDataSource(
						Base64.getMimeDecoder().decode(source.substring(data.end())), type)));
				part.setContentID("<" + id + ">");
				part.setDisposition(MimeBodyPart.INLINE);
				part.setFileName("image" + ids.size() + "." + data.group(2).toLowerCase(Locale.ROOT));
				images.add(part);
			}
			image.attr("src", "cid:" + id);
		}
		MimeBodyPart rich = new MimeBodyPart();
		if (images.isEmpty()) {
			rich.setText(html, StandardCharsets.UTF_8.name(), "html");
			return rich;
		}
		MimeMultipart related = new MimeMultipart("related");
		MimeBodyPart page = new MimeBodyPart();
		page.setText(document.body().html(), StandardCharsets.UTF_8.name(), "html");
		related.addBodyPart(page);
		for (MimeBodyPart part : images) {
			related.addBodyPart(part);
		}
		rich.setContent(related);
		return rich;
	}

	private Address[] addresses(List<MailAddress> values) throws MessagingException, IOException {
		Address[] result = new Address[values.size()];
		for (int i = 0; i < result.length; i++) {
			MailAddress value = values.get(i);
			InternetAddress address = new InternetAddress(value.address(), true);
			address.validate();
			address.setPersonal(value.name(), StandardCharsets.UTF_8.name());
			result[i] = address;
		}
		return result;
	}
}
