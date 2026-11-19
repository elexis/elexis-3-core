package ch.elexis.core.mail.internal.mailbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import jakarta.mail.Flags;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;

import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;

import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.model.MailBatchResult;
import ch.elexis.core.mail.model.MailFolderRole;
import ch.elexis.core.mail.service.MailNotFoundException;

final class MailFolderBatch {
	private final MailboxService mailbox;
	private static final int CHUNK = 2000;
	private final String accountId;
	private final String folder;
	private final String changeId;
	private final boolean empty;
	private final boolean deleteTree;
	private List<String> folders;
	private int folderIndex;
	private MailFolderRole sourceRole;
	private String target;
	private long[] uids;
	private long validity;
	private int offset;
	private int changed;
	private int total;

	MailFolderBatch(MailboxService mailbox, String accountId, String folder, String changeId, boolean empty, boolean deleteTree) {
		this.mailbox = mailbox;
		this.accountId = accountId;
		this.folder = folder;
		this.changeId = changeId;
		this.empty = empty;
		this.deleteTree = deleteTree;
	}

	MailBatchResult step(IMAPStore store) throws MessagingException {
		List<String> pending = folders;
		if (pending == null) {
			IMAPFolder source = deleteTree ? mailbox.folders.movableFolder(store, folder) : mailbox.folders.existingFolder(store, folder);
			if (deleteTree && !mailbox.folders.inside(source, MailUidCommands.trashFolder(store))) {
				throw new IllegalArgumentException("Only trash descendants can be deleted permanently");
			}
			if (empty) {
				OpenFolder.invalidate(folder, source.getSeparator(), true);
			}
			sourceRole = deleteTree ? MailFolderRole.TRASH
					: empty ? MailFolderRoles.role(source, MailFolderRoles.attributedRoles(store.getDefaultFolder().list("*")))
					: MailFolderRole.NONE;
			pending = new ArrayList<>();
			if (empty && sourceRole == MailFolderRole.TRASH) {
				for (Folder child : mailbox.folders.descendants(source)) {
					pending.add(child.getFullName());
				}
			}
			pending.add(folder);
			folders = pending;
		}
		String name = pending.get(folderIndex);
		IMAPFolder source = mailbox.folders.existingFolder(store, name);
		boolean selectable = (source.getType() & Folder.HOLDS_MESSAGES) != 0;
		if (!empty && !selectable) {
			throw new MessagingException("Mail folder is not selectable");
		}
		if ((deleteTree || folderIndex < pending.size() - 1) && uids == null) {
			int count = selectable ? source.getMessageCount() : 0;
			source.setSubscribed(false);
			if (source.delete(false)) {
				mailbox.connections.dates.removeFolder(accountId, name, source.getSeparator());
				changed += count;
				return completedFolder(store, pending, source);
			}
		}
		if (selectable) {
			try (OpenFolder opened = new OpenFolder(store, name, true)) {
				IMAPFolder selected = opened.folder();
				long[] selectedUids = uids;
				if (selectedUids == null) {
					validity = selected.getUIDValidity();
					selectedUids = MailUidCommands.searchUids(selected, empty ? "ALL" : "UNSEEN");
					uids = selectedUids;
					if (!empty) {
						total = selected.getMessageCount();
					}
					if (empty && sourceRole != MailFolderRole.TRASH && sourceRole != MailFolderRole.JUNK
							&& selectedUids.length > 0) {
						target = MailUidCommands.trashFolder(store).getFullName();
					}
				}
				if (selected.getUIDValidity() != validity) {
					throw new MailNotFoundException("Mail folder generation changed");
				}
				if (empty) {
					mailbox.connections.dates.removeFolder(accountId, name, source.getSeparator());
				}
				int limit = store.hasCapability("X-GM-EXT-1") ? 200 : CHUNK;
				int end = Math.min(selectedUids.length, offset + limit);
				if (end > offset) {
					long[] chunk = Arrays.copyOfRange(selectedUids, offset, end);
					String destination = target;
					if (destination != null) {
						IMAPFolder trash = MailUidCommands.targetFolder(store, destination);
						mailbox.connections.dates.removeFolder(accountId, destination, trash.getSeparator());
						changed += MailUidCommands.moveMessages(store, selected, trash, chunk).length;
					} else if (empty) {
						MailUidCommands.clearMessages(store, selected, chunk);
						changed += chunk.length;
					} else {
						MailUidCommands.uidCommand(selected, "STORE", chunk, null, Flags.Flag.SEEN, true);
						changed += chunk.length;
					}
					offset = end;
				}
				if (end < selectedUids.length) {
					if (!empty) {
						mailbox.connections.folderCounts(accountId, name, total, selectedUids.length - end);
					}
					return null;
				}
			} catch (MessagingException e) {
				Throwable cause = e;
				Throwable nested = cause.getCause();
				while (nested != null) {
					cause = nested;
					nested = cause.getCause();
				}
				mailbox.log.warn("Mail folder batch failed after {} confirmed messages: {}, cause {}", changed,
						e.getClass().getSimpleName(), cause.getClass().getSimpleName());
				throw e;
			}
		}
		if (deleteTree || folderIndex < pending.size() - 1) {
			source.setSubscribed(false);
			if (!source.delete(false)) {
				throw new MessagingException("Could not delete mail folder");
			}
		}
		return completedFolder(store, pending, source);
	}

	private MailBatchResult completedFolder(IMAPStore store, List<String> pending, IMAPFolder source)
			throws MessagingException {
		if (folderIndex < pending.size() - 1) {
			folderIndex++;
			uids = null;
			offset = 0;
			return null;
		}
		for (String completed : pending) {
			if (completed.equals(folder) && !deleteTree) {
				mailbox.folders.announceFolder(accountId, store, source, changeId);
			} else {
				mailbox.connections.folderChanged(accountId, completed);
			}
		}
		String destination = target;
		if (destination != null) {
			mailbox.folders.announceFolder(accountId, store, MailUidCommands.targetFolder(store, destination), changeId);
		}
		return new MailBatchResult(changed, 0);
	}
}
