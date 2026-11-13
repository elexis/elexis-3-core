package ch.elexis.core.mail.internal.oauth;

import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.internal.account.CredentialCipher;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;

public final class OAuth2AccountStore {
	private final IConfigService config;
	private final CredentialCipher cipher;
	private final Function<String, Optional<IUser>> owners;
	private final Function<String, Optional<IContact>> contacts;

	OAuth2AccountStore(IConfigService config, Function<String, Optional<IUser>> owners,
			Function<String, Optional<IContact>> contacts) {
		this.config = config;
		this.owners = owners;
		this.contacts = contacts;
		cipher = new CredentialCipher(() -> config.get(MailClientConfig.SECRET_SALT, null),
				salt -> config.set(MailClientConfig.SECRET_SALT, salt, false));
	}

	/**
	 * Reads a configuration value from the owner contact for personal accounts or from global configuration otherwise.
	 * @param account the account determining the configuration scope
	 * @param key the configuration key
	 * @return the stored value, or null if absent
	 */
	public String read(MailboxAccount account, String key) {
		return account.scope() == MailboxScope.PERSONAL ? config.get(owner(account), key, null)
				: config.get(key, null);
	}

	String refreshToken(MailboxAccount account) throws GeneralSecurityException {
		return decrypt(read(account, MailClientConfig.refreshToken(account.id())));
	}

	String decrypt(String encrypted) throws GeneralSecurityException {
		if (encrypted == null || encrypted.isEmpty()) {
			return "";
		}
		char[] clear = cipher.decrypt(encrypted);
		try {
			return new String(clear);
		} finally {
			Arrays.fill(clear, '\0');
		}
	}

	void refreshToken(MailboxAccount account, String token) throws GeneralSecurityException {
		char[] clear = token.toCharArray();
		try {
			write(account, MailClientConfig.refreshToken(account.id()), encrypt(clear));
		} finally {
			Arrays.fill(clear, '\0');
		}
	}

	String encrypt(char[] clear) throws GeneralSecurityException {
		return cipher.encrypt(clear);
	}

	void write(MailboxAccount account, String key, String value) {
		if (account.scope() == MailboxScope.PERSONAL) {
			config.set(owner(account), key, value);
		} else {
			config.set(key, value, false);
		}
	}

	private IContact owner(MailboxAccount account) {
		IUser user = owners.apply(account.ownerUserId())
				.orElseThrow(() -> new IllegalStateException("Account owner unavailable"));
		IContact contact = contacts.apply(user.getAssociatedContactId()).orElse(null);
		if (contact == null) {
			throw new IllegalStateException("Account owner contact unavailable");
		}
		return contact;
	}
}
