package ch.elexis.core.mail.service;

import java.util.concurrent.CompletableFuture;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.model.MailAccountTestResult;
import ch.elexis.core.mail.model.MailboxAccount;

/** Independent asynchronous login probes with no folder access and no outgoing mail. */
public interface IMailAccountTester {
	/**
	 * Tests both servers using short-lived connections.
	 * @param user the active manager of the existing or unsaved account
	 * @param account the account settings to validate and test
	 * @param password a password to consume and clear, or null to use stored credentials
	 * @return independent IMAP and SMTP outcomes
	 * @throws MailAccessException if the user cannot manage the account
	 * @throws IllegalArgumentException if account validation fails
	 */
	CompletableFuture<MailAccountTestResult> test(IUser user, MailboxAccount account, char[] password);
}
