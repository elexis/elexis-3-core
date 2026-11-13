package ch.elexis.core.mail.service;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.model.AccountConnectionState;
import ch.elexis.core.mail.model.MailAttachment;
import ch.elexis.core.mail.model.MailBatchResult;
import ch.elexis.core.mail.model.MailCleanupCandidates;
import ch.elexis.core.mail.model.MailCleanupPage;
import ch.elexis.core.mail.model.MailFolder;
import ch.elexis.core.mail.model.MailHeader;
import ch.elexis.core.mail.model.MailMessageView;
import ch.elexis.core.mail.model.MailListQuery;
import ch.elexis.core.mail.model.MailPage;
import ch.elexis.core.mail.model.MailQuota;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailTrashStatus;
import ch.elexis.core.mail.model.MailUidScan;
import jakarta.mail.MessagingException;

/**
 * Reads and manages messages of visible mailboxes. Mail operations block while talking to the server and must not be
 * called from the UI thread. The connection state query never connects or waits for mail work.
 */
public interface IMailboxService {

	/**
	 * Number of message headers per page.
	 */
	int PAGE_SIZE = 50;

	/**
	 * Queues a complete request on its account lane; nested synchronous operations use that lane directly.
	 * Folder-wide writes use their asynchronous entry points instead of this callback.
	 * Implementations without lanes execute the callback immediately for compatibility.
	 * @param <T> the response type
	 * @param user the requesting user
	 * @param accountId the visible account
	 * @param reader whether the request only opens a message or its remote-image preference
	 * @param request the callback, restricted to this account and lane; it must check visibility before data access
	 * @return the request result without waiting for account work
	 */
	default <T> CompletableFuture<T> submitRequest(IUser user, String accountId, boolean reader, Supplier<T> request) {
		return CompletableFuture.completedFuture(request.get());
	}

