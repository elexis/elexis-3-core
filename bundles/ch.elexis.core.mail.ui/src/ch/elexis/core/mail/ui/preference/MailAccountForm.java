package ch.elexis.core.mail.ui.preference;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import ch.elexis.core.mail.MailAccountPresets;
import ch.elexis.core.mail.html.OutgoingHtmlFilter;
import ch.elexis.core.mail.model.AuthMode;
import ch.elexis.core.mail.model.MailProvider;
import ch.elexis.core.mail.model.MailboxAccount;
import ch.elexis.core.mail.model.MailboxScope;
import ch.elexis.core.mail.model.MailSecurity;
import ch.elexis.core.mail.model.SentCopyMode;
import ch.elexis.core.mail.model.ServerEndpoint;

/**
 * Holds account edits independently of SWT, preserving explicitly edited server
 * fields.
 */
public final class MailAccountForm {

	/** Editable scalar fields, with enum values represented by their names. */
	public enum Field {
		ADDRESS, NAME, USERNAME, IMAP_HOST, IMAP_PORT, IMAP_SECURITY, SMTP_HOST, SMTP_PORT, SMTP_SECURITY, SENT_COPY,
		BUDGET, POLL, SIGNATURE
	}

	private final Map<Field, String> values = new EnumMap<>(Field.class);
	private final EnumSet<Field> edited = EnumSet.noneOf(Field.class);
	private final MailboxAccount original;
	private final String activeUserId;
	private final boolean accountManager;
	private String savedOwner;
	private String id;
	private MailProvider provider;
	private MailboxScope scope;
	private String owner;
	private List<String> delegates;
	private List<String> users;
	private List<String> groups;
	private AuthMode auth;
	private boolean sharedMailbox;
	private boolean groupsDropped;

	/**
	 * Loads a saved account or the initial provider preset.
	 * 
	 * @param account existing account, or null
	 * @param userId  active user's id
	 */
	public MailAccountForm(MailboxAccount account, String userId) {
		this(account, userId, false);
	}

	/**
	 * Loads account edits with the current management permission.
	 * @param account existing account, or null
	 * @param userId active user id
	 * @param accountManager whether all accounts may be managed
	 */
	public MailAccountForm(MailboxAccount account, String userId, boolean accountManager) {
		this.accountManager = accountManager;
		original = account;
		activeUserId = userId;
		MailboxAccount initial = account == null
				? MailAccountPresets.preset(MailProvider.IMAP, "", MailboxScope.PERSONAL, userId) //$NON-NLS-1$
				: account;
		id = initial.id();
		scope = initial.scope();
		owner = initial.ownerUserId();
		savedOwner = owner;
		delegates = initial.delegateUserIds();
		users = initial.visibleUserIds();
		groups = initial.visibleGroupIds();
		provider = initial.provider();
		auth = initial.authMode();
		sharedMailbox = initial.oauthSharedMailbox();
		fill(initial);
		if (account != null) {
			Map<Field, String> configured = new EnumMap<>(values);
			fill(MailAccountPresets.preset(provider, initial.emailAddress(), scope, owner));
			for (Field field : Field.values()) {
				if (!Objects.equals(configured.get(field), values.get(field))) {
					edited.add(field);
				}
			}
			values.putAll(configured);
		}
	}

	private void fill(MailboxAccount account) {
		put(Field.ADDRESS, account.emailAddress());
		put(Field.NAME, account.displayName());
		put(Field.USERNAME, account.username());
		MailboxAccount defaults = MailAccountPresets.preset(account.provider(), account.emailAddress(), account.scope(),
				account.ownerUserId());
		ServerEndpoint imap = account.imap() == null ? defaults.imap() : account.imap();
		ServerEndpoint smtp = account.smtp() == null ? defaults.smtp() : account.smtp();
		put(Field.IMAP_HOST, account.imap() == null ? "" : imap.host()); //$NON-NLS-1$
		put(Field.IMAP_PORT, Integer.toString(imap.port()));
		put(Field.IMAP_SECURITY, imap.security().name());
		put(Field.SMTP_HOST, account.smtp() == null ? "" : smtp.host()); //$NON-NLS-1$
		put(Field.SMTP_PORT, Integer.toString(smtp.port()));
		put(Field.SMTP_SECURITY, smtp.security().name());
		put(Field.SENT_COPY, account.sentCopyMode().name());
		put(Field.BUDGET, Integer.toString(account.connectionBudget()));
		put(Field.POLL, Integer.toString(account.pollIntervalSeconds()));
		put(Field.SIGNATURE, account.signatureHtml());
	}

	private void put(Field field, String value) {
		if (!edited.contains(field)) {
			values.put(field, Objects.toString(value, "")); //$NON-NLS-1$
		}
	}

