package ch.elexis.core.mail.internal.mailbox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import ch.elexis.core.mail.html.MailHtmlSanitizer;
import ch.elexis.core.mail.internal.send.MailMessageBuilder;
import ch.elexis.core.mail.internal.send.TooLargeException;
import ch.elexis.core.mail.model.MailAttachment;
import ch.elexis.core.mail.service.MailNotFoundException;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.MimePart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.internet.ParseException;

public final class MailMimeReader {
	private static final int MAX_DEPTH = 20;
	private static final int MAX_PARTS = 500;
	private static final int MAX_TEXT_BYTES = 5_000_000;
	private final Map<String, Part> attachments = new LinkedHashMap<>();
	private int count;

	record Body(Part html, Part plain) {
	}

	public record Display(String html, String plain, List<MailAttachment> attachments) {
	}

	/**
	 * Reads display text and attachment metadata, sanitizing HTML and falling back to plain text when needed.
	 * @param message the MIME message to scan
	 * @return the display body and attachment metadata
	 * @throws MessagingException if MIME data is invalid, exceeds scan limits or cannot be read
	 * @throws IOException if message content cannot be read
	 */
	public Display read(Part message) throws MessagingException, IOException {
		Body body = scan(message, "", 1);
		List<MailAttachment> metadata = metadata();
		String html = null;
		String plain = null;
		if (body.html() != null) {
			String source = readText(body.html());
			html = new MailHtmlSanitizer().sanitize(source, id -> metadata.stream()
					.filter(part -> id.equals(part.contentId())).map(part -> "mail-part:" + part.partPath())
					.findFirst().orElse(null)).filter(value -> !value.isBlank()).orElse(null);
			if (html == null) {
				plain = body.plain() == null ? Jsoup.parse(source).text() : readText(body.plain());
				if (plain.isBlank()) {
					plain = "This message has no displayable text.";
				}
			}
		} else if (body.plain() != null) {
			plain = readText(body.plain());
		}
		return new Display(html, plain, metadata);
	}

	/**
	 * Reads draft bodies and embeds supported inline CID images as data URLs, excluding those images from the attachment list.
	 * @param message the draft MIME message to scan
	 * @return the draft bodies and remaining attachment metadata
	 * @throws MessagingException if MIME data is invalid, exceeds scan limits or cannot be read
	 * @throws IOException if message content cannot be read or an embedded image exceeds the size limit
	 */
	public Display readDraft(Part message) throws MessagingException, IOException {
		Body body = scan(message, "", 1);
		String plain = body.plain() == null ? "" : readText(body.plain());
		if (body.html() == null) {
			return new Display(null, plain, metadata());
		}
		Document document = Jsoup.parseBodyFragment(readText(body.html()));
		document.outputSettings().prettyPrint(false);
		Set<String> embedded = new HashSet<>();
		List<MailAttachment> metadata = metadata();
		for (Element image : document.select("img[src]")) {
			String source = image.attr("src");
			if (!source.regionMatches(true, 0, "cid:", 0, 4)) {
				continue;
			}
			for (MailAttachment attachment : metadata) {
				if (!attachment.inline() || !source.substring(4).equals(attachment.contentId())
						|| !attachment.contentType().matches("image/(png|jpeg|gif|webp)")) {
					continue;
				}
				try (InputStream input = attachments.get(attachment.partPath()).getInputStream()) {
					byte[] bytes = input.readNBytes((int) MailMessageBuilder.MAX_MESSAGE_BYTES + 1);
					if (bytes.length > MailMessageBuilder.MAX_MESSAGE_BYTES) {
						throw new TooLargeException();
					}
					image.attr("src", "data:" + attachment.contentType() + ";base64,"
							+ Base64.getEncoder().encodeToString(bytes));
				}
				embedded.add(attachment.partPath());
				break;
			}
		}
		return new Display(document.body().html(), plain,
				metadata.stream().filter(part -> !embedded.contains(part.partPath())).toList());
	}

	Part attachment(Part message, String path) throws MessagingException, IOException {
		scan(message, "", 1);
		Part part = attachments.get(path);
		if (part == null) {
			throw new MailNotFoundException("Mail part does not exist");
		}
		return part;
	}

	boolean hasAttachments(Part part) throws MessagingException, IOException {
		if (++count > MAX_PARTS) {
			return true;
		}
		return hasAttachments(part, 1);
	}

