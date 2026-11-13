package ch.elexis.core.mail.internal.mailbox;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.mail.MailClientConfig;
import ch.elexis.core.mail.service.MailAccessException;

/** Stores a bounded ordered sender list only for the active Elexis user. */
public final class RemoteImageSenders {
	private final IConfigService config;
	private final Supplier<Optional<IUser>> activeUser;

	/**
	 * Creates the user-contact configuration adapter.
	 * @param config the configuration service
	 * @param activeUser supplies the current user for each operation
	 */
	public RemoteImageSenders(IConfigService config, Supplier<Optional<IUser>> activeUser) {
		this.config = config;
		this.activeUser = activeUser;
	}

	/**
	 * Checks a normalized sender; other users always have an empty list.
	 * @param user the requesting user
	 * @param address the sender address
	 * @return whether remote images are allowed
	 */
	public synchronized boolean contains(IUser user, String address) {
		return isActive(user) && read().contains(normalize(address));
	}

	/**
	 * Adds a sender, preserving insertion order and evicting the oldest beyond 500.
	 * @param user the requesting active user
	 * @param address the sender address
	 * @throws MailAccessException if the requester is not active
	 * @throws IllegalArgumentException if no single sender address is provided
	 * @throws IllegalStateException if persistence fails
	 */
	public synchronized void allow(IUser user, String address) {
		if (!isActive(user)) {
			throw new MailAccessException("Remote image preference access denied");
		}
		String sender = normalize(address);
		if (sender.isEmpty() || sender.contains(",")) {
			throw new IllegalArgumentException("A sender address is required");
		}
		List<String> senders = read();
		if (senders.contains(sender)) {
			return;
		}
		senders.add(sender);
		while (senders.size() > 500) {
			senders.removeFirst();
		}
		if (!config.setActiveUserContact(MailClientConfig.REMOTE_IMAGE_SENDERS, String.join(",", senders))) {
			throw new IllegalStateException("Remote image preference could not be persisted");
		}
	}

	private List<String> read() {
		String stored = config.getActiveUserContact(MailClientConfig.REMOTE_IMAGE_SENDERS, "");
		LinkedHashSet<String> senders = new LinkedHashSet<>();
		for (String address : stored.split(",")) {
			String normalized = normalize(address);
			if (!normalized.isEmpty()) {
				senders.add(normalized);
			}
		}
		List<String> result = new ArrayList<>(senders);
		return new ArrayList<>(result.subList(Math.max(0, result.size() - 500), result.size()));
	}

	private boolean isActive(IUser user) {
		return user != null && activeUser.get().map(active -> active.getId().equals(user.getId())).orElse(false);
	}

	private String normalize(String address) {
		return address == null ? "" : address.trim().toLowerCase(Locale.ROOT);
	}
}
