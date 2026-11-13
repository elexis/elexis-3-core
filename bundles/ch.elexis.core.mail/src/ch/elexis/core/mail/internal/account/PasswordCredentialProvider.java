package ch.elexis.core.mail.internal.account;

import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.function.Function;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;

public final class PasswordCredentialProvider {
	private final IConfigService configService;
	private final CredentialCipher cipher;
	private final Function<String, Optional<IUser>> findOwner;
	private final Function<String, Optional<IContact>> findContact;

	public PasswordCredentialProvider(IConfigService configService, CredentialCipher cipher,
			Function<String, Optional<IUser>> findOwner, Function<String, Optional<IContact>> findContact) {
		this.configService = configService;
		this.cipher = cipher;
		this.findOwner = findOwner;
		this.findContact = findContact;
	}

	Optional<char[]> getPassword(MailboxAccount account) throws GeneralSecurityException {
		String key = MailClientConfig.password(account.id());
		String encrypted;
		if (account.scope() == MailboxScope.PERSONAL) {
			IUser owner = findOwner.apply(account.ownerUserId())
					.orElseThrow(() -> new GeneralSecurityException("Account owner unavailable"));
			IContact contact = findContact.apply(owner.getAssociatedContactId()).orElse(null);
			if (contact == null) {
				throw new GeneralSecurityException("Account owner contact unavailable");
			}
			encrypted = configService.get(contact, key, null);
		} else {
			encrypted = configService.get(key, null);
		}
		return encrypted == null || encrypted.isEmpty() ? Optional.empty() : Optional.of(cipher.decrypt(encrypted));
	}
}
