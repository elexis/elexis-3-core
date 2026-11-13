package ch.elexis.core.mail.service;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.model.MailOAuthSignIn;
import ch.elexis.core.mail.model.MailProvider;

/** Interactive desktop OAuth sign-in and stored provider identity access. */
public interface IMailOAuthService {
	/**
	 * Checks whether a supported provider has configured client credentials.
	 * @param provider the provider
	 * @return true for configured M365 or GOOGLE, false otherwise
	 */
	boolean isAvailable(MailProvider provider);

	/**
	 * Signs in through PKCE and a one-shot loopback callback.
	 * @param user the active account manager
	 * @param accountId the saved OAuth account
	 * @param openBrowser receives the authorization URI for the system browser
	 * @return the identity after persistence; failure carries MailOAuthException, cancellation closes the listener
	 * @throws MailAccessException if the active user cannot manage the account
	 * @throws IllegalArgumentException if the account is missing or not a supported OAuth account
	 */
	CompletableFuture<MailOAuthSignIn> signIn(IUser user, String accountId, Consumer<URI> openBrowser);

	/**
	 * Removes the refresh token and provider identity and forgets cached access tokens.
	 * @param user the active account manager
	 * @param accountId the saved OAuth account
	 * @throws MailAccessException if management is denied
	 * @throws IllegalArgumentException if the account is missing or not a supported OAuth account
	 */
	void signOut(IUser user, String accountId);

	/**
	 * Reads the stored provider identity without connecting.
	 * @param user the user whose visibility is checked
	 * @param accountId the saved account
	 * @return the provider identity, empty when signed out
	 * @throws MailAccessException if the account is not visible
	 */
	Optional<String> getSignedInAs(IUser user, String accountId);

}
