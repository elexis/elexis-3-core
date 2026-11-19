package ch.elexis.core.mail.internal.mailbox;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;

import jakarta.mail.Address;
import jakarta.mail.FetchProfile;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.UIDFolder;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.eclipse.angus.mail.imap.IMAPFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.MailLog;
import ch.elexis.core.mail.model.MailAddress;
import ch.elexis.core.mail.model.MailFlag;
import ch.elexis.core.mail.model.MailHeader;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.service.MailNotFoundException;

/** Maps mailbox headers and provides the shared business identity for sent messages. */
public class MailHeaderMapper {
	private static final Logger log = LoggerFactory.getLogger(MailHeaderMapper.class);

	static Message message(IMAPFolder folder, MailRef ref) throws MessagingException {
		if (folder.getUIDValidity() != ref.uidValidity()) {
			throw new MailNotFoundException("Mail folder UID validity has changed");
		}
		Message message = folder.getMessageByUID(ref.uid());
		if (message == null || message.isExpunged()) {
			throw new MailNotFoundException("Mail message no longer exists");
		}
		return message;
	}

	/**
	 * Creates the fetch profile for list headers.
	 * @return the fetch profile
	 */
	public static FetchProfile fetchProfile() {
		FetchProfile profile = new FetchProfile();
		profile.add(FetchProfile.Item.ENVELOPE);
		profile.add(FetchProfile.Item.FLAGS);
		profile.add(UIDFolder.FetchProfileItem.UID);
		profile.add("Message-ID");
		profile.add("Date");
		profile.add("From");
		profile.add("Subject");
		profile.add("Content-Type");
		return profile;
	}

	/**
	 * Maps a fetched message to a list header.
	 * @param message the fetched message
	 * @param ref the message reference
	 * @param exact whether attachments are detected from the full structure
	 * @return the header
	 * @throws MessagingException if the message cannot be read
	 */
	public static MailHeader header(Message message, MailRef ref, boolean exact) throws MessagingException {
		List<MailAddress> from = addresses(message.getFrom());
		MailAddress sender = from.isEmpty() ? new MailAddress("", "") : from.getFirst();
		Date sent = message.getSentDate();
		Date received = message.getReceivedDate();
		boolean attachments;
		try {
			attachments = exact ? new MailMimeReader().hasAttachments(message)
					: joinedHeader(message, "Content-Type").split(";", 2)[0].trim().equalsIgnoreCase("multipart/mixed");
		} catch (MessagingException | IOException e) {
			log.debug("Mail attachment detection failed: {}", MailLog.causes(e));
			attachments = false;
		}
		return new MailHeader(ref, messageKey(message), sender.name(), sender.address(), message.getSubject(),
				sent == null ? null : sent.toInstant(), received == null ? null : received.toInstant(),
				flags(message.getFlags()), attachments);
	}

	static Message readable(Message message) throws MessagingException {
		try {
			message.getContentType();
			return message;
		} catch (MessagingException e) {
			return new MimeMessage((MimeMessage) message);
		}
	}

	/**
	 * Normalizes the Message-ID or hashes the fallback identity headers.
	 * @param message the received or sent message
	 * @return the stable business identity
	 * @throws MessagingException if headers cannot be read
	 */
	public static String messageKey(Message message) throws MessagingException {
		String id = joinedHeader(message, "Message-ID").replaceAll("(?U)\\s", "");
		if (!id.isEmpty() && id.length() <= 200) {
			return id;
		}
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			for (String name : new String[] { "Date", "From", "Subject" }) {
				digest.update(joinedHeader(message, name).getBytes(StandardCharsets.UTF_8));
				digest.update((byte) 0);
			}
			return "sha256:" + HexFormat.of().formatHex(digest.digest());
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is unavailable", e);
		}
	}

	private static String joinedHeader(Message message, String name) throws MessagingException {
		String[] values = message.getHeader(name);
		return values == null ? "" : String.join("\n", values);
	}

	static Set<MailFlag> flags(Flags flags) {
		Set<MailFlag> result = EnumSet.noneOf(MailFlag.class);
		if (flags.contains(Flags.Flag.SEEN)) {
			result.add(MailFlag.SEEN);
		}
		if (flags.contains(Flags.Flag.ANSWERED)) {
			result.add(MailFlag.ANSWERED);
		}
		if (flags.contains(Flags.Flag.FLAGGED)) {
			result.add(MailFlag.FLAGGED);
		}
		if (flags.contains(Flags.Flag.DRAFT)) {
			result.add(MailFlag.DRAFT);
		}
		if (flags.contains(Flags.Flag.DELETED)) {
			result.add(MailFlag.DELETED);
		}
		return Set.copyOf(result);
	}

	static List<MailAddress> addresses(Address[] addresses) {
		if (addresses == null) {
			return List.of();
		}
		List<MailAddress> result = new ArrayList<>();
		for (Address address : addresses) {
			if (address instanceof InternetAddress internet) {
				result.add(new MailAddress(internet.getPersonal() == null ? "" : internet.getPersonal(),
						internet.getAddress()));
			} else {
				result.add(new MailAddress("", address.toString()));
			}
		}
		return List.copyOf(result);
	}
}
