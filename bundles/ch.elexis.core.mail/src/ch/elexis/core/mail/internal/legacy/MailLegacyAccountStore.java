package ch.elexis.core.mail.internal.legacy;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.google.gson.Gson;

import ch.elexis.core.mail.MailAccount;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.MailEventTopics;
import ch.elexis.core.mail.internal.account.CredentialCipher;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.model.SentCopyDetected;
import ch.elexis.core.services.IConfigService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/** Persists shared legacy accounts for migration and the trusted compatibility facade. */
@Singleton
@Component(service = MailLegacyAccountStore.class)
public class MailLegacyAccountStore {
	@Inject
	@Reference
	private IConfigService configService;
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	private volatile EventAdmin eventAdmin;
	private final Gson gson = new Gson();

	/**
	 * Saves a shared legacy account and encrypts an explicitly supplied password.
	 * @param legacy the account to convert
	 * @throws IllegalArgumentException for invalid ids or a personal target account
	 * @throws IllegalStateException if encryption fails
	 */
	public synchronized void save(MailAccount legacy) {
		String id = legacy.getId();
		if (id == null || id.isBlank() || id.contains(",")) {
			throw new IllegalArgumentException("Invalid legacy mail account id");
		}
		MailClientConfig.mandators(id);
		if (MailClientConfig.cleanupTrashMarks(id).length() > 80) {
			throw new IllegalArgumentException("Mail configuration key exceeds 80 characters");
		}
		checkShared(id);
		MailboxAccount account = MailLegacyAccountConverter.toMailbox(legacy);
		if (legacy.getPassword() != null) {
			char[] password = legacy.getPassword().toCharArray();
			try {
				CredentialCipher cipher = new CredentialCipher(() -> configService.get(MailClientConfig.SECRET_SALT, null),
						salt -> configService.set(MailClientConfig.SECRET_SALT, salt, false));
				configService.set(MailClientConfig.password(id), cipher.encrypt(password), false);
			} catch (GeneralSecurityException e) {
				throw new IllegalStateException("Legacy mail credential encryption failed", e);
			} finally {
				Arrays.fill(password, '\0');
			}
		}
		configService.set(MailClientConfig.legacy(id), "true");
		configService.set(MailClientConfig.mandators(id), String.join(",", MailLegacyAccountConverter.mandatorIds(legacy)));
		configService.set(MailClientConfig.sentCopyDetected(id), SentCopyDetected.UNKNOWN.name());
		configService.set(MailClientConfig.account(id), gson.toJson(account));
		List<String> ids = ids();
		if (!ids.contains(id)) {
			ids.add(id);
		}
		configService.set(MailClientConfig.ACCOUNTS, String.join(",", ids));
		changed(id);
	}

	/**
	 * Removes a shared account while retaining all old configuration keys.
	 * @param id the shared account id
	 * @throws IllegalArgumentException if the target account is personal
	 */
	public synchronized void remove(String id) {
		checkShared(id);
		for (String key : List.of(MailClientConfig.account(id), MailClientConfig.password(id),
				MailClientConfig.refreshToken(id), MailClientConfig.signedInAs(id), MailClientConfig.sentCopyDetected(id),
				MailClientConfig.legacy(id), MailClientConfig.mandators(id))) {
			configService.set(key, null, false);
		}
		List<String> ids = ids();
		ids.remove(id);
		configService.set(MailClientConfig.ACCOUNTS, String.join(",", ids));
		changed(id);
	}

	private void checkShared(String id) {
		String previous = configService.get(MailClientConfig.account(id), null);
		if (previous != null && gson.fromJson(previous, MailboxAccount.class).scope() != MailboxScope.SHARED) {
			throw new IllegalArgumentException("Shared mail account required");
		}
	}

	private List<String> ids() {
		return new ArrayList<>(Arrays.stream(configService.get(MailClientConfig.ACCOUNTS, "").split(","))
				.filter(id -> !id.isBlank()).toList());
	}

	private void changed(String id) {
		EventAdmin events = eventAdmin;
		if (events != null) {
			events.postEvent(new Event(MailEventTopics.ACCOUNTS, Map.of(MailEventTopics.ACCOUNT_ID, id)));
		}
	}
}
