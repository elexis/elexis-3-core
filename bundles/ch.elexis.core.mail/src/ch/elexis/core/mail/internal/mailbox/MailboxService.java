package ch.elexis.core.mail.internal.mailbox;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.mail.Folder;
import jakarta.mail.MessagingException;

import org.eclipse.angus.mail.imap.IMAPStore;
import org.eclipse.angus.mail.imap.protocol.Status;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IUser;

import ch.elexis.core.mail.internal.connection.MailboxConnectionManager;
import ch.elexis.core.mail.model.AccountConnectionState;
import ch.elexis.core.mail.model.MailAttachment;
import ch.elexis.core.mail.model.MailBatchResult;
import ch.elexis.core.mail.model.MailCleanupCandidates;
import ch.elexis.core.mail.model.MailCleanupPage;
import ch.elexis.core.mail.model.MailFolder;
import ch.elexis.core.mail.model.MailHeader;
import ch.elexis.core.mail.model.MailListQuery;
import ch.elexis.core.mail.model.MailMessageView;
import ch.elexis.core.mail.model.MailPage;
import ch.elexis.core.mail.model.MailQuota;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailTrashStatus;
import ch.elexis.core.mail.model.MailUidScan;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.IMailboxAccountService;
import ch.elexis.core.mail.service.IMailboxService;
import ch.elexis.core.mail.service.MailAccessException;

/** Reads and manages visible mailboxes with serialized connections, bounded MIME parsing and sanitized display bodies. */
@Singleton
@Component(service = IMailboxService.class)
public class MailboxService implements IMailboxService {
	static final int PROVISIONAL_THRESHOLD = 2000;
	final Logger log = LoggerFactory.getLogger(getClass());

	@Inject
	@Reference
	private IMailboxAccountService accountService;
	@Inject
	@Reference
	MailboxConnectionManager connections;

	final MailFolderOperations folders = new MailFolderOperations(this);
	private final MailHeaderList headers = new MailHeaderList(this);
	private final MailMessageReader reader = new MailMessageReader(this);
	private final MailMessageActions actions = new MailMessageActions(this);

	/** {@inheritDoc} */
	@Override
	public <T> CompletableFuture<T> submitRequest(IUser user, String accountId, boolean reader, Supplier<T> request) {
		return connections.submitRequest(accountId, reader, () -> {
			visibleAccount(user, accountId);
			return request.get();
		});
	}

	/** {@inheritDoc} */
	@Override
	public AccountConnectionState getConnectionState(IUser user, String accountId) {
		visibleAccount(user, accountId);
		return connections.getState(accountId);
	}

	/** {@inheritDoc} */
	@Override
	public List<MailFolder> listFolders(IUser user, String accountId) throws MessagingException {
		return folders.listFolders(user, accountId);
	}

	/**
	 * Lists all folders and collects status counts when the server supports LIST-STATUS.
	 * @param store the connected store
	 * @param counts the map receiving available message and unread counts by full folder name
	 * @return the listed folders
	 * @throws MessagingException if capabilities or folders cannot be read
	 */
	public static Folder[] listedFolders(IMAPStore store, Map<String, Status> counts) throws MessagingException {
		return MailFolderOperations.listedFolders(store, counts);
	}

	/** {@inheritDoc} */
	@Override
	public MailFolder createFolder(IUser user, String accountId, String parentFolder, String name)
			throws MessagingException {
		return folders.createFolder(user, accountId, parentFolder, name);
	}

	/** {@inheritDoc} */
	@Override
	public MailFolder renameFolder(IUser user, String accountId, String folder, String newName)
			throws MessagingException {
		return folders.renameFolder(user, accountId, folder, newName);
	}

	/** {@inheritDoc} */
	@Override
	public MailBatchResult markFolderSeen(IUser user, String accountId, String folder, String changeId)
			throws MessagingException {
		return folders.markFolderSeen(user, accountId, folder, changeId);
	}

	/** {@inheritDoc} */
	@Override
	public CompletableFuture<MailBatchResult> markFolderSeenAsync(IUser user, String accountId, String folder,
			String changeId) {
		return folders.markFolderSeenAsync(user, accountId, folder, changeId);
	}

	/** {@inheritDoc} */
	@Override
	public MailFolder moveFolder(IUser user, String accountId, String folder, String newParent)
			throws MessagingException {
		return folders.moveFolder(user, accountId, folder, newParent);
	}

	/** {@inheritDoc} */
	@Override
	public MailFolder moveFolderToTrash(IUser user, String accountId, String folder) throws MessagingException {
		return folders.moveFolderToTrash(user, accountId, folder);
	}

	/** {@inheritDoc} */
	@Override
	public void deleteFolderPermanently(IUser user, String accountId, String folder) throws MessagingException {
		folders.deleteFolderPermanently(user, accountId, folder);
	}

	/** {@inheritDoc} */
	@Override
	public CompletableFuture<Void> deleteFolderPermanentlyAsync(IUser user, String accountId, String folder) {
		return folders.deleteFolderPermanentlyAsync(user, accountId, folder);
	}

	/** {@inheritDoc} */
	@Override
	public MailBatchResult emptyFolder(IUser user, String accountId, String folder, String changeId)
			throws MessagingException {
		return folders.emptyFolder(user, accountId, folder, changeId);
	}

	/** {@inheritDoc} */
	@Override
	public CompletableFuture<MailBatchResult> emptyFolderAsync(IUser user, String accountId, String folder,
			String changeId) {
		return folders.emptyFolderAsync(user, accountId, folder, changeId);
	}

	/** {@inheritDoc} */
	@Override
	public MailPage listHeaders(IUser user, String accountId, String folderName, int pageIndex)
			throws MessagingException {
		return headers.listHeaders(user, accountId, folderName, pageIndex);
	}

