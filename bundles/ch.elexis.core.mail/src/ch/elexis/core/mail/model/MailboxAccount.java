package ch.elexis.core.mail.model;

import java.util.List;

/**
 * Configuration of one mailbox as stored under <code>mail/client/account/&lt;id&gt;</code>. Secrets are stored
 * separately and are never part of this record.
 *
 * @param id
 *            the unique account id, <code>null</code> for a new account
 * @param displayName
 *            the name shown in the folder tree
 * @param emailAddress
 *            the address of the mailbox
 * @param provider
 *            the provider
 * @param scope
 *            shared or personal
 * @param ownerUserId
 *            the owning user of a personal account
 * @param delegateUserIds
 *            additional users of a personal account
 * @param visibleUserIds
 *            users that see a shared account
 * @param visibleGroupIds
 *            user groups whose members see a shared account
 * @param imap
 *            the IMAP server, or null for an SMTP-only account
 * @param smtp
 *            the SMTP server, or null for an IMAP-only account
 * @param username
 *            the login name
 * @param authMode
 *            the authentication mode
 * @param oauthSharedMailbox
 *            <code>true</code> if the mailbox is a Microsoft 365 shared mailbox
 * @param signatureHtml
 *            the sanitized HTML signature
 * @param sentCopyMode
 *            who stores the copy of sent messages
 * @param connectionBudget
 *            the maximum number of IMAP connections per station, 1 to 3
 * @param pollIntervalSeconds
 *            the status poll interval when IDLE is not used, 60 to 900
 */
public record MailboxAccount(String id, String displayName, String emailAddress, MailProvider provider,
		MailboxScope scope, String ownerUserId, List<String> delegateUserIds, List<String> visibleUserIds,
		List<String> visibleGroupIds, ServerEndpoint imap, ServerEndpoint smtp, String username, AuthMode authMode,
		boolean oauthSharedMailbox, String signatureHtml, SentCopyMode sentCopyMode, int connectionBudget,
		int pollIntervalSeconds) {
}