	/**
	 * Reads a scalar field.
	 * 
	 * @param field field to read
	 * @return non-null value
	 */
	public String get(Field field) {
		return values.getOrDefault(field, ""); //$NON-NLS-1$
	}

	/**
	 * Marks a field as explicitly edited, even when it matches a previous preset.
	 * 
	 * @param field edited field
	 * @param value new value
	 */
	public void set(Field field, String value) {
		values.put(field, Objects.requireNonNull(value));
		edited.add(field);
		if (field == Field.ADDRESS) {
			String imapHost = get(Field.IMAP_HOST);
			String smtpHost = get(Field.SMTP_HOST);
			fill(MailAccountPresets.preset(provider, value, scope, owner));
			if (original != null && original.imap() == null && imapHost.isBlank()) {
				values.put(Field.IMAP_HOST, imapHost);
			}
			if (original != null && original.smtp() == null && smtpHost.isBlank()) {
				values.put(Field.SMTP_HOST, smtpHost);
			}
		}
	}

	/**
	 * Applies a provider preset only to fields not explicitly edited.
	 * 
	 * @param value           selected provider
	 * @param googleAvailable whether Google sign-in is configured
	 */
	public void provider(MailProvider value, boolean googleAvailable) {
		provider = value;
		MailboxAccount preset = MailAccountPresets.preset(value, get(Field.ADDRESS), scope, owner);
		fill(preset);
		auth = value == MailProvider.GOOGLE && !googleAvailable ? AuthMode.PASSWORD : preset.authMode();
		sharedMailbox = value == MailProvider.M365 && sharedMailbox;
	}

	/** @return selected provider */
	public MailProvider provider() {
		return provider;
	}

	/** @return selected authentication mode */
	public AuthMode auth() {
		return auth;
	}

	/** @return selected scope */
	public MailboxScope scope() {
		return scope;
	}

	/** @return persisted account id, or null */
	public String id() {
		return id;
	}

	/** @param value saved account id */
	public void saved(String value) {
		id = value;
		savedOwner = owner;
	}

	/** @param value selected authentication mode */
	public void auth(AuthMode value) {
		auth = value;
	}

	/** @param value whether this is a Microsoft shared mailbox */
	public void sharedMailbox(boolean value) {
		sharedMailbox = value;
	}

	/** @return whether this is a Microsoft shared mailbox */
	public boolean sharedMailbox() {
		return sharedMailbox;
	}

	/**
	 * Sets ACL lists without allowing saved accounts to change scope.
	 * 
	 * @param value       selected scope
	 * @param userId      active user id for new personal accounts
	 * @param delegateIds personal delegates
	 * @param userIds     shared visibility users
	 * @param groupIds    shared visibility groups
	 */
	public void access(MailboxScope value, String userId, List<String> delegateIds, List<String> userIds,
			List<String> groupIds) {
		access(value, userId, delegateIds, userIds, groupIds, false);
	}

	/**
	 * Sets access lists and converts them when an authorized scope change is requested.
	 * @param value selected scope
	 * @param userId active user id
	 * @param delegateIds personal delegates
	 * @param userIds shared visibility users
	 * @param groupIds shared visibility groups
	 * @param canManageShared whether the active user has shared management rights
	 */
	public void access(MailboxScope value, String userId, List<String> delegateIds, List<String> userIds,
			List<String> groupIds, boolean canManageShared) {
		delegates = List.copyOf(delegateIds);
		users = List.copyOf(userIds);
		groups = List.copyOf(groupIds);
		if (value != scope && canChangeScope(canManageShared)) {
			groupsDropped = value == MailboxScope.PERSONAL && !groups.isEmpty();
			if (value == MailboxScope.SHARED) {
				LinkedHashSet<String> visible = new LinkedHashSet<>();
				visible.add(Objects.requireNonNull(owner));
				visible.addAll(delegates);
				if (users.isEmpty()) {
					users = List.copyOf(visible);
				}
				delegates = List.of();
				owner = null;
			} else {
				if (delegates.isEmpty()) {
					delegates = users.stream().filter(id -> !id.equals(userId)).distinct().toList();
				}
				users = List.of();
				groups = List.of();
				owner = userId;
			}
			scope = value;
		}
	}

	/**
	 * Checks whether scope controls may be offered.
	 * @param canManageShared whether shared management is authorized
	 * @return whether the form permits scope changes
	 */
	public boolean canChangeScope(boolean canManageShared) {
		return original == null && id == null || canManageShared;
	}

	/** @return selected personal owner, or null for shared accounts */
	public String owner() {
		return owner;
	}

	/** @return whether saving hands the personal account to another user */
	public boolean ownerChanged() {
		return scope == MailboxScope.PERSONAL && !Objects.equals(savedOwner, owner) && !activeUserId.equals(owner);
	}