	/** {@inheritDoc} */
	@Override
	public MailPage listHeaders(IUser user, String accountId, String folderName, MailListQuery query, int pageIndex)
			throws MessagingException {
		return headers.listHeaders(user, accountId, folderName, query, pageIndex);
	}

	/** {@inheritDoc} */
	@Override
	public MailHeader locate(IUser user, String accountId, String folderName, long uid) throws MessagingException {
		return headers.locate(user, accountId, folderName, uid);
	}

	/** {@inheritDoc} */
	@Override
	public MailPage search(IUser user, String accountId, String folderName, String text, int pageIndex)
			throws MessagingException {
		return headers.search(user, accountId, folderName, text, pageIndex);
	}

	/** {@inheritDoc} */
	@Override
	public MailMessageView openMessage(IUser user, MailRef ref) throws MessagingException {
		return reader.openMessage(user, ref);
	}

	/** {@inheritDoc} */
	@Override
	public void prefetch(IUser user, Object session, List<MailRef> refs, Predicate<MailRef> needed,
			Consumer<MailMessageView> loaded) {
		reader.prefetch(user, session, refs, needed, loaded);
	}

	/** {@inheritDoc} */
	@Override
	public void writeMessage(IUser user, MailRef ref, OutputStream target) throws MessagingException, IOException {
		reader.writeMessage(user, ref, target);
	}

	/** {@inheritDoc} */
	@Override
	public MailAttachment writeAttachment(IUser user, MailRef ref, String partPath, OutputStream target)
			throws MessagingException, IOException {
		return reader.writeAttachment(user, ref, partPath, target);
	}

	/** {@inheritDoc} */
	@Override
	public MailQuota getQuota(IUser user, String accountId) {
		if (accountService.getVisibleAccounts(user).stream().filter(account -> account.imap() != null).noneMatch(account -> account.id().equals(accountId))
				&& accountService.getManageableAccounts(user).stream().filter(account -> account.imap() != null).noneMatch(account -> account.id().equals(accountId))) {
			throw new MailAccessException("Mail account access denied");
		}
		return connections.getQuota(accountId);
	}

	/** {@inheritDoc} */
	@Override
	public void setSeen(IUser user, MailRef ref, boolean seen) throws MessagingException {
		actions.setSeen(user, ref, seen);
	}

	/** {@inheritDoc} */
	@Override
	public void setSeenOnOpen(IUser user, MailRef ref) {
		actions.setSeenOnOpen(user, ref);
	}

	/** {@inheritDoc} */
	@Override
	public MailBatchResult move(IUser user, List<MailRef> refs, String targetFolder, String changeId)
			throws MessagingException {
		return actions.move(user, refs, targetFolder, changeId);
	}

	/** {@inheritDoc} */
	@Override
	public MailBatchResult moveToTrash(IUser user, List<MailRef> refs, String changeId) throws MessagingException {
		return actions.moveToTrash(user, refs, changeId);
	}

	/** {@inheritDoc} */
	@Override
	public MailBatchResult deletePermanently(IUser user, List<MailRef> refs, String changeId) throws MessagingException {
		return actions.deletePermanently(user, refs, changeId);
	}

	/** {@inheritDoc} */
	@Override
	public MailBatchResult setFlagged(IUser user, List<MailRef> refs, boolean flagged, String changeId)
			throws MessagingException {
		return actions.setFlagged(user, refs, flagged, changeId);
	}

	/** {@inheritDoc} */
	@Override
	public MailBatchResult setSeen(IUser user, List<MailRef> refs, boolean seen, String changeId)
			throws MessagingException {
		return actions.setSeen(user, refs, seen, changeId);
	}

	/** {@inheritDoc} */
	@Override
	public MailUidScan scanNewer(IUser user, String accountId, String folder, long afterUid, LocalDate since,
			int limit) throws MessagingException {
		return new MailUidScanner(this).scan(user, accountId, folder, afterUid, since, limit);
	}

	/** {@inheritDoc} */
	@Override
	public List<MailFolder> cleanupFolders(IUser user, String accountId) throws MessagingException {
		return folders.listFolders(user, accountId, true);
	}

	/** {@inheritDoc} */
	@Override
	public MailCleanupCandidates searchCleanup(IUser user, String accountId, String folder, LocalDate before,
			long upperUid) throws MessagingException {
		return new MailCleanupMailbox(this).search(user, accountId, folder, before, upperUid);
	}

	/** {@inheritDoc} */
	@Override
	public MailCleanupPage fetchCleanup(IUser user, String accountId, String folder, long uidValidity,
			List<Long> uids) throws MessagingException {
		return new MailCleanupMailbox(this).fetch(user, accountId, folder, uidValidity, uids);
	}

	/** {@inheritDoc} */
	@Override
	public String cleanupArchive(IUser user, String accountId, String configured) throws MessagingException {
		return new MailCleanupMailbox(this).archive(user, accountId, configured);
	}

	/** {@inheritDoc} */
	@Override
	public MailTrashStatus cleanupTrashStatus(IUser user, String accountId, String folder) throws MessagingException {
		return new MailCleanupMailbox(this).status(user, accountId, folder);
	}

	/** {@inheritDoc} */
	@Override
	public int applyCleanup(IUser user, List<MailRef> refs, String target) throws MessagingException {
		return new MailCleanupMailbox(this).apply(user, refs, target);
	}

	MailboxAccount visibleAccount(IUser user, String id) {
		return accountService.getVisibleAccounts(user).stream().filter(account -> account.imap() != null).filter(account -> account.id().equals(id)).findFirst()
				.orElseThrow(() -> new MailAccessException("Mail account access denied"));
	}
}
