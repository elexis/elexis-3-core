package ch.elexis.core.mail.internal.mailbox;

import java.util.List;

import jakarta.mail.Flags;
import jakarta.mail.FolderNotFoundException;
import jakarta.mail.MessageRemovedException;
import jakarta.mail.MessagingException;

import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;

import ch.elexis.core.model.IUser;

import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.model.MailBatchResult;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.MailNotFoundException;

class MailMessageActions {
	private final MailboxService mailbox;

	MailMessageActions(MailboxService mailbox) {
		this.mailbox = mailbox;
	}

	/** {@inheritDoc} */
	void setSeen(IUser user, MailRef ref, boolean seen) throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, ref.accountId());
		mailbox.connections.execute(account, store -> {
			writeSeen(store, ref, seen);
			return null;
		});
	}

	/** {@inheritDoc} */
	void setSeenOnOpen(IUser user, MailRef ref) {
		try {
			MailboxAccount account = mailbox.visibleAccount(user, ref.accountId());
			mailbox.connections.enqueueWrite(account, store -> {
				mailbox.visibleAccount(user, ref.accountId());
				writeSeen(store, ref, true);
				return null;
			}, () -> mailbox.connections.folderChanged(ref.accountId(), ref.folder()));
		} catch (MessagingException | RuntimeException e) {
			mailbox.log.debug("Mail open seen update could not be queued");
			mailbox.connections.folderChanged(ref.accountId(), ref.folder());
		}
	}

	private void writeSeen(IMAPStore store, MailRef ref, boolean seen) throws MessagingException {
		try (OpenFolder opened = new OpenFolder(store, ref.folder(), true)) {
			MailHeaderMapper.message(opened.folder(), ref).setFlag(Flags.Flag.SEEN, seen);
			mailbox.connections.requestGmailCounts(ref.accountId());
		} catch (MessageRemovedException | FolderNotFoundException e) {
			throw new MailNotFoundException("Mail message no longer exists");
		}
	}

	/** {@inheritDoc} */
	MailBatchResult move(IUser user, List<MailRef> refs, String targetFolder, String changeId)
			throws MessagingException {
		if (targetFolder == null || targetFolder.isBlank()) {
			throw new IllegalArgumentException("Mail destination is required");
		}
		return batch(user, refs, targetFolder, null, false, changeId);
	}

	/** {@inheritDoc} */
	MailBatchResult moveToTrash(IUser user, List<MailRef> refs, String changeId) throws MessagingException {
		return batch(user, refs, null, null, false, changeId);
	}

	/** {@inheritDoc} */
	MailBatchResult deletePermanently(IUser user, List<MailRef> refs, String changeId) throws MessagingException {
		return batch(user, refs, null, Flags.Flag.DELETED, true, changeId);
	}

	/** {@inheritDoc} */
	MailBatchResult setFlagged(IUser user, List<MailRef> refs, boolean flagged, String changeId)
			throws MessagingException {
		return batch(user, refs, null, Flags.Flag.FLAGGED, flagged, changeId);
	}

	/** {@inheritDoc} */
	MailBatchResult setSeen(IUser user, List<MailRef> refs, boolean seen, String changeId)
			throws MessagingException {
		return batch(user, refs, null, Flags.Flag.SEEN, seen, changeId);
	}

	private MailRef selection(List<MailRef> refs) {
		if (refs == null || refs.isEmpty() || refs.size() > 1000 || refs.stream().anyMatch(ref -> ref == null)) {
			throw new IllegalArgumentException("Mail selection must contain one to 1000 identities");
		}
		MailRef first = refs.getFirst();
		if (refs.stream().anyMatch(ref -> !ref.accountId().equals(first.accountId())
				|| !ref.folder().equals(first.folder())) || refs.stream().distinct().count() != refs.size()) {
			throw new IllegalArgumentException("Mail selection must contain distinct identities in one folder");
		}
		return first;
	}

	MailBatchResult batch(IUser user, List<MailRef> refs, String targetName, Flags.Flag flag, boolean value,
			String changeId) throws MessagingException {
		MailRef first = selection(refs);
		MailboxAccount account = mailbox.visibleAccount(user, first.accountId());
		return mailbox.connections.execute(account, store -> {
			IMAPFolder target = null;
			if (flag == null || flag == Flags.Flag.DELETED) {
				target = targetName == null ? MailUidCommands.trashFolder(store) : MailUidCommands.targetFolder(store, targetName);
				boolean same = first.folder().equals(target.getFullName());
				if (flag == Flags.Flag.DELETED ? !same : same) {
					throw new IllegalArgumentException("Invalid mail source folder for operation");
				}
			}
			long[] affected;
			try (OpenFolder opened = new OpenFolder(store, first.folder(), true)) {
				IMAPFolder source = opened.folder();
				if (refs.stream().anyMatch(ref -> ref.uidValidity() != first.uidValidity())
						|| source.getUIDValidity() != first.uidValidity()) {
					return new MailBatchResult(0, refs.size());
				}
				long[] uids = refs.stream().mapToLong(MailRef::uid).sorted().toArray();
				uids = MailUidCommands.existingUids(source, uids);
				if (uids.length == 0) {
					return new MailBatchResult(0, refs.size());
				}
				if (flag == null) {
					affected = MailUidCommands.moveMessages(store, source, target, uids);
				} else {
					MailUidCommands.uidCommand(source, "STORE", uids, null, flag, value);
					affected = uids;
					if (flag == Flags.Flag.DELETED) {
						if (store.hasCapability("UIDPLUS")) {
							MailUidCommands.uidCommand(source, "EXPUNGE", uids, null, null, false);
						} else {
							source.expunge();
						}
					}
				}
				if (affected.length > 0) {
					if (flag == null || flag == Flags.Flag.DELETED) {
						mailbox.connections.dates.remove(account.id(), source.getFullName(), first.uidValidity(), affected);
					}
					mailbox.connections.folderChanged(account.id(), source, affected, changeId);
				}
			} catch (FolderNotFoundException e) {
				return new MailBatchResult(0, refs.size());
			}
			if (flag == null && target != null && affected.length > 0) {
				try (OpenFolder opened = new OpenFolder(store, target.getFullName())) {
					mailbox.connections.folderChanged(account.id(), opened.folder(), affected, changeId);
				}
			}
			return new MailBatchResult(affected.length, refs.size() - affected.length);
		});
	}
}
