package ch.elexis.core.mail.internal.mailbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;

import org.eclipse.angus.mail.iap.Argument;
import org.eclipse.angus.mail.iap.Response;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;
import org.eclipse.angus.mail.imap.protocol.BASE64MailboxDecoder;
import org.eclipse.angus.mail.imap.protocol.IMAPResponse;
import org.eclipse.angus.mail.imap.protocol.ListInfo;
import org.eclipse.angus.mail.imap.protocol.Status;

import ch.elexis.core.model.IUser;

import ch.elexis.core.mail.internal.connection.MailboxConnectionManager;
import ch.elexis.core.mail.internal.connection.OpenFolder;
import ch.elexis.core.mail.model.MailBatchResult;
import ch.elexis.core.mail.model.MailFolder;
import ch.elexis.core.mail.model.MailFolderRole;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.MailFolderExistsException;
import ch.elexis.core.mail.service.MailNotFoundException;

class MailFolderOperations {
	private final MailboxService mailbox;

	MailFolderOperations(MailboxService mailbox) {
		this.mailbox = mailbox;
	}

	/** {@inheritDoc} */
	List<MailFolder> listFolders(IUser user, String accountId) throws MessagingException {
		return listFolders(user, accountId, false);
	}

	List<MailFolder> listFolders(IUser user, String accountId, boolean reader) throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		MailboxConnectionManager.StoreCallback<List<MailFolder>> request = store -> {
			List<MailFolder> result = new ArrayList<>();
			Map<String, Status> counts = new HashMap<>();
			Folder[] folders = listedFolders(store, counts);
			Set<MailFolderRole> attributed = MailFolderRoles.attributedRoles(folders);
			for (Folder folder : folders) {
				boolean selectable = (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
				Folder parent = folder.getParent();
				String parentName = parent == null || parent.getFullName().isEmpty() ? null : parent.getFullName();
				Status status = selectable ? counts.get(folder.getFullName()) : null;
				if (selectable && status == null) {
					status = (Status) ((IMAPFolder) folder).doCommand(protocol ->
							protocol.status(folder.getFullName(), new String[] { "MESSAGES", "UNSEEN" }));
				}
				result.add(new MailFolder(folder.getFullName(), folder.getName(), parentName,
						MailFolderRoles.role((IMAPFolder) folder, attributed), selectable, status == null ? -1 : status.unseen,
						status == null ? -1 : status.total));
			}
			List<MailFolder> hierarchy = List.copyOf(result);
			result.sort(Comparator.comparing((MailFolder folder) -> MailFolderRoles.treeKey(folder, hierarchy)));
			return List.copyOf(result);
		};
		return reader ? mailbox.connections.executeReader(account, request)
				: mailbox.connections.executeRead(account, request);
	}

	static Folder[] listedFolders(IMAPStore store, Map<String, Status> counts) throws MessagingException {
		if (!store.hasCapability("LIST-STATUS")) {
			return store.getDefaultFolder().list("*");
		}
		return (Folder[]) ((IMAPFolder) store.getFolder("INBOX")).doCommand(protocol -> {
			Argument arguments = new Argument();
			arguments.writeString("");
			arguments.writeString("*");
			arguments.writeAtom(protocol.hasCapability("SPECIAL-USE") ? "RETURN (SPECIAL-USE STATUS (MESSAGES UNSEEN))"
					: "RETURN (STATUS (MESSAGES UNSEEN))");
			Response[] responses = protocol.command("LIST", arguments);
			List<Folder> folders = new ArrayList<>();
			for (Response response : responses) {
				if (response instanceof IMAPResponse imap) {
					if (imap.keyEquals("LIST")) {
						ListInfo info = new ListInfo(imap);
						if (Arrays.stream(info.attrs).anyMatch("\\NonExistent"::equalsIgnoreCase)) {
							info.canOpen = false;
						}
						if (!protocol.supportsUtf8()) {
							info.name = BASE64MailboxDecoder.decode(info.name);
						}
						folders.add(new ListedFolder(info, store));
					} else if (imap.keyEquals("STATUS")) {
						Status status = new Status(imap);
						if (!protocol.supportsUtf8()) {
							status.mbox = BASE64MailboxDecoder.decode(status.mbox);
						}
						counts.put(status.mbox, status);
					}
				}
			}
			protocol.notifyResponseHandlers(responses);
			protocol.handleResult(responses[responses.length - 1]);
			return folders.toArray(Folder[]::new);
		});
	}

	private static final class ListedFolder extends IMAPFolder {
		private ListedFolder(ListInfo info, IMAPStore store) {
			super(info, store);
		}
	}

