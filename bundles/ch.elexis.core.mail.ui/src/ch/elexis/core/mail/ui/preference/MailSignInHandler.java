package ch.elexis.core.mail.ui.preference;

import java.net.URI;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Consumer;

import ch.elexis.core.model.IUser;
import ch.elexis.core.mail.model.MailOAuthSignIn;
import ch.elexis.core.mail.service.IMailOAuthService;
import ch.elexis.core.mail.service.IMailboxAccountService;
import ch.elexis.core.mail.service.MailAccessException;
import ch.elexis.core.mail.service.MailOAuthException;

/** Shared asynchronous account sign-in for preference and web callers. */
public final class MailSignInHandler {
	private MailSignInHandler() {
	}

	/**
	 * Starts browser sign-in for an account managed by the user.
	 * @param user active user
	 * @param accountId saved account identifier
	 * @param accounts account service
	 * @param oauth OAuth service
	 * @param openBrowser UI-thread browser launcher
	 * @return persisted provider identity
	 */
	public static CompletableFuture<MailOAuthSignIn> signIn(IUser user, String accountId,
			IMailboxAccountService accounts, IMailOAuthService oauth, Consumer<URI> openBrowser) {
		if (accounts.getManageableAccounts(user).stream().noneMatch(account -> accountId.equals(account.id()))) {
			throw new MailAccessException("Mail account management denied"); //$NON-NLS-1$
		}
		return oauth.signIn(user, accountId, openBrowser);
	}

	/**
	 * Maps failures without returning provider responses or sensitive exception
	 * messages.
	 * 
	 * @param failure completion or service failure
	 * @return stable public error code
	 */
	public static String error(Throwable failure) {
		Throwable cause = failure;
		while (cause instanceof CompletionException && cause.getCause() != null) {
			cause = cause.getCause();
		}
		if (cause instanceof MailOAuthException oauthFailure) {
			return oauthFailure.reason().name();
		}
		if (cause instanceof MailAccessException) {
			return "ACCESS_DENIED"; //$NON-NLS-1$
		}
		if (cause instanceof CancellationException) {
			return "CANCELLED"; //$NON-NLS-1$
		}
		if (cause instanceof IllegalArgumentException) {
			return "BAD_REQUEST"; //$NON-NLS-1$
		}
		return "FAILED"; //$NON-NLS-1$
	}
}