	private boolean hasAttachments(Part part, int depth) throws MessagingException, IOException {
		if (isAttachment(part)) {
			return true;
		}
		if (part.isMimeType("multipart/*")) {
			if (depth >= MAX_DEPTH) {
				return true;
			}
			Multipart multipart = multipart(part);
			boolean related = part.isMimeType("multipart/related");
			for (int i = 0; i < multipart.getCount(); i++) {
				Part child = multipart.getBodyPart(i);
				if (++count > MAX_PARTS || (!(related && embedded(child)) && hasAttachments(child, depth + 1))) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean embedded(Part part) throws MessagingException {
		return part instanceof MimePart mime && mime.getContentID() != null
				&& !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition());
	}

	private Body scan(Part part, String path, int depth) throws MessagingException, IOException {
		if (depth > MAX_DEPTH || ++count > MAX_PARTS) {
			throw new MessagingException("Message exceeds MIME depth or part limit");
		}
		if (isAttachment(part)) {
			attachments.put(path.isEmpty() ? "1" : path, part);
			return new Body(null, null);
		}
		if (part.isMimeType("multipart/*")) {
			Multipart multipart;
			try {
				multipart = multipart(part);
				multipart.getCount();
			} catch (ParseException e) {
				return new Body(null, part);
			}
			Body selected = new Body(null, null);
			boolean alternative = part.isMimeType("multipart/alternative");
			for (int i = 0; i < multipart.getCount(); i++) {
				String childPath = path.isEmpty() ? Integer.toString(i + 1) : path + "." + (i + 1);
				Body child = scan(multipart.getBodyPart(i), childPath, depth + 1);
				if (alternative) {
					selected = new Body(child.html() == null ? selected.html() : child.html(),
							child.plain() == null ? selected.plain() : child.plain());
				} else if (selected.html() == null && selected.plain() == null) {
					selected = child;
				}
			}
			return selected;
		}
		return new Body(part.isMimeType("text/html") ? part : null, part.isMimeType("text/plain") ? part : null);
	}

	private Multipart multipart(Part part) throws MessagingException, IOException {
		Object content = part.getContent();
		if (content instanceof Multipart multipart) {
			return multipart;
		}
		throw new ParseException("Invalid multipart message structure");
	}

	private boolean isAttachment(Part part) throws MessagingException {
		return Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()) || part.getFileName() != null
				|| part.isMimeType("message/rfc822")
				|| (!part.isMimeType("multipart/*") && !part.isMimeType("text/plain")
						&& !part.isMimeType("text/html"));
	}

	private List<MailAttachment> metadata() throws MessagingException, IOException {
		List<MailAttachment> result = new ArrayList<>();
		for (Map.Entry<String, Part> entry : attachments.entrySet()) {
			result.add(metadata(entry.getValue(), entry.getKey()));
		}
		return List.copyOf(result);
	}

	MailAttachment metadata(Part part, String path) throws MessagingException, IOException {
		String name = part.getFileName();
		String contentId = part instanceof MimePart mime ? mime.getContentID() : null;
		if (contentId != null) {
			contentId = contentId.trim();
			if (contentId.startsWith("<") && contentId.endsWith(">")) {
				contentId = contentId.substring(1, contentId.length() - 1);
			}
		}
		boolean inline = !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition())
				&& (contentId != null || Part.INLINE.equalsIgnoreCase(part.getDisposition()));
		return new MailAttachment(path, name == null ? "" : MimeUtility.decodeText(name).replaceAll("\\p{Cf}", ""),
				new ContentType(part.getContentType()).getBaseType().toLowerCase(Locale.ROOT), part.getSize(), inline,
				contentId);
	}

	private String readText(Part part) throws MessagingException, IOException {
		ContentType type = new ContentType(part.getContentType());
		String charsetName = type.getParameter("charset");
		Charset charset;
		try {
			charset = charsetName == null ? StandardCharsets.US_ASCII
					: Charset.forName(MimeUtility.javaCharset(charsetName));
		} catch (UnsupportedCharsetException | IllegalCharsetNameException e) {
			charset = null;
		}
		try (InputStream input = part.getInputStream(); ByteArrayOutputStream bytes = new ByteArrayOutputStream()) {
			byte[] buffer = new byte[8192];
			int read;
			while ((read = input.read(buffer, 0, Math.min(buffer.length, MAX_TEXT_BYTES - bytes.size() + 1))) != -1) {
				if (bytes.size() + read > MAX_TEXT_BYTES) {
					throw new MessagingException("Message text exceeds display size limit");
				}
				bytes.write(buffer, 0, read);
			}
			return charset == null ? undeclared(bytes.toByteArray()) : bytes.toString(charset);
		}
	}

	private static String undeclared(byte[] bytes) {
		try {
			return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
		} catch (CharacterCodingException e) {
			return new String(bytes, Charset.forName("windows-1252"));
		}
	}
}