	/** {@inheritDoc} */
	MailFolder createFolder(IUser user, String accountId, String parentFolder, String name)
			throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		return mailbox.connections.execute(account, store -> {
			Folder parent;
			if (parentFolder == null) {
				Folder[] namespaces = store.getPersonalNamespaces();
				parent = namespaces.length == 0 || namespaces[0].getFullName().isEmpty() ? store.getDefaultFolder()
						: namespaces[0];
			} else {
				parent = store.getFolder(parentFolder);
			}
			IMAPFolder target = folderTarget(parent, name);
			if (!target.create(Folder.HOLDS_MESSAGES | Folder.HOLDS_FOLDERS)) {
				if (target.exists()) {
					throw new MailFolderExistsException("Mail folder already exists");
				}
				throw new MessagingException("Could not create mail folder");
			}
			target.setSubscribed(true);
			return managedFolder(store, target);
		});
	}

	/** {@inheritDoc} */
	MailFolder renameFolder(IUser user, String accountId, String folder, String newName)
			throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		return mailbox.connections.execute(account, store -> {
			IMAPFolder source = movableFolder(store, folder);
			return renameFolderTree(account.id(), store, source, folderTarget(source.getParent(), newName));
		});
	}

	/** {@inheritDoc} */
	MailBatchResult markFolderSeen(IUser user, String accountId, String folder, String changeId)
			throws MessagingException {
		return mailbox.connections.await(markFolderSeenAsync(user, accountId, folder, changeId));
	}

	/** {@inheritDoc} */
	CompletableFuture<MailBatchResult> markFolderSeenAsync(IUser user, String accountId, String folder,
			String changeId) {
		MailFolderBatch batch = new MailFolderBatch(mailbox, accountId, folder, changeId, false, false);
		return mailbox.connections.submitSteps(accountId, () -> mailbox.visibleAccount(user, accountId), batch::step);
	}

	/** {@inheritDoc} */
	MailFolder moveFolder(IUser user, String accountId, String folder, String newParent)
			throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		return mailbox.connections.execute(account, store -> {
			IMAPFolder source = movableFolder(store, folder);
			Folder parent;
			if (newParent == null || newParent.isBlank()) {
				Folder[] namespaces = store.getPersonalNamespaces();
				parent = namespaces.length == 0 || namespaces[0].getFullName().isEmpty() ? store.getDefaultFolder()
						: namespaces[0];
			} else {
				parent = store.getFolder(newParent);
			}
			if (parent.getFullName().equals(source.getFullName()) || inside(parent, source)) {
				throw new IllegalArgumentException("Cannot move a folder into its own tree");
			}
			return renameFolderTree(accountId, store, source, folderTarget(parent, source.getName(), false));
		});
	}

	/** {@inheritDoc} */
	MailFolder moveFolderToTrash(IUser user, String accountId, String folder) throws MessagingException {
		MailboxAccount account = mailbox.visibleAccount(user, accountId);
		return mailbox.connections.execute(account, store -> {
			IMAPFolder source = movableFolder(store, folder);
			IMAPFolder trash = MailUidCommands.trashFolder(store);
			if (inside(source, trash) || inside(trash, source)) {
				throw new IllegalArgumentException("Invalid folder tree for moving to trash");
			}
			String name = source.getName();
			for (int suffix = 1;; suffix++) {
				IMAPFolder target;
				try {
					target = folderTarget(trash, suffix == 1 ? name : name + " (" + suffix + ")", false);
				} catch (MailFolderExistsException e) {
					continue;
				}
				return renameFolderTree(accountId, store, source, target);
			}
		});
	}

	/** {@inheritDoc} */
	void deleteFolderPermanently(IUser user, String accountId, String folder) throws MessagingException {
		mailbox.connections.await(deleteFolderPermanentlyAsync(user, accountId, folder));
	}

	/** {@inheritDoc} */
	CompletableFuture<Void> deleteFolderPermanentlyAsync(IUser user, String accountId, String folder) {
		MailFolderBatch batch = new MailFolderBatch(mailbox, accountId, folder, null, true, true);
		return mailbox.connections.submitSteps(accountId, () -> mailbox.visibleAccount(user, accountId), batch::step)
				.thenApply(result -> null);
	}

	/** {@inheritDoc} */
	MailBatchResult emptyFolder(IUser user, String accountId, String folder, String changeId)
			throws MessagingException {
		return mailbox.connections.await(emptyFolderAsync(user, accountId, folder, changeId));
	}

	/** {@inheritDoc} */
	CompletableFuture<MailBatchResult> emptyFolderAsync(IUser user, String accountId, String folder,
			String changeId) {
		MailFolderBatch batch = new MailFolderBatch(mailbox, accountId, folder, changeId, true, false);
		return mailbox.connections.submitSteps(accountId, () -> mailbox.visibleAccount(user, accountId), batch::step);
	}

	IMAPFolder existingFolder(IMAPStore store, String name) throws MessagingException {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("A mail folder name is required");
		}
		IMAPFolder folder = (IMAPFolder) store.getFolder(name);
		if (!folder.exists()) {
			throw new MailNotFoundException("Mail folder no longer exists");
		}
		return folder;
	}

	IMAPFolder movableFolder(IMAPStore store, String name) throws MessagingException {
		IMAPFolder source = existingFolder(store, name);
		if (source.getFullName().equalsIgnoreCase("INBOX")
				|| MailFolderRoles.role(source, MailFolderRoles.attributedRoles(store.getDefaultFolder().list("*"))) != MailFolderRole.NONE) {
			throw new IllegalArgumentException("Special mail folders cannot be moved or deleted");
		}
		return source;
	}

	boolean inside(Folder folder, Folder parent) throws MessagingException {
		return folder.getFullName().startsWith(parent.getFullName() + parent.getSeparator());
	}

	List<Folder> descendants(IMAPFolder source) throws MessagingException {
		char separator = source.getSeparator();
		String prefix = source.getFullName() + separator;
		return Arrays.stream(source.list("*")).filter(folder -> folder.getFullName().startsWith(prefix))
				.sorted(Comparator.comparingLong((Folder folder) -> folder.getFullName().chars()
						.filter(value -> value == separator).count()).reversed()).toList();
	}

	private MailFolder renameFolderTree(String accountId, IMAPStore store, IMAPFolder source, IMAPFolder target)
			throws MessagingException {
		OpenFolder.invalidate(source.getFullName(), source.getSeparator(), true);
		List<Folder> tree = new ArrayList<>(descendants(source));
		tree.add(source);
		Map<String, Boolean> subscriptions = new HashMap<>();
		for (Folder folder : tree) {
			subscriptions.put(folder.getFullName(), folder.isSubscribed());
			if (folder.isOpen()) {
				folder.close(false);
			}
		}
		String oldName = source.getFullName();
		String newName = target.getFullName();
		mailbox.connections.dates.removeFolder(accountId, oldName, source.getSeparator());
		mailbox.connections.dates.removeFolder(accountId, newName, target.getSeparator());
		if (!source.renameTo(target)) {
			if (target.exists()) {
				throw new MailFolderExistsException("Mail folder already exists");
			}
			throw new MessagingException("Could not rename mail folder");
		}
		for (Folder folder : tree) {
			String oldPath = folder.getFullName();
			IMAPFolder moved = (IMAPFolder) store.getFolder(newName + oldPath.substring(oldName.length()));
			moved.setSubscribed(Boolean.TRUE.equals(subscriptions.get(oldPath)));
			store.getFolder(oldPath).setSubscribed(false);
			mailbox.connections.folderChanged(accountId, oldPath);
			announceFolder(accountId, store, moved, null);
		}
		return managedFolder(store, target);
	}

	void announceFolder(String accountId, IMAPStore store, IMAPFolder folder, String changeId)
			throws MessagingException {
		if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
			mailbox.connections.folderChanged(accountId, folder, new long[0], changeId);
		} else {
			mailbox.connections.folderChanged(accountId, folder.getFullName());
		}
	}

	private IMAPFolder folderTarget(Folder parent, String name) throws MessagingException {
		return folderTarget(parent, name, true);
	}

	private IMAPFolder folderTarget(Folder parent, String name, boolean newName) throws MessagingException {
		if (!parent.getFullName().isEmpty() && !parent.exists()) {
			throw new MailNotFoundException("Mail parent folder does not exist");
		}
		if (!parent.getFullName().isEmpty() && (parent.getType() & Folder.HOLDS_FOLDERS) == 0) {
			throw new IllegalArgumentException("Mail parent cannot hold folders");
		}
		String leaf = name == null ? "" : newName ? name.trim() : name;
		if (leaf.isEmpty() || (newName && (leaf.length() > 100 || leaf.indexOf(parent.getSeparator()) >= 0
				|| leaf.indexOf('*') >= 0 || leaf.indexOf('%') >= 0 || leaf.chars().anyMatch(Character::isISOControl)
				|| leaf.equalsIgnoreCase("INBOX")))) {
			throw new IllegalArgumentException("Invalid mail folder name");
		}
		IMAPFolder target = (IMAPFolder) parent.getFolder(leaf);
		if (target.exists()) {
			throw new MailFolderExistsException("Mail folder already exists");
		}
		return target;
	}

	private MailFolder managedFolder(IMAPStore store, IMAPFolder folder) throws MessagingException {
		Folder parent = folder.getParent();
		boolean selectable = (folder.getType() & Folder.HOLDS_MESSAGES) != 0;
		return new MailFolder(folder.getFullName(), folder.getName(),
				parent == null || parent.getFullName().isEmpty() ? null : parent.getFullName(),
				MailFolderRoles.role(folder, MailFolderRoles.attributedRoles(store.getDefaultFolder().list("*"))), selectable,
				selectable ? folder.getUnreadMessageCount() : -1, selectable ? folder.getMessageCount() : -1);
	}
}
