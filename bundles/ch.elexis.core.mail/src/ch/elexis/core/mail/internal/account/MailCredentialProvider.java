package ch.elexis.core.mail.internal.account;

import java.security.GeneralSecurityException;
import java.util.Arrays;

import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.internal.oauth.OAuth2TokenCache;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailboxAccount;

import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;

public final class MailCredentialProvider {
	private final PasswordCredentialProvider passwords;
	private final OAuth2TokenCache tokens;

	/**
	 * Creates a credential provider backed by password and OAuth token providers.
	 * @param passwords the stored password provider
	 * @param tokens the OAuth token cache
	 */
	public MailCredentialProvider(PasswordCredentialProvider passwords, OAuth2TokenCache tokens) {
		this.passwords = passwords;
		this.tokens = tokens;
	}

	/**
	 * Connects with account credentials, retrying OAuth authentication once after invalidating the cached token.
	 * @param account the account to authenticate
	 * @param smtp whether to select the SMTP login identity
	 * @param supplied the optional password used for password authentication
	 * @param login the connection callback
	 * @throws MessagingException if credentials cannot be obtained or the connection fails
	 */
	public void connect(MailboxAccount account, boolean smtp, char[] supplied, Login login) throws MessagingException {
		boolean oauth = account.authMode() == AuthMode.OAUTH2;
		for (int attempt = 0; attempt < (oauth ? 2 : 1); attempt++) {
			char[] password = credential(account, supplied);
			try {
				login.connect(username(account, smtp), password);
				return;
			} catch (AuthenticationFailedException e) {
				if (!oauth) {
					throw e;
				}
				tokens.invalidate(account.id());
				if (attempt == 1) {
					throw e;
				}
			} finally {
				Arrays.fill(password, '\0');
			}
		}
	}

	private char[] credential(MailboxAccount account, char[] supplied) throws MessagingException {
		if (account.authMode() == AuthMode.OAUTH2) {
			if (tokens == null) {
				throw new AuthenticationFailedException("Mail OAuth service is unavailable");
			}
			return tokens.accessToken(account).toCharArray();
		}
		if (supplied != null) {
			return supplied.clone();
		}
		try {
			return passwords.getPassword(account)
					.orElseThrow(() -> new AuthenticationFailedException("Mail credentials are required"));
		} catch (GeneralSecurityException e) {
			throw new AuthenticationFailedException("Mail credentials cannot be decrypted");
		}
	}

	private String username(MailboxAccount account, boolean smtp) throws AuthenticationFailedException {
		if (account.authMode() != AuthMode.OAUTH2) {
			return account.username();
		}
		if (smtp && account.oauthSharedMailbox()) {
			String identity = tokens.store().read(account, MailClientConfig.signedInAs(account.id()));
			if (identity == null || identity.isBlank()) {
				throw new AuthenticationFailedException("Mail OAuth identity is required");
			}
			return identity;
		}
		return account.emailAddress();
	}

	@FunctionalInterface
	public interface Login {
		/**
		 * Connects using the supplied login identity and credential.
		 * @param username the login identity
		 * @param password the credential cleared by the caller after the callback
		 * @throws MessagingException if the connection fails
		 */
		void connect(String username, char[] password) throws MessagingException;
	}
}
