package ch.elexis.core.mail.internal.mailbox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.eclipse.angus.mail.iap.Argument;
import org.eclipse.angus.mail.iap.Response;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.protocol.IMAPResponse;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailUidScan;
import ch.elexis.core.mail.model.MailboxAccount;
import jakarta.mail.MessagingException;

/** Searches UIDs on the reader lane without fetching message bodies or flags. */
final class MailUidScanner {
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d-MMM-yyyy", Locale.ENGLISH);
	private final MailboxService mailbox;

	MailUidScanner(MailboxService mailbox) {
		this.mailbox = mailbox;
	}

	MailUidScan scan(IUser user, String accountId, String name, long afterUid, LocalDate since, int limit)
			throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		if (afterUid < 0 || afterUid > 0xffffffffL || limit < 1) {
			throw new IllegalArgumentException("Invalid UID scan parameters");
		}
		return mailbox.connections.executeReader(account, store -> {
			try (OpenFolder opened = new OpenFolder(store, name)) {
				IMAPFolder folder = opened.folder();
				long validity = folder.getUIDValidity();
				if (afterUid == 0xffffffffL) {
					return new MailUidScan(validity, List.of(), false);
				}
				TreeSet<Long> uids = new TreeSet<>();
				folder.doCommand(protocol -> {
					Argument args = new Argument();
					args.writeAtom("UID");
					args.writeAtom((afterUid + 1) + ":*");
					if (since != null) {
						args.writeAtom("SINCE");
						args.writeAtom(DATE.format(since));
					}
					Response[] responses = protocol.command("UID SEARCH", args);
					for (Response response : responses) {
						if (response instanceof IMAPResponse imap && imap.keyEquals("SEARCH")) {
							long uid;
							while ((uid = imap.readLong()) != -1) {
								if (uid > afterUid) {
									uids.add(uid);
								}
							}
						}
					}
					protocol.notifyResponseHandlers(responses);
					protocol.handleResult(responses[responses.length - 1]);
					return null;
				});
				List<MailRef> refs = uids.stream().limit(limit).map(uid -> new MailRef(accountId, name, validity, uid))
						.toList();
				return new MailUidScan(validity, refs, uids.size() > limit);
			}
		});
	}
}
