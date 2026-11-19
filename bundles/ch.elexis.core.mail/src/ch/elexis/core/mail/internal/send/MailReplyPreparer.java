package ch.elexis.core.mail.internal.send;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.Strings;
import org.eclipse.osgi.util.NLS;
import org.jsoup.nodes.Entities;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.html.OutgoingHtmlFilter;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.internal.mailbox.MailMimeReader;
import ch.elexis.core.mail.messages.Messages;
import ch.elexis.core.mail.model.ComposeMode;
import ch.elexis.core.mail.model.MailAddress;
import ch.elexis.core.mail.model.MailAttachment;
import ch.elexis.core.mail.model.MailAttachmentSource;
import ch.elexis.core.mail.model.MailDraft;
import ch.elexis.core.mail.model.MailFolderRole;
import ch.elexis.core.mail.model.MailOutgoingAttachment;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.MailAccessException;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

final class MailReplyPreparer {
	private final MailSendService service;

	MailReplyPreparer(MailSendService service) {
		this.service = service;
	}

	/** {@inheritDoc} */
	MailDraft prepare(IUser user, ComposeMode mode, String accountId, MailRef original)
			throws MessagingException {
		List<MailboxAccount> visible = service.getSendAccounts(user);
		if (original != null) {
			service.visibleAccount(user, original.accountId());
		}
		String selected = mode == ComposeMode.DRAFT && original != null ? original.accountId()
				: accountId != null ? accountId : original != null ? original.accountId()
				: visible.isEmpty() ? null : visible.getFirst().id();
		if (accountId == null && original != null && mode != ComposeMode.DRAFT
				&& visible.stream().noneMatch(candidate -> candidate.id().equals(original.accountId()))) {
			selected = visible.isEmpty() ? null : visible.getFirst().id();
		}
		MailboxAccount account = service.visibleAccount(user, selected);
		if (account.smtp() == null) {
			throw new MailAccessException("SMTP account required");
		}
		String domain = account.emailAddress().substring(account.emailAddress().lastIndexOf('@') + 1);
		String messageId = "<" + UUID.randomUUID() + "@" + domain + ">";
		MailDraft draft = new MailDraft(UUID.randomUUID().toString(), account.id(), List.of(), List.of(), List.of(),
				"", "", List.of(), messageId, null, List.of(), null, null);
		if (mode == ComposeMode.NEW) {
			return draft;
		}
		if (original == null) {
			throw new IllegalArgumentException("An original message is required");
		}
		MailboxAccount source = service.visibleAccount(user, original.accountId());
		if (mode == ComposeMode.DRAFT && !original.folder().equals(service.sentCopies.roleFolder(user, source, MailFolderRole.DRAFTS))) {
			throw new MessagingException("Message is not in the drafts folder");
		}
		return service.connections.execute(source, store -> {
			service.visibleAccount(user, source.id());
			try (OpenFolder opened = new OpenFolder(store, original.folder())) {
				MimeMessage message = (MimeMessage) service.message(opened.folder(), original);
				return mode == ComposeMode.DRAFT ? prepareDraft(draft, original, message)
						: prepareOriginal(draft, account, mode, original, null, message);
			} catch (IOException e) {
				throw new MessagingException("Could not prepare the original message", e);
			}
		});
	}

	MailDraft prepareFiled(IUser user, ComposeMode mode, String documentId, MimeMessage message)
			throws MessagingException, IOException {
		if (mode != ComposeMode.REPLY && mode != ComposeMode.REPLY_ALL && mode != ComposeMode.FORWARD) {
			throw new MessagingException("Unsupported filed compose mode");
		}
		List<MailboxAccount> visible = service.getSendAccounts(user);
		List<MailAddress> from = addresses(message.getFrom());
		MailboxAccount selected = matching(visible, from);
		if (selected == null) {
			List<MailAddress> recipients = new ArrayList<>(addresses(message.getRecipients(Message.RecipientType.TO)));
			recipients.addAll(addresses(message.getRecipients(Message.RecipientType.CC)));
			selected = matching(visible, recipients);
		}
		MailDraft draft = prepare(user, ComposeMode.NEW, selected == null ? null : selected.id(), null);
		MailboxAccount account = service.visibleAccount(user, draft.accountId());
		return prepareOriginal(draft, account, mode, null, documentId, message);
	}

	private MailboxAccount matching(List<MailboxAccount> accounts, List<MailAddress> addresses) {
		return accounts.stream().filter(account -> addresses.stream()
				.anyMatch(address -> Strings.CI.equals(account.emailAddress(), address.address()))).findFirst().orElse(null);
	}

	private MailDraft prepareDraft(MailDraft draft, MailRef original, MimeMessage message)
			throws MessagingException, IOException {
		MailMimeReader.Display content = new MailMimeReader().readDraft(message);
		String html = content.html() == null ? escape(content.plain()).replace("\r\n", "\n")
				.replace('\r', '\n').replace("\n", "<br>") : content.html();
		List<MailOutgoingAttachment> attachments = content.attachments().stream()
				.map(part -> new MailOutgoingAttachment(UUID.randomUUID().toString(), part.fileName(),
						part.contentType(), part.size(), MailAttachmentSource.ORIGINAL_PART, null, original,
						part.partPath())).toList();
		String references = message.getHeader("References", " ");
		return new MailDraft(draft.sessionId(), original.accountId(),
				addresses(message.getRecipients(Message.RecipientType.TO)),
				addresses(message.getRecipients(Message.RecipientType.CC)),
				addresses(message.getRecipients(Message.RecipientType.BCC)), service.value(message.getSubject()),
				new OutgoingHtmlFilter().filter(html), attachments,
				message.getMessageID() == null ? draft.messageId() : message.getMessageID(),
				message.getHeader("In-Reply-To", " "), references == null || references.isBlank() ? List.of()
						: List.of(references.trim().split("\\s+")), null, original);
	}

