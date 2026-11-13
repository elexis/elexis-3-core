package ch.elexis.core.mail.service;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.model.MailboxAccount;

/**
 * Stores mailbox accounts and their secrets. Never connects to a mail server, so it is safe to use during startup.
 */
public interface IMailboxAccountService {

	/**
	 * Returns the accounts the user may see, in the configured order.
	 *
	 * @param user
	 *            the Elexis user
	 * @return the visible accounts, empty if none
	 */
	List<MailboxAccount> getVisibleAccounts(IUser user);

	/**
	 * Returns all accounts for account managers, otherwise only manageable visible accounts.
	 * @param user the active user
	 * @return manageable accounts in configured order
	 * @throws MailAccessException if the user is not active
	 */
	List<MailboxAccount> getManageableAccounts(IUser user);

	/**
	 * Checks the shared-account management right of the active user.
	 * @param user the user
	 * @return whether the active user has the right
	 */
	boolean canManageShared(IUser user);

	/**
	 * Checks for a stored password or refresh token without decrypting or connecting.
	 * @param user the user whose visibility is checked
	 * @param accountId the saved account
	 * @return whether the authentication mode's credential is stored
	 * @throws MailAccessException if the account is not visible
	 * @throws IllegalArgumentException if the account is unknown
	 */
	boolean hasCredentials(IUser user, String accountId);

	/**
	 * Loads an account without checking the visibility.
	 *
	 * @param accountId
	 *            the account id
	 * @return the account, empty if it does not exist
	 */
	Optional<MailboxAccount> getAccount(String accountId);

	/**
	 * Checks whether an account was inherited from the legacy mail client.
	 * @param accountId the account id
	 * @return whether legacy transport security is permitted
	 */
	boolean isLegacy(String accountId);

	/**
	 * Reads the mandator selection without granting any account access.
	 * @param accountId the account id
	 * @return selected mandator ids in configured order
	 */
	List<String> getMandatorIds(String accountId);

	/**
	 * Updates the mandator selection with the same management checks as saving.
	 * @param user the active user
	 * @param accountId the saved account id
	 * @param mandatorIds selected mandator ids, empty to clear
	 * @throws MailAccessException if management is denied
	 */
	void setMandatorIds(IUser user, String accountId, List<String> mandatorIds);

	/**
	 * Creates or updates an account. A new account (id <code>null</code>) gets a generated id.
	 * Scope changes require management of the existing account and the shared-account management right.
	 * Changing to shared clears the owner and delegates and defaults visibility to the old owner and delegates.
	 * Changing to personal assigns the supplied owner (default: active user), defaults delegates to old visible users
	 * without the new owner, and clears shared visibility. Non-empty supplied target lists override these defaults.
	 * Stored password, refresh token and signed-in identity move unchanged before the definition is saved.
	 * The current personal owner or an account manager may transfer ownership within personal scope.
	 * The new owner must exist and have a contact. Delegates are deduplicated and exclude the new owner;
	 * the old owner is not added automatically. Authorization uses the existing account only.
	 * A successful save posts one ACCOUNTS event.
	 *
	 * @param user
	 *            the Elexis user saving the account
	 * @param account
	 *            the account to save
	 * @return the saved account with its id
	 * @throws MailAccessException
	 *             if the user may not manage the account or change its scope or owner
	 * @throws IllegalArgumentException if settings are invalid, the account is unknown, or the new owner
	 *             does not exist or has no assigned contact
	 * @throws IllegalStateException if a secret owner or its contact is unavailable
	 */
	MailboxAccount save(IUser user, MailboxAccount account);

	/**
	 * Removes an account together with its secrets.
	 *
	 * @param user
	 *            the Elexis user removing the account
	 * @param accountId
	 *            the account id
	 * @throws MailAccessException
	 *             if the user may not manage the account
	 */
	void remove(IUser user, String accountId);

	/**
	 * Stores the password or mail token of an account encrypted, globally for a shared account and for the owner
	 * of a personal account.
	 *
	 * @param user
	 *            the Elexis user setting the password
	 * @param accountId
	 *            the account id
	 * @param password
	 *            the password, the caller should clear the array afterwards
	 * @throws MailAccessException
	 *             if the user may not manage the account
	 */
	void setPassword(IUser user, String accountId, char[] password);
}