	/** @return whether the active user may choose the personal owner */
	public boolean canChangeOwner() {
		return scope == MailboxScope.PERSONAL && (original == null ? id != null
				: accountManager || original.scope() == MailboxScope.SHARED
						|| activeUserId.equals(original.ownerUserId()));
	}

	/**
	 * Selects an owner and removes that user from the delegates.
	 * @param value selected user id
	 */
	public void owner(String value) {
		if (canChangeOwner()) {
			owner = Objects.requireNonNull(value);
			delegates = delegates.stream().filter(id -> !id.equals(value)).distinct().toList();
		}
	}

	/** @return whether authentication actions keep the original and selected personal owner */
	public boolean canAuthenticate() {
		return (original == null || original.scope() == MailboxScope.SHARED
				|| activeUserId.equals(original.ownerUserId()))
				&& (scope == MailboxScope.SHARED || activeUserId.equals(owner)) && !ownerChanged();
	}

	/**
	 * Resolves the owner column without SWT or secret access.
	 * @param account account to label
	 * @param userLabel resolves a user id to its label, or null if unavailable
	 * @param sharedLabel localized shared account label
	 * @return shared label or personal owner label, falling back to the user id
	 */
	public static String ownerLabel(MailboxAccount account, Function<String, String> userLabel, String sharedLabel) {
		return account.scope() == MailboxScope.SHARED ? sharedLabel
				: Objects.toString(userLabel.apply(account.ownerUserId()), account.ownerUserId());
	}

	/** @return whether the last scope change discarded shared groups */
	public boolean groupsDropped() {
		return groupsDropped;
	}

	/** @return personal delegate ids */
	public List<String> delegates() {
		return delegates;
	}

	/** @return shared visibility user ids */
	public List<String> visibleUsers() {
		return users;
	}

	/** @return shared visibility group ids */
	public List<String> visibleGroups() {
		return groups;
	}

	/**
	 * Validates local fields without accessing a server.
	 * 
	 * @return localization key, or null when valid
	 */
	public String validationKey() {
		if (get(Field.ADDRESS).isBlank() || !get(Field.ADDRESS).contains("@")) { //$NON-NLS-1$
			return "MailSettings_InvalidAddress"; //$NON-NLS-1$
		}
		if (original == null && (get(Field.IMAP_HOST).isBlank() || get(Field.SMTP_HOST).isBlank())
				|| get(Field.IMAP_HOST).isBlank() && get(Field.SMTP_HOST).isBlank()) {
			return "MailSettings_InvalidHost"; //$NON-NLS-1$
		}
		if (!get(Field.IMAP_HOST).isBlank() && !range(Field.IMAP_PORT, 1, 65535)
				|| !get(Field.SMTP_HOST).isBlank() && !range(Field.SMTP_PORT, 1, 65535)) {
			return "MailSettings_InvalidPort"; //$NON-NLS-1$
		}
		if (!range(Field.BUDGET, 1, 3) || !range(Field.POLL, 60, 900)) {
			return "MailSettings_InvalidLimits"; //$NON-NLS-1$
		}
		return null;
	}

	private boolean range(Field field, int min, int max) {
		try {
			int value = Integer.parseInt(get(field));
			return value >= min && value <= max;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	/**
	 * Builds the account, filtering the rich text signature with the core allow
	 * list.
	 * 
	 * @return account ready for service validation and saving
	 * @throws IllegalArgumentException if local validation fails
	 */
	public MailboxAccount build() {
		String invalid = validationKey();
		if (invalid != null) {
			throw new IllegalArgumentException(invalid);
		}
		return new MailboxAccount(id, get(Field.NAME), get(Field.ADDRESS), provider, scope, owner,
				scope == MailboxScope.PERSONAL ? delegates : List.of(),
				scope == MailboxScope.SHARED ? users : List.of(), scope == MailboxScope.SHARED ? groups : List.of(),
				get(Field.IMAP_HOST).isBlank() ? null : new ServerEndpoint(get(Field.IMAP_HOST).strip(), Integer.parseInt(get(Field.IMAP_PORT)),
						MailSecurity.valueOf(get(Field.IMAP_SECURITY))),
				get(Field.SMTP_HOST).isBlank() ? null : new ServerEndpoint(get(Field.SMTP_HOST).strip(), Integer.parseInt(get(Field.SMTP_PORT)),
						MailSecurity.valueOf(get(Field.SMTP_SECURITY))),
				get(Field.USERNAME), auth, provider == MailProvider.M365 && sharedMailbox,
				new OutgoingHtmlFilter().filter(get(Field.SIGNATURE)), SentCopyMode.valueOf(get(Field.SENT_COPY)),
				Integer.parseInt(get(Field.BUDGET)), Integer.parseInt(get(Field.POLL)));
	}
}
