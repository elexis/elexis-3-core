package ch.elexis.core.mail.internal.send;

import org.eclipse.angus.mail.imap.AppendUID;
import org.eclipse.angus.mail.imap.IMAPFolder;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.model.MailDraft;
import ch.elexis.core.mail.model.MailFolderRole;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.MailNotFoundException;
import jakarta.mail.Flags;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.search.HeaderTerm;

final class MailDraftStore {
	private final MailSendService service;

	MailDraftStore(MailSendService service) {
		this.service = service;
	}

	/** {@inheritDoc} */
	MailDraft saveDraft(IUser user, MailDraft draft) throws MessagingException {
		MailboxAccount account = service.checkDraft(user, draft);
		if (account.imap() == null) {
			throw new MessagingException("IMAP account required for drafts");
		}
		MimeMessage message = service.buildWithMailClassLoader(user, account, draft);
		message.setFlag(Flags.Flag.DRAFT, true);
		message.setFlag(Flags.Flag.SEEN, true);
		String folder = service.sentCopies.roleFolder(user, account, MailFolderRole.DRAFTS);
		MailRef saved = service.connections.execute(account, store -> {
			service.visibleAccount(user, account.id());
			try (OpenFolder opened = new OpenFolder(store, folder, true)) {
				IMAPFolder target = opened.folder();
				if (store.hasCapability("UIDPLUS")) {
					AppendUID[] appended = target.appendUIDMessages(new Message[] { message });
					if (appended != null && appended.length == 1 && appended[0] != null) {
						return new MailRef(account.id(), folder, appended[0].uidvalidity, appended[0].uid);
					}
				} else {
					target.appendMessages(new Message[] { message });
				}
				Message newest = findMessageId(target, draft.messageId());
				if (newest == null) {
					throw new MessagingException("Appended draft UID could not be resolved");
				}
				return new MailRef(account.id(), folder, target.getUIDValidity(), target.getUID(newest));
			}
		});
		service.connections.folderChanged(account.id(), folder);
		if (draft.savedDraftRef() != null && !saved.equals(draft.savedDraftRef())) {
			try {
				remove(user, draft.savedDraftRef());
			} catch (MessagingException | RuntimeException e) {
				service.log.warn("Previous mail draft cleanup failed: {}", e.getClass().getSimpleName());
			}
		}
		return service.stored(draft, saved);
	}

	/** {@inheritDoc} */
	void discardDraft(IUser user, MailDraft draft) throws MessagingException {
		service.checkDraft(user, draft);
		if (draft.savedDraftRef() != null) {
			remove(user, draft.savedDraftRef());
		}
	}

	Message findMessageId(IMAPFolder folder, String id) throws MessagingException {
		Message newest = null;
		long uid = -1;
		for (Message message : folder.search(new HeaderTerm("Message-ID", id))) {
			if (id.equals(((MimeMessage) message).getMessageID()) && !message.isSet(Flags.Flag.DELETED)
					&& folder.getUID(message) > uid) {
				newest = message;
				uid = folder.getUID(message);
			}
		}
		return newest;
	}

	void remove(IUser user, MailRef ref) throws MessagingException {
		MailboxAccount account = service.visibleAccount(user, ref.accountId());
		service.connections.execute(account, store -> {
			service.visibleAccount(user, account.id());
			try (OpenFolder opened = new OpenFolder(store, ref.folder(), true)) {
				IMAPFolder folder = opened.folder();
				if (folder.getUIDValidity() != ref.uidValidity()) {
					throw new MailNotFoundException("Mail folder UID validity has changed");
				}
				Message old = folder.getMessageByUID(ref.uid());
				if (old != null && !old.isExpunged()) {
					old.setFlag(Flags.Flag.DELETED, true);
					if (store.hasCapability("UIDPLUS")) {
						folder.expunge(new Message[] { old });
					}
				}
			}
			return null;
		});
		service.connections.folderChanged(account.id(), ref.folder());
	}
}
