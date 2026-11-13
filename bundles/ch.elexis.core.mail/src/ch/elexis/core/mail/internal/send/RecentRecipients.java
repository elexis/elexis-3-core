package ch.elexis.core.mail.internal.send;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.model.MailAddress;
import ch.elexis.core.mail.service.MailAccessException;

public final class RecentRecipients {
	private final IConfigService config;
	private final Supplier<Optional<IUser>> activeUser;

	/**
	 * Creates a recent recipient store using contact configuration and an active user supplier.
	 * @param config the configuration service
	 * @param activeUser the supplier of the active user
	 */
	public RecentRecipients(IConfigService config, Supplier<Optional<IUser>> activeUser) {
		this.config = config;
		this.activeUser = activeUser;
	}

	/**
	 * Reads up to 200 distinct nonempty recipient addresses in lowercase for the active user.
	 * @param user the user who must be active
	 * @return the stored recipient addresses
	 */
	public synchronized List<String> read(IUser user) {
		checkActive(user);
		LinkedHashSet<String> addresses = new LinkedHashSet<>();
		for (String value : config.getActiveUserContact(MailClientConfig.RECENT_RECIPIENTS, "").split(",")) {
			String address = value.trim().toLowerCase(Locale.ROOT);
			if (!address.isEmpty()) {
				addresses.add(address);
			}
		}
		return addresses.stream().limit(200).toList();
	}

	/**
	 * Persists up to 200 distinct lowercase recipient addresses with the supplied recipients first.
	 * @param user the user who must be active
	 * @param recipients the recipients to place before previously stored addresses
	 */
	public synchronized void remember(IUser user, List<MailAddress> recipients) {
		List<String> previous = read(user);
		LinkedHashSet<String> updated = new LinkedHashSet<>();
		for (MailAddress recipient : recipients) {
			updated.add(recipient.address().trim().toLowerCase(Locale.ROOT));
		}
		updated.addAll(previous);
		List<String> bounded = new ArrayList<>(updated.stream().limit(200).toList());
		checkActive(user);
		if (!bounded.equals(previous)
				&& !config.setActiveUserContact(MailClientConfig.RECENT_RECIPIENTS, String.join(",", bounded))) {
			throw new IllegalStateException("Recent recipients could not be persisted");
		}
	}

	/**
	 * Rejects access unless the supplied user matches the active user.
	 * @param user the user to check
	 */
	public void checkActive(IUser user) {
		if (user == null || activeUser.get().filter(active -> active.getId().equals(user.getId())).isEmpty()) {
			throw new MailAccessException("Recent recipient access denied");
		}
	}
}
