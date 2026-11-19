package ch.elexis.core.mail.internal.oauth;

import java.security.GeneralSecurityException;
import java.time.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.mail.MailEventTopics;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.service.MailOAuthException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;

/** Serializes refreshes and keeps access tokens and sign-in-required marks only in memory. */
@Singleton
@Component(service = { OAuth2TokenCache.class, EventHandler.class },
		property = "event.topics=" + MailEventTopics.ACCOUNTS)
public class OAuth2TokenCache implements EventHandler {
	@Inject
	@Reference
	private IConfigService configService;
	@Inject
	@Reference(target = "(service.model.name=ch.elexis.core.model)")
	private IModelService modelService;
	private final Map<String, Entry> entries = new HashMap<>();
	private final Map<String, Boolean> required = new HashMap<>();
	private Clock clock = Clock.systemUTC();
	private OAuth2AccountStore store;
	private OAuth2Client client;

	@PostConstruct
	@Activate
	void activate() {
		store = new OAuth2AccountStore(configService, id -> modelService.load(id, IUser.class),
				id -> modelService.load(id, IContact.class));
		client = new OAuth2Client();
	}

	/**
	 * Returns the account store initialized during activation.
	 * @return the account store, or null before activation
	 */
	public OAuth2AccountStore store() {
		return store;
	}

	OAuth2Client client() {
		return client;
	}

	/**
	 * Returns a reusable cached access token or refreshes it, persisting a rotated refresh token when supplied.
	 * @param account the account requiring an access token
	 * @return the access token
	 * @throws MessagingException if sign-in is required, credentials cannot be decrypted or token retrieval fails
	 */
	public synchronized String accessToken(MailboxAccount account) throws MessagingException {
		if (required.containsKey(account.id())) {
			throw new AuthenticationFailedException("Mail OAuth sign-in is required");
		}
		try {
			String refresh = store.refreshToken(account);
			if (refresh.isEmpty()) {
				throw new AuthenticationFailedException("Mail OAuth sign-in is required");
			}
			Entry cached = entries.get(account.id());
			if (cached != null && cached.refresh().equals(refresh)
					&& clock.instant().isBefore(cached.expires().minusSeconds(300))) {
				return cached.access();
			}
			OAuth2Client.Token token = client.request(account.provider(),
					Map.of("grant_type", "refresh_token", "refresh_token", refresh, "scope", client.scope(account.provider())));
			if (!token.refresh().isEmpty() && !token.refresh().equals(refresh)) {
				store.refreshToken(account, token.refresh());
				refresh = token.refresh();
			}
			entries.put(account.id(), new Entry(token.access(), refresh, clock.instant().plusSeconds(token.expiresIn())));
			return token.access();
		} catch (OAuth2Client.TokenFailure e) {
			if (e.signInRequired()) {
				required.put(account.id(), true);
				entries.remove(account.id());
				throw new AuthenticationFailedException("Mail OAuth sign-in is required");
			}
			throw new MessagingException("Mail OAuth token request failed");
		} catch (MailOAuthException e) {
			throw new MessagingException("Mail OAuth token endpoint is unavailable");
		} catch (GeneralSecurityException e) {
			throw new AuthenticationFailedException("Mail OAuth credentials cannot be decrypted");
		}
	}

	/**
	 * Removes the cached access token while preserving any sign-in-required mark.
	 * @param accountId the account id
	 */
	public synchronized void invalidate(String accountId) {
		entries.remove(accountId);
	}

	/**
	 * Removes the cached access token and any sign-in-required mark.
	 * @param accountId the account id
	 */
	public synchronized void clear(String accountId) {
		entries.remove(accountId);
		required.remove(accountId);
	}

	/**
	 * Drops access tokens and the sign-in-required mark after account changes.
	 * @param event the asynchronous account event
	 */
	@Override
	public void handleEvent(Event event) {
		Object id = event.getProperty(MailEventTopics.ACCOUNT_ID);
		if (MailEventTopics.ACCOUNTS.equals(event.getTopic()) && id instanceof String accountId) {
			clear(accountId);
		}
	}

	@PreDestroy
	@Deactivate
	synchronized void deactivate() {
		entries.clear();
		required.clear();
		if (client != null) {
			client.close();
		}
	}

	private record Entry(String access, String refresh, Instant expires) {
	}
}
