package ch.elexis.core.mail.internal.mailbox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Strings;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.protocol.Status;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.model.MailCleanupCandidates;
import ch.elexis.core.mail.model.MailCleanupPage;
import ch.elexis.core.mail.model.MailHeader;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailTrashStatus;
import ch.elexis.core.mail.model.MailboxAccount;
import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;

/** Implements bounded cleanup operations using the existing account lanes. */
final class MailCleanupMailbox {
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH);
	private final MailboxService mailbox;

	MailCleanupMailbox(MailboxService mailbox) {
		this.mailbox = mailbox;
	}

	MailCleanupCandidates search(IUser user, String accountId, String name, LocalDate before, long upper)
			throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		return mailbox.connections.executeReader(account, store -> {
			try (OpenFolder opened = new OpenFolder(store, name)) {
				IMAPFolder folder = opened.folder();
				String criteria = "UID 1:" + (upper > 0 ? Long.toString(upper) : "*") + " NOT FLAGGED"
						+ (before == null ? "" : " BEFORE " + DATE.format(before));
				List<Long> uids = Arrays.stream(MailUidCommands.searchUids(folder, criteria))
						.filter(uid -> upper == 0 || uid <= upper).sorted().boxed().toList();
				return new MailCleanupCandidates(folder.getUIDValidity(), uids);
			}
		});
	}

	MailCleanupPage fetch(IUser user, String accountId, String name, long uidValidity, List<Long> uids)
			throws MessagingException {
		if (uids.isEmpty() || uids.size() > 50) {
			throw new IllegalArgumentException("Cleanup page must contain one to 50 UIDs");
		}
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		return mailbox.connections.executeReader(account, store -> {
			try (OpenFolder opened = new OpenFolder(store, name)) {
				IMAPFolder folder = opened.folder();
				long validity = folder.getUIDValidity();
				if (validity != uidValidity) {
					return new MailCleanupPage(validity, List.of());
				}
				Message[] messages = Arrays.stream(folder.getMessagesByUID(uids.stream().mapToLong(Long::longValue)
						.toArray())).filter(value -> value != null).toArray(Message[]::new);
				folder.fetch(messages, MailHeaderMapper.fetchProfile());
				List<MailHeader> headers = new ArrayList<>();
				for (Message message : messages) {
					if (!message.isExpunged() && !message.isSet(Flags.Flag.FLAGGED)) {
						MailRef ref = new MailRef(accountId, name, validity, folder.getUID(message));
						headers.add(MailHeaderMapper.header(message, ref, false));
					}
				}
				return new MailCleanupPage(validity, List.copyOf(headers));
			}
		});
	}

	String archive(IUser user, String accountId, String configured) throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		return mailbox.connections.executeReader(account, store -> {
			for (Folder candidate : store.getDefaultFolder().list("*")) {
				if ((candidate.getType() & Folder.HOLDS_MESSAGES) != 0) {
					for (String attribute : ((IMAPFolder) candidate).getAttributes()) {
						if (Strings.CI.equals(attribute, "\\Archive")) {
							return candidate.getFullName();
						}
					}
				}
			}
			String name = configured.isEmpty() ? "Archiv" : configured;
			IMAPFolder configuredFolder = (IMAPFolder) store.getFolder(name);
			if (configuredFolder.exists()) {
				for (String attribute : configuredFolder.getAttributes()) {
					if (Strings.CI.equals(attribute, "\\All")) {
						throw new MessagingException("All mail cannot be used as cleanup archive");
					}
				}
			}
			return name;
		});
	}

	MailTrashStatus status(IUser user, String accountId, String name) throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		return mailbox.connections.executeReader(account, store -> {
			try (OpenFolder opened = new OpenFolder(store, name)) {
				Status status = (Status) opened.folder().doCommand(protocol ->
						protocol.status(name, new String[] { "UIDNEXT", "UIDVALIDITY" }));
				return new MailTrashStatus(status.uidvalidity, status.uidnext, store.hasCapability("UIDPLUS"));
			}
		});
	}

	int apply(IUser user, List<MailRef> refs, String targetName) throws MessagingException {
		if (refs.isEmpty() || refs.size() > 50) {
			throw new IllegalArgumentException("Cleanup selection must contain one to 50 identities");
		}
		MailRef first = refs.getFirst();
		MailboxAccount account = mailbox.visibleAccount(user, first.accountId());
		if (refs.stream().anyMatch(ref -> !ref.accountId().equals(first.accountId())
				|| !ref.folder().equals(first.folder()) || ref.uidValidity() != first.uidValidity())) {
			throw new IllegalArgumentException("Cleanup selection must share one source");
		}
		return mailbox.connections.execute(account, store -> {
			if (targetName == null && !store.hasCapability("UIDPLUS")) {
				return -1;
			}
			try (OpenFolder opened = new OpenFolder(store, first.folder(), true)) {
				IMAPFolder source = opened.folder();
				if (source.getUIDValidity() != first.uidValidity()) {
					return -1;
				}
				String selection = refs.stream().map(ref -> Long.toString(ref.uid())).collect(Collectors.joining(","));
				long[] uids = MailUidCommands.searchUids(source, "UID " + selection + " NOT FLAGGED");
				if (uids.length == 0) {
					return 0;
				}
				long[] affected;
				if (targetName == null) {
					MailUidCommands.uidCommand(source, "STORE", uids, null, Flags.Flag.DELETED, true);
					MailUidCommands.uidCommand(source, "EXPUNGE", uids, null, null, false);
					affected = uids;
				} else {
					if (Strings.CI.equals(first.folder(), targetName)) {
						return 0;
					}
					IMAPFolder target = (IMAPFolder) store.getFolder(targetName);
					if (!target.exists() && !target.create(Folder.HOLDS_MESSAGES)) {
						throw new MessagingException("Cleanup destination could not be created");
					}
					affected = MailUidCommands.moveMessages(store, source,
							MailUidCommands.targetFolder(store, targetName), uids);
				}
				mailbox.connections.dates.remove(account.id(), first.folder(), first.uidValidity(), affected);
				mailbox.connections.folderChanged(account.id(), source, affected, null);
				return affected.length;
			}
		});
	}
}