	private MailDraft prepareOriginal(MailDraft draft, MailboxAccount account, ComposeMode mode, MailRef original,
			String documentId, MimeMessage message) throws MessagingException, IOException {
		boolean reply = mode == ComposeMode.REPLY || mode == ComposeMode.REPLY_ALL;
		String subject = message.getSubject() == null ? "" : message.getSubject();
		if (!MailSendService.SUBJECT_PREFIX.matcher(subject.stripLeading()).find()) {
			subject = (reply ? Messages.ReplyPrefix : Messages.ForwardPrefix) + " " + subject;
		}
		List<MailOutgoingAttachment> attachments = new ArrayList<>();
		List<MailAddress> to = new ArrayList<>();
		List<MailAddress> cc = new ArrayList<>();
		String html = "";
		String inReplyTo = reply ? message.getMessageID() : null;
		List<String> references = new ArrayList<>();
		if (reply) {
			String previous = message.getHeader("References", " ");
			if (previous != null && !previous.isBlank()) {
				references.addAll(Arrays.asList(previous.trim().split("\\s+")));
			}
			if (inReplyTo != null && !references.contains(inReplyTo)) {
				references.add(inReplyTo);
			}
			to.addAll(addresses(message.getReplyTo()));
			if (to.isEmpty()) {
				to.addAll(addresses(message.getFrom()));
			}
			if (mode == ComposeMode.REPLY_ALL) {
				to.addAll(addresses(message.getRecipients(Message.RecipientType.TO)));
				cc.addAll(addresses(message.getRecipients(Message.RecipientType.CC)));
				Map<String, MailAddress> unique = new LinkedHashMap<>();
				for (MailAddress address : to) {
					if (!Strings.CI.equals(address.address(), account.emailAddress())) {
						unique.putIfAbsent(address.address().toLowerCase(Locale.ROOT), address);
					}
				}
				to = new ArrayList<>(unique.values());
				List<MailAddress> copies = new ArrayList<>();
				for (MailAddress address : cc) {
					if (!Strings.CI.equals(address.address(), account.emailAddress())
							&& unique.putIfAbsent(address.address().toLowerCase(Locale.ROOT), address) == null) {
						copies.add(address);
					}
				}
				cc = copies;
			}
		}
		if (mode == ComposeMode.FORWARD_AS_ATTACHMENT) {
			String name = (message.getSubject() == null ? "message" : message.getSubject())
					.replaceAll("[\\p{Cntrl}<>:\"/\\\\|?*]", "_").strip();
			name = name.replaceAll("[. ]+$", "");
			if (name.isBlank() || name.matches("(?i)(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])(?:\\..*)?")) {
				name = "message";
			}
			name = name.substring(0, Math.min(name.length(), 120)) + ".eml";
			attachments.add(new MailOutgoingAttachment(UUID.randomUUID().toString(), name, "message/rfc822",
					message.getSize(), MailAttachmentSource.ORIGINAL_MESSAGE, null, original, null));
		} else {
			MailMimeReader.Display display = new MailMimeReader().read(message);
			String body = display.html() == null ? escape(display.plain() == null ? "" : display.plain())
					.replace("\r\n", "\n").replace('\r', '\n').replace("\n", "<br>") : display.html();
			body = new OutgoingHtmlFilter().filter(body);
			String sender = InternetAddress.toString(message.getFrom());
			Date sent = message.getSentDate();
			String date = sent == null ? "" : DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
					.withLocale(Locale.getDefault()).withZone(ZoneId.systemDefault()).format(sent.toInstant());
			if (reply) {
				html = "<p></p><p>" + escape(NLS.bind(Messages.QuoteHeader, date, sender == null ? "" : sender))
						+ "</p><blockquote>" + body + "</blockquote>";
			} else {
				String recipients = InternetAddress.toString(message.getRecipients(Message.RecipientType.TO));
				html = "<p></p><p>" + escape(Messages.From) + ": " + escape(sender == null ? "" : sender)
						+ "<br>" + escape(Messages.Date) + ": " + escape(date) + "<br>" + escape(Messages.To) + ": "
						+ escape(recipients == null ? "" : recipients) + "<br>" + escape(Messages.Subject) + ": "
						+ escape(message.getSubject() == null ? "" : message.getSubject()) + "</p><blockquote>"
						+ body + "</blockquote>";
				for (MailAttachment attachment : display.attachments()) {
					if (!attachment.inline()) {
						attachments.add(new MailOutgoingAttachment(UUID.randomUUID().toString(), attachment.fileName(),
								attachment.contentType(), attachment.size(), documentId == null
										? MailAttachmentSource.ORIGINAL_PART : MailAttachmentSource.FILED_PART, null,
								original, attachment.partPath(), documentId));
					}
				}
			}
		}
		return new MailDraft(draft.sessionId(), draft.accountId(), List.copyOf(to), List.copyOf(cc), List.of(), subject,
				html, List.copyOf(attachments), draft.messageId(), inReplyTo, List.copyOf(references),
				reply ? original : null, null);
	}

	private List<MailAddress> addresses(Address[] values) {
		if (values == null) {
			return List.of();
		}
		List<MailAddress> result = new ArrayList<>();
		for (Address address : values) {
			if (address instanceof InternetAddress internet) {
				result.add(new MailAddress(service.value(internet.getPersonal()), internet.getAddress()));
			}
		}
		return result;
	}

	private String escape(String text) {
		return Entities.escape(text);
	}
}
