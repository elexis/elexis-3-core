package ch.elexis.core.mail.internal.account;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.MailClientRights;
import ch.elexis.core.mail.MailEventTopics;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.model.ServerEndpoint;
import ch.elexis.core.mail.model.SentCopyDetected;
import ch.elexis.core.mail.service.IMailboxAccountService;
import ch.elexis.core.mail.service.MailAccessException;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/** Stores account definitions and secrets for the active Elexis user without opening mail connections. */
@Singleton
@Component(service = IMailboxAccountService.class)
public class MailboxAccountService implements IMailboxAccountService {
	@Inject
	@Reference
	private IConfigService configService;
	@Inject
	@Reference
	private IUserService userService;
	@Inject
	@Reference
	private IAccessControlService accessControlService;
	@Inject
	@Reference
	private IContextService contextService;
	@Inject
	@Reference(target = "(service.model.name=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
	private volatile EventAdmin eventAdmin;
	private final Logger log = LoggerFactory.getLogger(getClass());

	private final Gson gson = new Gson();
	private MailAccessPolicy policy;
	private CredentialCipher cipher;

	@PostConstruct
	@Activate
	void activate() {
		policy = new MailAccessPolicy(userService::getUserGroups,
				user -> user.getId().equals(contextService.getActiveUserId())
						&& accessControlService.evaluate(EvACE.of(MailClientRights.MAIL_CLIENT_MANAGE_ACCOUNTS)));
		cipher = new CredentialCipher(() -> configService.get(MailClientConfig.SECRET_SALT, null),
				salt -> configService.set(MailClientConfig.SECRET_SALT, salt, false));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized List<MailboxAccount> getVisibleAccounts(IUser user) {
		return accountIds().stream().map(this::getAccount).flatMap(Optional::stream)
				.filter(account -> policy.canSee(user, account)).toList();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized List<MailboxAccount> getManageableAccounts(IUser user) {
		checkActiveUser(user);
		return accountIds().stream().map(this::getAccount).flatMap(Optional::stream)
				.filter(account -> policy.canManage(user, account)).toList();
	}

	/** {@inheritDoc} */
	@Override
	public boolean canManageShared(IUser user) {
		return user != null && user.getId().equals(contextService.getActiveUserId())
				&& accessControlService.evaluate(EvACE.of(MailClientRights.MAIL_CLIENT_MANAGE_ACCOUNTS));
	}

	/** {@inheritDoc} */
	@Override
	public synchronized boolean hasCredentials(IUser user, String accountId) {
		MailboxAccount account = getAccount(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Unknown mail account"));
		policy.checkSee(user, account);
		String key = account.authMode() == AuthMode.OAUTH2 ? MailClientConfig.refreshToken(accountId)
				: MailClientConfig.password(accountId);
		String stored;
		if (account.scope() == MailboxScope.PERSONAL) {
			IUser owner = modelService.load(account.ownerUserId(), IUser.class)
					.orElseThrow(() -> new IllegalStateException("Account owner unavailable"));
			stored = configService.get(ownerContact(owner), key, null);
		} else {
			stored = configService.get(key, null);
		}
		return stored != null && !stored.isEmpty();
	}

	/** {@inheritDoc} */
	@Override
	public Optional<MailboxAccount> getAccount(String accountId) {
		String json = configService.get(MailClientConfig.account(accountId), null);
		return Optional.ofNullable(json).map(value -> gson.fromJson(value, MailboxAccount.class));
	}

	/**
	 * {@inheritDoc}
	 * @throws IllegalArgumentException if settings are invalid or the account is unknown
	 */
	@Override
	public synchronized MailboxAccount save(IUser user, MailboxAccount account) {
		checkActiveUser(user);
		Optional<MailboxAccount> existing = account.id() == null ? Optional.empty() : getAccount(account.id());
		MailboxAccount candidate = account;
		MailboxAccount previous = existing.orElse(null);
		if (previous != null) {
			policy.checkManage(user, previous);
			if (previous.scope() != account.scope()) {
				if (!canManageShared(user)) {
					throw new MailAccessException("Shared account management required for scope changes");
				}
				candidate = convertScope(user, previous, account);
			} else if (!Objects.equals(previous.ownerUserId(), account.ownerUserId())) {
				if (account.scope() != MailboxScope.PERSONAL) {
					throw new MailAccessException("Shared account ownership cannot be changed");
				}
				candidate = personalOwner(account, account.ownerUserId());
			}
		}
		if (previous == null) {
			policy.checkManage(user, candidate);
		}
		validate(candidate, candidate.id() != null && isLegacy(candidate.id()));
		if (account.id() != null && existing.isEmpty()) {
			throw new IllegalArgumentException("Unknown mail account");
		}
		MailboxAccount saved = candidate.id() == null ? withId(candidate, UUID.randomUUID().toString()) : candidate;
		if (previous != null && (previous.scope() != saved.scope()
				|| !Objects.equals(previous.ownerUserId(), saved.ownerUserId()))) {
			moveSecrets(previous, saved);
		}
		List<String> ids = accountIds();
		if (!ids.contains(saved.id())) {
			ids.add(saved.id());
		}
		if (existing.isEmpty() || existing.get().sentCopyMode() != saved.sentCopyMode()) {
			configService.set(MailClientConfig.sentCopyDetected(saved.id()), SentCopyDetected.UNKNOWN.name());
		}
		configService.set(MailClientConfig.account(saved.id()), gson.toJson(saved));
		configService.set(MailClientConfig.ACCOUNTS, String.join(",", ids));
		accountsChanged(saved.id());
		return saved;
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void remove(IUser user, String accountId) {
		checkActiveUser(user);
		Optional<MailboxAccount> loaded = getAccount(accountId);
		if (loaded.isEmpty()) {
			return;
		}
		MailboxAccount account = loaded.get();
		policy.checkManage(user, account);
		if (account.scope() == MailboxScope.PERSONAL) {
			modelService.load(account.ownerUserId(), IUser.class).map(this::ownerContact).ifPresent(owner -> {
				configService.set(owner, MailClientConfig.password(accountId), null);
				configService.set(owner, MailClientConfig.refreshToken(accountId), null);
				configService.set(owner, MailClientConfig.signedInAs(accountId), null);
			});
		}
		configService.set(MailClientConfig.password(accountId), null, false);
		configService.set(MailClientConfig.refreshToken(accountId), null, false);
		configService.set(MailClientConfig.signedInAs(accountId), null, false);
		configService.set(MailClientConfig.sentCopyDetected(accountId), null);
		configService.set(MailClientConfig.account(accountId), null);
		configService.set(MailClientConfig.legacy(accountId), null);
		configService.set(MailClientConfig.mandators(accountId), null);
		List<String> ids = accountIds();
		ids.remove(accountId);
		configService.set(MailClientConfig.ACCOUNTS, String.join(",", ids));
		accountsChanged(accountId);
	}

	/**
	 * {@inheritDoc}
	 * @throws IllegalStateException if encryption fails or the owner has no assigned contact
	 * @throws IllegalArgumentException if the account does not exist
	 */
	@Override
	public synchronized void setPassword(IUser user, String accountId, char[] password) {
		checkActiveUser(user);
		MailboxAccount account = getAccount(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Unknown mail account"));
		policy.checkManage(user, account);
		IContact owner = account.scope() == MailboxScope.PERSONAL ? accountOwnerContact(account) : null;
		try {
			String encrypted = cipher.encrypt(password);
			if (account.scope() == MailboxScope.PERSONAL) {
				configService.set(owner, MailClientConfig.password(accountId), encrypted);
			} else {
				configService.set(MailClientConfig.password(accountId), encrypted, false);
			}
			accountsChanged(accountId);
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException("Credential encryption failed", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public boolean isLegacy(String accountId) {
		return accountId != null && Boolean.parseBoolean(configService.get(MailClientConfig.legacy(accountId), "false"));
	}

	/** {@inheritDoc} */
	@Override
	public List<String> getMandatorIds(String accountId) {
		return Arrays.stream(configService.get(MailClientConfig.mandators(accountId), "").split(","))
				.filter(id -> !id.isBlank()).toList();
	}

	/** {@inheritDoc} */
	@Override
	public synchronized void setMandatorIds(IUser user, String accountId, List<String> mandatorIds) {
		checkActiveUser(user);
		MailboxAccount account = getAccount(accountId)
				.orElseThrow(() -> new IllegalArgumentException("Unknown mail account"));
		policy.checkManage(user, account);
		configService.set(MailClientConfig.mandators(accountId), String.join(",", mandatorIds));
		accountsChanged(accountId);
	}

	private MailboxAccount convertScope(IUser user, MailboxAccount previous, MailboxAccount requested) {
		List<String> delegates = List.of();
		List<String> users = List.of();
		List<String> groups = List.of();
		String owner = null;
		if (requested.scope() == MailboxScope.SHARED) {
			LinkedHashSet<String> visible = new LinkedHashSet<>();
			visible.add(Objects.requireNonNull(previous.ownerUserId(), "Account owner required"));
			visible.addAll(previous.delegateUserIds());
			users = requested.visibleUserIds().isEmpty() ? List.copyOf(visible) : requested.visibleUserIds();
			groups = requested.visibleGroupIds();
		} else {
			owner = requested.ownerUserId() == null ? user.getId() : requested.ownerUserId();
			delegates = requested.delegateUserIds().isEmpty() ? previous.visibleUserIds() : requested.delegateUserIds();
		}
		MailboxAccount converted = new MailboxAccount(requested.id(), requested.displayName(), requested.emailAddress(),
				requested.provider(), requested.scope(), owner, delegates, users, groups, requested.imap(),
				requested.smtp(), requested.username(), requested.authMode(), requested.oauthSharedMailbox(),
				requested.signatureHtml(), requested.sentCopyMode(), requested.connectionBudget(),
				requested.pollIntervalSeconds());
		return requested.scope() == MailboxScope.PERSONAL ? personalOwner(converted, owner) : converted;
	}

	private MailboxAccount personalOwner(MailboxAccount a, String owner) {
		transferContact(owner);
		return new MailboxAccount(a.id(), a.displayName(), a.emailAddress(), a.provider(), MailboxScope.PERSONAL,
				owner, a.delegateUserIds().stream().filter(id -> !id.equals(owner)).distinct().toList(),
				List.of(), List.of(), a.imap(), a.smtp(), a.username(), a.authMode(), a.oauthSharedMailbox(),
				a.signatureHtml(), a.sentCopyMode(), a.connectionBudget(), a.pollIntervalSeconds());
	}

	private IContact transferContact(String ownerId) {
		if (ownerId == null) {
			throw new IllegalArgumentException("Account owner required");
		}
		IUser owner = modelService.load(ownerId, IUser.class)
				.orElseThrow(() -> new IllegalArgumentException("Account owner unavailable"));
		IContact contact = modelService.load(owner.getAssociatedContactId(), IContact.class).orElse(null);
		if (contact == null) {
			throw new IllegalArgumentException("Account owner contact required");
		}
		return contact;
	}

	private void moveSecrets(MailboxAccount previous, MailboxAccount saved) {
		IContact source = previous.scope() == MailboxScope.PERSONAL
				? modelService.load(previous.ownerUserId(), IUser.class).map(this::ownerContact).orElse(null) : null;
		if (previous.scope() == MailboxScope.PERSONAL && source == null) {
			return;
		}
		IContact destination = saved.scope() == MailboxScope.PERSONAL ? transferContact(saved.ownerUserId()) : null;
		if (source != null && destination != null && source.getId().equals(destination.getId())) {
			return;
		}
		for (String key : List.of(MailClientConfig.password(saved.id()), MailClientConfig.refreshToken(saved.id()),
				MailClientConfig.signedInAs(saved.id()))) {
			String value = source != null ? configService.get(source, key, null) : configService.get(key, null);
			if (value != null) {
				if (destination != null) {
					configService.set(destination, key, value);
				} else {
					configService.set(key, value, false);
				}
				if (source != null) {
					configService.set(source, key, null);
				} else {
					configService.set(key, null, false);
				}
			}
		}
	}

	private void accountsChanged(String accountId) {
		EventAdmin events = eventAdmin;
		if (events != null) {
			try {
				events.postEvent(new Event(MailEventTopics.ACCOUNTS, Map.of(MailEventTopics.ACCOUNT_ID, accountId)));
			} catch (IllegalStateException e) {
				log.warn("Mail account event service is unavailable");
			}
		}
	}

	private void checkActiveUser(IUser user) {
		if (user == null || !user.getId().equals(contextService.getActiveUserId())) {
			throw new MailAccessException("Active mail user required");
		}
	}

	private IContact accountOwnerContact(MailboxAccount account) {
		return ownerContact(modelService.load(account.ownerUserId(), IUser.class)
				.orElseThrow(() -> new IllegalStateException("Account owner unavailable")));
	}

	private IContact ownerContact(IUser user) {
		IContact contact = modelService.load(user.getAssociatedContactId(), IContact.class).orElse(null);
		if (contact == null) {
			throw new IllegalStateException("Account owner contact required");
		}
		return contact;
	}

	private List<String> accountIds() {
		String configured = configService.get(MailClientConfig.ACCOUNTS, "");
		return configured == null || configured.isEmpty() ? new ArrayList<>()
				: new ArrayList<>(Arrays.asList(configured.split(",")));
	}

	static void validate(MailboxAccount account) {
		validate(account, false);
	}

	static void validate(MailboxAccount account, boolean legacy) {
		Objects.requireNonNull(account.scope(), "Account scope required");
		Objects.requireNonNull(account.delegateUserIds(), "Account delegates required");
		Objects.requireNonNull(account.visibleUserIds(), "Account visibility required");
		Objects.requireNonNull(account.visibleGroupIds(), "Account visibility required");
		if (account.imap() == null && account.smtp() == null) {
			throw new IllegalArgumentException("At least one mail server required");
		}
		if (account.id() != null) {
			MailClientConfig.mandators(account.id());
		}
		validateEndpoint(account.imap(), legacy);
		validateEndpoint(account.smtp(), legacy);
		if (account.connectionBudget() < 1 || account.connectionBudget() > 3) {
			throw new IllegalArgumentException("Connection budget must be between 1 and 3");
		}
		if (account.pollIntervalSeconds() < 60 || account.pollIntervalSeconds() > 900) {
			throw new IllegalArgumentException("Poll interval must be between 60 and 900 seconds");
		}
	}

	private static void validateEndpoint(ServerEndpoint endpoint, boolean legacy) {
		if (endpoint == null) {
			return;
		}
		Objects.requireNonNull(endpoint.security(), "Mail security required");
		if (!legacy && endpoint.security() == MailSecurity.NONE && !"127.0.0.1".equals(endpoint.host())) {
			throw new IllegalArgumentException("Unencrypted mail requires IPv4 loopback");
		}
	}

	private MailboxAccount withId(MailboxAccount a, String id) {
		return new MailboxAccount(id, a.displayName(), a.emailAddress(), a.provider(), a.scope(), a.ownerUserId(),
				List.copyOf(a.delegateUserIds()), List.copyOf(a.visibleUserIds()), List.copyOf(a.visibleGroupIds()),
				a.imap(), a.smtp(), a.username(), a.authMode(), a.oauthSharedMailbox(), a.signatureHtml(),
				a.sentCopyMode(), a.connectionBudget(), a.pollIntervalSeconds());
	}
}