	/**
	 * Marks unread folder messages in bounded steps that yield to user requests.
	 * @param user the requesting user
	 * @param accountId the account id
	 * @param folder the folder name
	 * @param changeId optional page correlation identifier
	 * @return the changed count or an operation failure
	 */
	default CompletableFuture<MailBatchResult> markFolderSeenAsync(IUser user, String accountId, String folder,
			String changeId) {
		try {
			return CompletableFuture.completedFuture(markFolderSeen(user, accountId, folder, changeId));
		} catch (MessagingException | RuntimeException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	/**
	 * Empties a folder in bounded steps that yield to user requests.
	 * @param user the requesting user
	 * @param accountId the account id
	 * @param folder the folder name
	 * @param changeId optional page correlation identifier
	 * @return the changed count or an operation failure
	 */
	default CompletableFuture<MailBatchResult> emptyFolderAsync(IUser user, String accountId, String folder,
			String changeId) {
		try {
			return CompletableFuture.completedFuture(emptyFolder(user, accountId, folder, changeId));
		} catch (MessagingException | RuntimeException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	/**
	 * Deletes a trash subtree, clearing populated folders in yielding steps if DELETE is refused.
	 * @param user the requesting user
	 * @param accountId the account id
	 * @param folder the trash descendant to delete
	 * @return completion or an operation failure
	 */
	default CompletableFuture<Void> deleteFolderPermanentlyAsync(IUser user, String accountId, String folder) {
		try {
			deleteFolderPermanently(user, accountId, folder);
			return CompletableFuture.completedFuture(null);
		} catch (MessagingException | RuntimeException e) {
			return CompletableFuture.failedFuture(e);
		}
	}

	/**
	 * Returns the latest quota without waiting for IMAP work or connecting.
	 *
	 * @param user
	 *            the requesting user
	 * @param accountId
	 *            the visible or manageable account id
	 * @return the session value, persisted value or UNKNOWN
	 * @throws MailAccessException
	 *             if the account is neither visible nor manageable
	 */
	MailQuota getQuota(IUser user, String accountId);

	/**
	 * Changes the seen flag by UID without expunging any messages.
	 *
	 * @param user
	 *            the requesting user
	 * @param ref
	 *            the message identity
	 * @param seen
	 *            the desired flag
	 * @throws MailAccessException
	 *             if the account is invisible
	 * @throws MailNotFoundException
	 *             if the UID or UIDVALIDITY no longer matches
	 * @throws MessagingException
	 *             if the flag cannot be changed
	 */
	void setSeen(IUser user, MailRef ref, boolean seen) throws MessagingException;

	/**
	 * Enqueues the automatic seen write before returning; later explicit writes must win.
	 * Implementations without a queue retain the synchronous compatibility behavior.
	 * @param user the requesting user
	 * @param ref the opened message
	 * @throws MessagingException if a synchronous implementation cannot update the flag
	 */
	default void setSeenOnOpen(IUser user, MailRef ref) throws MessagingException {
		setSeen(user, ref, true);
	}

	/**
	 * Moves messages within one account using MOVE or COPY and selective expunge.
	 * All identities must share an account and folder. Missing UIDs or a changed UIDVALIDITY count as missing.
	 * @param user the requesting user
	 * @param refs one to 1000 distinct source identities
	 * @param targetFolder the existing selectable destination, different from the source
	 * @param changeId optional page correlation identifier, null when absent
	 * @return the affected and missing counts
	 * @throws IllegalArgumentException if the selection is empty, too large or mixed, or the source is invalid
	 * @throws MailAccessException if the account is invisible
	 * @throws MailNotFoundException if the destination does not exist or is not selectable
	 * @throws MessagingException if the server operation fails
	 */
	MailBatchResult move(IUser user, List<MailRef> refs, String targetFolder, String changeId) throws MessagingException;

	/**
	 * Moves messages to the selectable trash folder; concurrent COPY fallbacks may create duplicates.
	 * All identities must share an account and folder. Missing UIDs or a changed UIDVALIDITY count as missing.
	 * @param user the requesting user
	 * @param refs one to 1000 distinct source identities
	 * @param changeId optional page correlation identifier, null when absent
	 * @return the affected and missing counts
	 * @throws IllegalArgumentException if the selection is empty, too large or mixed, or the source is invalid
	 * @throws MailAccessException if the account is invisible
	 * @throws MailNoTrashFolderException if no selectable trash folder exists
	 * @throws MessagingException if the server operation fails
	 */
	MailBatchResult moveToTrash(IUser user, List<MailRef> refs, String changeId) throws MessagingException;

	/**
	 * Deletes selected trash messages; without UIDPLUS expunges all deleted messages in trash.
	 * All identities must share an account and folder. Missing UIDs or a changed UIDVALIDITY count as missing.
	 * @param user the requesting user
	 * @param refs one to 1000 distinct source identities
	 * @param changeId optional page correlation identifier, null when absent
	 * @return the affected and missing counts
	 * @throws IllegalArgumentException if the selection is empty, too large or mixed, or the source is invalid
	 * @throws MailAccessException if the account is invisible
	 * @throws MailNoTrashFolderException if no selectable trash folder exists
	 * @throws MessagingException if the server operation fails
	 */
	MailBatchResult deletePermanently(IUser user, List<MailRef> refs, String changeId) throws MessagingException;

	/**
	 * Sets or clears the flagged status without expunging messages.
	 * All identities must share an account and folder. Missing UIDs or a changed UIDVALIDITY count as missing.
	 * @param user the requesting user
	 * @param refs one to 1000 distinct source identities
	 * @param flagged the desired flagged status
	 * @param changeId optional page correlation identifier, null when absent
	 * @return the affected and missing counts
	 * @throws IllegalArgumentException if the selection is empty, too large or mixed
	 * @throws MailAccessException if the account is invisible
	 * @throws MessagingException if the server operation fails
	 */
	MailBatchResult setFlagged(IUser user, List<MailRef> refs, boolean flagged, String changeId) throws MessagingException;

	/**
	 * Sets or clears the seen status and announces the changed folder.
	 * All identities must share an account and folder. Missing UIDs or a changed UIDVALIDITY count as missing.
	 * @param user the requesting user
	 * @param refs one to 1000 distinct source identities
	 * @param seen the desired seen status
	 * @param changeId optional page correlation identifier, null when absent
	 * @return the affected and missing counts
	 * @throws IllegalArgumentException if the selection is empty, too large or mixed
	 * @throws MailAccessException if the account is invisible
	 * @throws MessagingException if the server operation fails
	 */
	MailBatchResult setSeen(IUser user, List<MailRef> refs, boolean seen, String changeId) throws MessagingException;

	/**
	 * Returns the last known state without connecting or creating an account worker.
	 *
	 * @param user
	 *            the requesting Elexis user
	 * @param accountId
	 *            the visible account id
	 * @return the last known state, OFFLINE before first use
	 * @throws MailAccessException
	 *             if the account is not visible to the user
	 */
	AccountConnectionState getConnectionState(IUser user, String accountId);

	/**
	 * Lists the folder tree of an account.
	 *
	 * @param user
	 *            the Elexis user requesting the folders
	 * @param accountId
	 *            the account to read
	 * @return the folders with unread and total counts, parents before their children
	 * @throws MailAccessException
	 *             if the user may not access the account
	 * @throws MessagingException
	 *             if the server cannot be read
	 */
	List<MailFolder> listFolders(IUser user, String accountId) throws MessagingException;

	/**
	 * Creates and subscribes a folder in the first personal namespace or under an existing parent.
	 * Runs on the account executor and does not post folder events.
	 * @param user the requesting user
	 * @param accountId the visible account id
	 * @param parentFolder the full parent name, null for the top level
	 * @param name the leaf name, trimmed to one to 100 characters without separators, wildcards or controls
	 * @return the created folder with zero message counts
	 * @throws MailAccessException if the account is invisible
	 * @throws MailNotFoundException if the parent does not exist
	 * @throws MailFolderExistsException if the target already exists
	 * @throws IllegalArgumentException if the name is invalid, is INBOX, or the parent cannot hold folders
	 * @throws MessagingException if creation or subscription fails
	 */
	MailFolder createFolder(IUser user, String accountId, String parentFolder, String name) throws MessagingException;

	/**
	 * Renames a normal folder under its existing parent, retaining its messages and children.
	 * Preserves subscriptions of the entire tree, invalidates date snapshots and posts folder/count events.
	 * @param user the requesting user
	 * @param accountId the visible account id
	 * @param folder the full source folder name
	 * @param newName the leaf name, trimmed to one to 100 characters without separators, wildcards or controls
	 * @return the renamed folder as returned by listFolders
	 * @throws MailAccessException if the account is invisible
	 * @throws MailNotFoundException if the source or parent does not exist
	 * @throws MailFolderExistsException if the target already exists
	 * @throws IllegalArgumentException if the name is invalid, the source has a role or the parent cannot hold folders
	 * @throws MessagingException if renaming or subscription fails
	 */
	MailFolder renameFolder(IUser user, String accountId, String folder, String newName) throws MessagingException;


	/**
	 * Marks every message in a folder as seen with one UID STORE on the account worker.
	 * @param user the requesting user
	 * @param accountId the visible account id
	 * @param folder the full folder name
	 * @param changeId optional event correlation identifier
	 * @return previously unread messages as changed, with zero missing
	 * @throws MailAccessException if the account is invisible
	 * @throws IllegalArgumentException if the folder name is blank
	 * @throws MailNotFoundException if the folder no longer exists
	 * @throws MessagingException if IMAP fails
	 */
	MailBatchResult markFolderSeen(IUser user, String accountId, String folder, String changeId) throws MessagingException;

	/**
	 * Moves a normal folder and its descendants by RENAME, preserving their subscriptions and messages.
	 * @param user the requesting user
	 * @param accountId the visible account id
	 * @param folder the full source folder name
	 * @param newParent the destination parent, null or blank for the first personal namespace
	 * @return the moved folder with its new full name
	 * @throws MailAccessException if the account is invisible
	 * @throws IllegalArgumentException for a role folder, INBOX, blank source or a parent inside the source tree
	 * @throws MailNotFoundException if source or parent no longer exists
	 * @throws MailFolderExistsException if the destination already exists
	 * @throws MessagingException if IMAP fails
	 */
	MailFolder moveFolder(IUser user, String accountId, String folder, String newParent) throws MessagingException;

	/**
	 * Moves a normal folder tree into the trash, choosing the first free leaf name with a numbered suffix.
	 * @param user the requesting user
	 * @param accountId the visible account id
	 * @param folder the full source folder name
	 * @return the moved folder in the trash
	 * @throws MailAccessException if the account is invisible
	 * @throws IllegalArgumentException for role folders, INBOX, blank source or a source already inside the trash
	 * @throws MailNotFoundException if the source no longer exists
	 * @throws MailNoTrashFolderException if there is no selectable trash folder
	 * @throws MessagingException if IMAP fails
	 */
	MailFolder moveFolderToTrash(IUser user, String accountId, String folder) throws MessagingException;

	/**
	 * Permanently deletes a trash descendant and its children, deepest first, on the account worker.
	 * @param user the requesting user
	 * @param accountId the visible account id
	 * @param folder the full source folder name
	 * @throws MailAccessException if the account is invisible
	 * @throws IllegalArgumentException for role folders, INBOX, blank source or anything outside the trash descendants
	 * @throws MailNotFoundException if the source no longer exists
	 * @throws MailNoTrashFolderException if there is no selectable trash folder
	 * @throws MessagingException if IMAP fails
	 */
	void deleteFolderPermanently(IUser user, String accountId, String folder) throws MessagingException;

	/**
	 * Moves all messages to the trash, or permanently empties trash/junk; emptying trash also removes its children.
	 * Uses UID lists or ranges without fetching message envelopes, on the account worker.
	 * @param user the requesting user
	 * @param accountId the visible account id
	 * @param folder the full source folder name
	 * @param changeId optional event correlation identifier
	 * @return handled messages (including deleted trash descendants) as changed, with zero missing
	 * @throws MailAccessException if the account is invisible
	 * @throws IllegalArgumentException if the folder name is blank
	 * @throws MailNotFoundException if the source no longer exists
	 * @throws MailNoTrashFolderException if moving messages requires a missing trash folder
	 * @throws MessagingException if IMAP fails
	 */
	MailBatchResult emptyFolder(IUser user, String accountId, String folder, String changeId) throws MessagingException;

	/**
	 * Loads one page of message headers, newest first.
	 *
	 * @param user
	 *            the Elexis user requesting the page
	 * @param accountId
	 *            the account to read
	 * @param folder
	 *            the full folder name
	 * @param pageIndex
	 *            the zero based page number
	 * @return the page with headers and the total count
	 * @throws MailAccessException
	 *             if the user may not access the account
	 * @throws MessagingException
	 *             if the server cannot be read
	 */
	MailPage listHeaders(IUser user, String accountId, String folder, int pageIndex) throws MessagingException;

	/**
	 * Loads a page ordered over the entire folder by received date, then UID.
	 * Page zero refreshes the date snapshot; later pages reuse it until the next page-zero request or connection reset.
	 * @param user the requesting user
	 * @param accountId the visible account id
	 * @param folder the full folder name
	 * @param query the required order, filter and optional server-side text search
	 * @param pageIndex the zero based page index
	 * @return the page and total number of matches
	 * @throws IllegalArgumentException if query or page index is invalid
	 * @throws MailAccessException if the account is invisible
	 * @throws MessagingException if the server cannot be read or search fails, without local search fallback
	 */
	MailPage listHeaders(IUser user, String accountId, String folder, MailListQuery query, int pageIndex)
			throws MessagingException;


	/**
	 * Locates one header by UID in the current folder generation without reading its body.
	 * @param user the requesting user
	 * @param accountId the visible account id
	 * @param folder the full folder name
	 * @param uid the positive UID
	 * @return the header with a current message identity
	 * @throws MailAccessException if the account is invisible
	 * @throws MailNotFoundException if the message no longer exists
	 * @throws IllegalArgumentException if the UID is invalid
	 * @throws MessagingException if the server cannot be read
	 */
	MailHeader locate(IUser user, String accountId, String folder, long uid) throws MessagingException;

	/**
	 * Searches on the server only, with every word matching From, To, Cc, Subject or Body.
	 * @param user the requesting user
	 * @param accountId the visible account
	 * @param folder the full folder name
	 * @param text the search text, trimmed length two to 200 characters
	 * @param pageIndex the zero based page number
	 * @return matching headers, newest received date and highest UID first, in pages of 50
	 * @throws IllegalArgumentException if text or page index is invalid
	 * @throws MailAccessException if the account is invisible
	 * @throws MessagingException if server search fails, without client-side fallback
	 */
	MailPage search(IUser user, String accountId, String folder, String text, int pageIndex) throws MessagingException;

	/**
	 * Loads a message for display, with sanitized HTML and attachment metadata. Attachment content is not loaded.
	 *
	 * @param user
	 *            the Elexis user requesting the message
	 * @param ref
	 *            the message reference
	 * @return the message view
	 * @throws MailAccessException
	 *             if the user may not access the account
	 * @throws MailNotFoundException
	 *             if the message was moved or deleted meanwhile
	 * @throws MessagingException
	 *             if the server cannot be read
	 */
	MailMessageView openMessage(IUser user, MailRef ref) throws MessagingException;

	/**
	 * Queues up to five speculative PEEK reads on the account reader lane and returns immediately.
	 * A newer call replaces queued reads for the same session and account. User work takes priority.
	 * Failures are dropped without change events or seen flags; successful views use the same sanitizer as open.
	 * @param user requesting user, checked again before background access
	 * @param session session identity used to replace queued work
	 * @param refs one to five message identities
	 * @param needed predicate checked before loading to skip views already cached
	 * @param loaded callback invoked for each loaded view on the reader thread
	 * @throws MailAccessException if the account is not visible
	 * @throws IllegalArgumentException if the parameters are invalid
	 */
	void prefetch(IUser user, Object session, List<MailRef> refs, Predicate<MailRef> needed,
			Consumer<MailMessageView> loaded);


	/**
	 * Streams the decoded content of one attachment or inline part to the target without holding it in memory.
	 *
	 * @param user
	 *            the Elexis user requesting the part
	 * @param ref
	 *            the message reference
	 * @param partPath
	 *            the part path as returned in {@link MailAttachment#partPath()}
	 * @param target
	 *            the stream to write to, not closed by this method
	 * @return the metadata of the written part
	 * @throws MailAccessException
	 *             if the user may not access the account
	 * @throws MailNotFoundException
	 *             if the message or the part does not exist
	 * @throws MessagingException
	 *             if the server cannot be read
	 * @throws IOException
	 *             if writing to the target fails
	 */
	MailAttachment writeAttachment(IUser user, MailRef ref, String partPath, OutputStream target)
			throws MessagingException, IOException;

	/**
	 * Streams the complete original message, including all headers and MIME parts, into the target.
	 * @param user the requesting user
	 * @param ref the message identity
	 * @param target the output stream, not closed by this method
	 * @throws MailAccessException if the account is invisible
	 * @throws MailNotFoundException if the message or folder generation no longer exists
	 * @throws MessagingException if the server cannot be read
	 * @throws IOException if staging or writing to the target fails
	 */
	void writeMessage(IUser user, MailRef ref, OutputStream target) throws MessagingException, IOException;

	/**
	 * Scans ascending UIDs above a cursor without changing message flags.
	 * @param user the requesting user
	 * @param accountId the visible account
	 * @param folder the full folder name
	 * @param afterUid the exclusive UID cursor, zero for the beginning
	 * @param since optional inclusive received date
	 * @param limit the positive maximum number of references
	 * @return the folder generation, references and whether more matches exist
	 * @throws MessagingException if the server search fails
	 * @throws IllegalArgumentException if cursor or limit is invalid
	 * @throws MailAccessException if the account is invisible
	 */
	MailUidScan scanNewer(IUser user, String accountId, String folder, long afterUid, LocalDate since, int limit)
			throws MessagingException;

	/**
	 * Lists role folders on the reader lane without refreshing the write-lane quota cache.
	 * @param user visible account user
	 * @param accountId account identity
	 * @return mailbox folders with roles
	 * @throws MessagingException if listing fails
	 */
	List<MailFolder> cleanupFolders(IUser user, String accountId) throws MessagingException;

	/**
	 * Searches all unflagged cleanup candidates of one folder once, in ascending UID order.
	 * @param user visible account user
	 * @param accountId account identity
	 * @param folder source folder
	 * @param before exclusive internal-date cutoff, null for trash
	 * @param upperUid inclusive trash boundary, zero for no upper boundary
	 * @return matching UIDs with the searched identity
	 * @throws MessagingException if reading fails
	 */
	MailCleanupCandidates searchCleanup(IUser user, String accountId, String folder, LocalDate before,
			long upperUid) throws MessagingException;

	/**
	 * Fetches the headers of at most 50 candidates, skipping messages flagged or removed since the search.
	 * @param user visible account user
	 * @param accountId account identity
	 * @param folder source folder
	 * @param uidValidity identity returned by the search
	 * @param uids one to 50 candidate UIDs
	 * @return fetched headers, empty with the current identity if it changed
	 * @throws MessagingException if reading fails
	 */
	MailCleanupPage fetchCleanup(IUser user, String accountId, String folder, long uidValidity, List<Long> uids)
			throws MessagingException;

	/**
	 * Resolves the special-use archive, configured folder or Archiv without creating it.
	 * @param user visible account user
	 * @param accountId account identity
	 * @param configured configured fallback
	 * @return proposed destination
	 * @throws MessagingException if listing fails
	 */
	String cleanupArchive(IUser user, String accountId, String configured) throws MessagingException;

	/**
	 * Reads trash STATUS and UIDPLUS on the reader lane.
	 * @param user visible account user
	 * @param accountId account identity
	 * @param folder trash folder
	 * @return current identity and next UID
	 * @throws MessagingException if reading fails
	 */
	MailTrashStatus cleanupTrashStatus(IUser user, String accountId, String folder) throws MessagingException;

	/**
	 * Moves a bounded selection or purges it selectively, rechecking identity and flags on the write lane.
	 * @param user visible account user
	 * @param refs at most 50 source identities
	 * @param target destination, null for UIDPLUS-only purge
	 * @return affected count, negative on identity change or unavailable UIDPLUS
	 * @throws MessagingException if changing the mailbox fails
	 */
	int applyCleanup(IUser user, List<MailRef> refs, String target) throws MessagingException;
}
