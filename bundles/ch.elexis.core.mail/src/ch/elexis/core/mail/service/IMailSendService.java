package ch.elexis.core.mail.service;

import java.util.List;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.model.ComposeMode;
import ch.elexis.core.mail.model.MailDraft;
import ch.elexis.core.mail.model.MailRecipientSuggestion;
import ch.elexis.core.mail.model.MailRef;
import ch.elexis.core.mail.model.MailSendFiling;
import ch.elexis.core.mail.model.MailSendResult;
import ch.elexis.core.mail.model.MailboxAccount;
import jakarta.mail.MessagingException;

/**
 * Writes and sends messages. All methods block while talking to a server and must not be called from the UI
 * thread.
 */
public interface IMailSendService {

	/**
	 * Returns the accounts the user may send from.
	 *
	 * @param user
	 *            the Elexis user
	 * @return the accounts, empty if none
	 */
	List<MailboxAccount> getSendAccounts(IUser user);

	/**
	 * Prepares a new draft: recipients, subject prefix, quoted original, reply headers and forwarded attachments.
	 *
	 * @param user
	 *            the Elexis user
	 * @param mode
	 *            the kind of message
	 * @param accountId
	 *            the sending account, <code>null</code> to take the account of the original or the first one
	 * @param original
	 *            the stored draft for DRAFT, otherwise the answered or forwarded message, <code>null</code> for {@link ComposeMode#NEW}
	 * @return the prepared draft; DRAFT preserves its stored reference and Message-ID when present
	 * @throws MailAccessException
	 *             if the user may not access an account involved
	 * @throws MailNotFoundException
	 *             if the original no longer exists
	 * @throws MessagingException
	 *             if the original cannot be read or DRAFT references a message outside the drafts role folder
	 */
	MailDraft prepare(IUser user, ComposeMode mode, String accountId, MailRef original) throws MessagingException;

	/**
	 * Prepares a reply or inline forward from a readable filed message.
	 * @param user active sending user
	 * @param mode REPLY, REPLY_ALL or FORWARD
	 * @param documentId server-only document identity
	 * @return prepared draft without a mailbox answered reference
	 * @throws MessagingException if the document cannot be read or the mode is unsupported
	 * @throws MailAccessException if the user cannot send or is no longer active
	 */
	default MailDraft prepareFiled(IUser user, ComposeMode mode, String documentId) throws MessagingException {
		throw new MessagingException("Filed composition is unavailable");
	}

	/**
	 * Stores the draft in the drafts folder of its account and removes the copy stored before.
	 *
	 * @param user
	 *            the Elexis user
	 * @param draft
	 *            the draft to store
	 * @return the draft with the reference of the new copy
	 * @throws MailAccessException
	 *             if the user may not access the account
	 * @throws MessagingException
	 *             if the draft cannot be stored
	 */
	MailDraft saveDraft(IUser user, MailDraft draft) throws MessagingException;

	/**
	 * Removes the stored copy of a draft, if there is one.
	 *
	 * @param user
	 *            the Elexis user
	 * @param draft
	 *            the draft to discard
	 * @throws MailAccessException
	 *             if the user may not access the account
	 * @throws MessagingException
	 *             if the copy cannot be removed
	 */
	void discardDraft(IUser user, MailDraft draft) throws MessagingException;

	/**
	 * Sends the draft. Never loses content: after a failure the draft is stored in the drafts folder.
	 *
	 * @param user
	 *            the Elexis user
	 * @param draft
	 *            the draft to send
	 * @return the outcome
	 * @throws MailAccessException
	 *             if the user may not send from the account
	 */
	MailSendResult send(IUser user, MailDraft draft);

	/**
	 * Sends a draft and optionally waits for filing after successful delivery.
	 * @param user the sending user
	 * @param draft the captured draft
	 * @param filing the captured filing intent, null to skip filing
	 * @return the send outcome with an independent filing result
	 * @throws MailAccessException if the user cannot send from the account
	 */
	MailSendResult send(IUser user, MailDraft draft, MailSendFiling filing);

	/**
	 * Proposes recipients for a typed text: Elexis contacts with a mail address and addresses the user wrote to
	 * recently. Never connects to a mail server.
	 *
	 * @param user
	 *            the Elexis user
	 * @param query
	 *            the typed text, at least two characters
	 * @param limit
	 *            the maximum number of proposals
	 * @return the proposals, recent addresses first
	 */
	List<MailRecipientSuggestion> searchRecipients(IUser user, String query, int limit);
}
