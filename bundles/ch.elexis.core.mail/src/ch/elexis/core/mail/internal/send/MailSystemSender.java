package ch.elexis.core.mail.internal.send;

import java.io.File;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.MailMessage;
import ch.elexis.core.mail.internal.account.MailCredentialProvider;
import ch.elexis.core.mail.internal.connection.MailSessionFactory;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.internal.legacy.MailLegacyAccountConverter;
import ch.elexis.core.mail.internal.mailbox.MailFolderRoles;
import ch.elexis.core.mail.model.MailFolderRole;
import ch.elexis.core.mail.model.MailProvider;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.SentCopyDetected;
import ch.elexis.core.mail.model.SentCopyMode;
import ch.elexis.core.services.IConfigService;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.URLName;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.search.HeaderTerm;

/** Sends trusted compatibility messages without requiring an active user. */
public final class MailSystemSender {
	private final Logger log = LoggerFactory.getLogger(getClass());
	private final IConfigService config;
	private final MailCredentialProvider credentials;

	/**
	 * Creates the internal sender; callers enforce shared or local legacy scope.
	 * @param config the installation configuration
	 * @param credentials the core password and OAuth provider
	 */
	public MailSystemSender(IConfigService config, MailCredentialProvider credentials) {
		this.config = config;
		this.credentials = credentials;
	}

	/**
	 * Sends the legacy MIME structure on a mail worker and handles the sent copy.
	 * @param account the resolved shared or local legacy account
	 * @param source the compatibility message
	 * @param supplied the optional local legacy password
	 * @param sessions the core session factory with the account's security policy
	 * @throws MessagingException if SMTP delivery fails
	 */
	public void send(MailboxAccount account, MailMessage source, char[] supplied, MailSessionFactory sessions)
			throws MessagingException {
		Session session = sessions.createSmtpSession(account);
		MimeMessage message = new MimeMessage(session);
		message.addHeader("X-ElexisMail", "ch.elexis.core.mail");
		message.setFrom(MailLegacyAccountConverter.toLegacy(account, java.util.List.of()).getFromAddress());
		message.setSubject(source.getSubject(), "UTF-8");
		message.setRecipients(Message.RecipientType.TO, source.getToAddress());
		message.setRecipients(Message.RecipientType.CC, source.getCcAddress());
		Multipart multipart = new MimeMultipart("related");
		MimeBodyPart body = new MimeBodyPart();
		body.setContent(source.getHtmlText(), "text/html;charset=UTF-8");
		multipart.addBodyPart(body);
		if (source.hasImage()) {
			for (String imageString : source.getImageStrings()) {
				File image = source.getImage(imageString);
				if (image != null) {
					MimeBodyPart part = filePart(image);
					part.setHeader("Content-ID", "<" + source.getImageMimeContentId(imageString) + ">");
					part.setHeader("Content-Location", image.getName());
					part.setDisposition(MimeBodyPart.INLINE);
					multipart.addBodyPart(part);
				}
			}
		}
		if (source.hasAttachments()) {
			for (File file : source.getAttachments()) {
				multipart.addBodyPart(filePart(file));
			}
		}
		message.setContent(multipart);
		message.saveChanges();
		SendSmtpTransport transport = new SendSmtpTransport(session,
				new URLName("smtp", account.smtp().host(), account.smtp().port(), null, null, null),
				account.smtp().security() == MailSecurity.SSL);
		try {
			credentials.connect(account, true, supplied, (username, password) -> transport.connect(
					account.smtp().host(), account.smtp().port(), username, new String(password)));
			transport.sendMessage(message, message.getAllRecipients());
		} finally {
			try {
				transport.close();
			} catch (MessagingException e) {
				log.debug("System mail SMTP cleanup failed: {}", e.getClass().getSimpleName());
			}
		}
		if (account.imap() != null) {
			try {
				sentCopy(account, message, supplied, sessions);
			} catch (MessagingException | RuntimeException e) {
				log.warn("System mail sent-copy failed for account {}: {}", account.id(), e.getClass().getSimpleName());
			}
		}
	}

	private MimeBodyPart filePart(File file) throws MessagingException {
		MimeBodyPart part = new MimeBodyPart();
		part.setDataHandler(new DataHandler(new FileDataSource(file)));
		part.setFileName(file.getName());
		return part;
	}

	private void sentCopy(MailboxAccount account, MimeMessage message, char[] supplied, MailSessionFactory sessions)
			throws MessagingException {
		if (account.sentCopyMode() == SentCopyMode.SERVER
				|| account.sentCopyMode() == SentCopyMode.AUTO && account.provider() == MailProvider.M365) {
			return;
		}
		String key = MailClientConfig.sentCopyDetected(account.id());
		String settings = config.get(MailClientConfig.account(account.id()), null);
		SentCopyDetected detected;
		try {
			detected = SentCopyDetected.valueOf(config.get(key, SentCopyDetected.UNKNOWN.name()));
		} catch (IllegalArgumentException e) {
			detected = SentCopyDetected.UNKNOWN;
		}
		if (account.sentCopyMode() == SentCopyMode.AUTO && detected == SentCopyDetected.SERVER_COPIES) {
			return;
		}
		try (IMAPStore store = sessions.createStore(account)) {
			credentials.connect(account, false, supplied, (username, password) -> store.connect(
					account.imap().host(), account.imap().port(), username, new String(password)));
			Folder[] folders = store.getDefaultFolder().list("*");
			Set<MailFolderRole> attributed = MailFolderRoles.attributedRoles(folders);
			Folder sent = null;
			for (Folder folder : folders) {
				if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0
						&& MailFolderRoles.role((IMAPFolder) folder, attributed) == MailFolderRole.SENT) {
					sent = folder;
					break;
				}
			}
			if (sent == null) {
				throw new MessagingException("Required mail folder is not configured");
			}
			if (account.sentCopyMode() == SentCopyMode.AUTO && detected == SentCopyDetected.UNKNOWN) {
				detected = detect(store, sent.getFullName(), message.getMessageID());
			}
			if (detected != SentCopyDetected.SERVER_COPIES || account.sentCopyMode() == SentCopyMode.CLIENT) {
				message.setFlag(Flags.Flag.SEEN, true);
				sent.appendMessages(new Message[] { message });
			}
			if (account.sentCopyMode() == SentCopyMode.AUTO && detected != SentCopyDetected.UNKNOWN
					&& Objects.equals(settings, config.get(MailClientConfig.account(account.id()), null))) {
				config.set(key, detected.name());
			}
		}
	}

	private SentCopyDetected detect(IMAPStore store, String folder, String id) {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
		try (OpenFolder opened = new OpenFolder(store, folder)) {
			do {
				for (Message match : opened.folder().search(new HeaderTerm("Message-ID", id))) {
					if (id.equals(((MimeMessage) match).getMessageID()) && !match.isSet(Flags.Flag.DELETED)) {
						return SentCopyDetected.SERVER_COPIES;
					}
				}
				long remaining = deadline - System.nanoTime();
				if (remaining <= 0) {
					return SentCopyDetected.NO_SERVER_COPY;
				}
				TimeUnit.NANOSECONDS.sleep(Math.min(remaining, TimeUnit.SECONDS.toNanos(3)));
			} while (true);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.debug("System mail sent-copy detection interrupted");
		} catch (MessagingException | RuntimeException e) {
			log.warn("System mail sent-copy detection failed: {}", e.getClass().getSimpleName());
		}
		return SentCopyDetected.UNKNOWN;
	}
}
