package ch.elexis.core.mail.internal.connection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.angus.mail.iap.Argument;
import org.eclipse.angus.mail.iap.Response;
import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.protocol.BODY;
import org.eclipse.angus.mail.imap.protocol.FetchResponse;
import org.eclipse.angus.mail.imap.protocol.INTERNALDATE;
import org.eclipse.angus.mail.imap.protocol.UID;

import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

/** Keeps bounded immutable date snapshots; network work is serialized by each account's connection worker. */
public final class MailDateIndex {

	private static final int MAX_FOLDERS = 32;
	private final Map<Key, Snapshot> snapshots = new LinkedHashMap<>(16, 0.75f, true);
	private final Map<Key, Partial> partials = new HashMap<>();

	public record Entry(long uid, long receivedMillis) {
	}

	public record Snapshot(long validity, List<Entry> entries, long highestUid) {
	}

	private record Key(String accountId, String folder) {
	}

	private record Partial(long validity, int count, Map<Long, Entry> entries, int fetched) {
	}

	boolean buildStep(String accountId, IMAPFolder folder, int chunk) throws MessagingException {
		Key key = new Key(accountId, folder.getFullName());
		long validity = folder.getUIDValidity();
		Partial partial;
		synchronized (snapshots) {
			partial = partials.remove(key);
		}
		if (partial == null || partial.validity() != validity) {
			partial = new Partial(validity, count(folder), new HashMap<>(), 0);
		}
		int count = partial.count();
		Map<Long, Entry> collected = partial.entries();
		int last = Math.min(count, partial.fetched() + chunk);
		if (last > partial.fetched()) {
			entries(folder, fetch(folder, "FETCH", (partial.fetched() + 1) + ":" + last, "UID INTERNALDATE"), 0)
					.forEach(entry -> collected.put(entry.uid(), entry));
		}
		if (last < count) {
			synchronized (snapshots) {
				partials.put(key, new Partial(validity, count, collected, last));
			}
			return false;
		}
		if (collected.size() == count) {
			put(key, sorted(validity, List.copyOf(collected.values())));
		}
		return true;
	}

	/**
	 * Checks whether a cached snapshot matches the folder UID validity.
	 * @param accountId the account id
	 * @param folder the folder to check
	 * @return whether a matching snapshot exists
	 * @throws MessagingException if the folder UID validity cannot be read
	 */
	public boolean ready(String accountId, IMAPFolder folder) throws MessagingException {
		synchronized (snapshots) {
			Snapshot snapshot = snapshots.get(new Key(accountId, folder.getFullName()));
			return snapshot != null && snapshot.validity() == folder.getUIDValidity();
		}
	}

	/**
	 * Returns a date-sorted snapshot, building it when absent or invalid and updating it when refresh is requested.
	 * @param accountId the account id
	 * @param folder the folder to index
	 * @param refresh whether to check for changes to a valid cached snapshot
	 * @return the snapshot sorted by received time and UID
	 * @throws MessagingException if folder data cannot be read
	 */
	public Snapshot snapshot(String accountId, IMAPFolder folder, boolean refresh) throws MessagingException {
		Key key = new Key(accountId, folder.getFullName());
		long validity = folder.getUIDValidity();
		Snapshot snapshot;
		synchronized (snapshots) {
			snapshot = snapshots.get(key);
		}
		if (snapshot == null || snapshot.validity() != validity) {
			snapshot = build(folder, validity);
		} else if (refresh) {
			int count = count(folder);
			long next = folder.getUIDNext();
			if (snapshot.entries().size() == count && next > 0 && next <= snapshot.highestUid() + 1) {
				return snapshot;
			}
			List<Entry> added = count == 0 ? List.of() : fetch(folder, snapshot.highestUid());
			if (added.isEmpty() && snapshot.entries().size() == count) {
				return snapshot;
			}
			List<Entry> entries = new ArrayList<>(snapshot.entries());
			entries.addAll(added);
			snapshot = entries.size() == count ? sorted(validity, entries) : build(folder, validity);
		} else {
			return snapshot;
		}
		put(key, snapshot);
		return snapshot;
	}

	private void put(Key key, Snapshot snapshot) {
		synchronized (snapshots) {
			partials.remove(key);
			snapshots.put(key, snapshot);
			if (snapshots.size() > MAX_FOLDERS) {
				snapshots.remove(snapshots.keySet().iterator().next());
			}
		}
	}

	/**
	 * Removes matching UIDs from a cached snapshot only when its UID validity matches.
	 * @param accountId the account id
	 * @param folder the full folder name
	 * @param validity the expected UID validity
	 * @param uids the UIDs to remove
	 */
	public void remove(String accountId, String folder, long validity, long[] uids) {
		Key key = new Key(accountId, folder);
		Set<Long> removed = Arrays.stream(uids).boxed().collect(Collectors.toSet());
		synchronized (snapshots) {
			Snapshot snapshot = snapshots.get(key);
			if (snapshot != null && snapshot.validity() == validity) {
				List<Entry> entries = snapshot.entries().stream().filter(entry -> !removed.contains(entry.uid())).toList();
				snapshots.put(key, new Snapshot(validity, entries, snapshot.highestUid()));
			}
		}
	}


	/**
	 * Discards completed and partial snapshots for a folder and its descendants.
	 * @param accountId the account id
	 * @param folder the full folder name
	 * @param separator the folder hierarchy separator
	 */
	public void removeFolder(String accountId, String folder, char separator) {
		synchronized (snapshots) {
			snapshots.keySet().removeIf(key -> key.accountId().equals(accountId)
					&& (key.folder().equals(folder) || key.folder().startsWith(folder + separator)));
			partials.keySet().removeIf(key -> key.accountId().equals(accountId)
					&& (key.folder().equals(folder) || key.folder().startsWith(folder + separator)));
		}
	}

	void removeAccount(String accountId) {
		synchronized (snapshots) {
			snapshots.keySet().removeIf(key -> key.accountId().equals(accountId));
			partials.keySet().removeIf(key -> key.accountId().equals(accountId));
		}
	}

	private int count(IMAPFolder folder) throws MessagingException {
		return folder instanceof MailReadFolder retained ? retained.liveCount() : folder.getMessageCount();
	}

	private Snapshot build(IMAPFolder folder, long validity) throws MessagingException {
		return sorted(validity, count(folder) == 0 ? List.of() : fetch(folder, 0));
	}

	private Snapshot sorted(long validity, List<Entry> entries) {
		List<Entry> sorted = entries.stream()
				.sorted(Comparator.comparingLong(Entry::receivedMillis).thenComparingLong(Entry::uid)).toList();
		return new Snapshot(validity, sorted, sorted.stream().mapToLong(Entry::uid).max().orElse(0));
	}

	private List<Entry> fetch(IMAPFolder folder, long highestUid) throws MessagingException {
		return entries(folder, fetch(folder, "UID FETCH", (highestUid + 1) + ":*", "UID INTERNALDATE"), highestUid);
	}

	private List<Entry> entries(IMAPFolder folder, Response[] responses, long highestUid) throws MessagingException {
		Map<Long, Entry> entries = new HashMap<>();
		List<Long> missingDates = new ArrayList<>();
		for (Response response : responses) {
			if (response instanceof FetchResponse fetched) {
				UID uid = fetched.getItem(UID.class);
				if (uid != null && uid.uid > highestUid) {
					INTERNALDATE internal = fetched.getItem(INTERNALDATE.class);
					Date received = internal == null ? null : internal.getDate();
					entries.put(uid.uid, new Entry(uid.uid, received == null ? 0 : received.getTime()));
					if (received == null) {
						missingDates.add(uid.uid);
					}
				}
			}
		}
		if (!missingDates.isEmpty()) {
			String uids = missingDates.stream().map(Object::toString).collect(Collectors.joining(","));
			for (Response response : fetch(folder, "UID FETCH", uids, "UID BODY.PEEK[HEADER.FIELDS (Date)]")) {
				if (response instanceof FetchResponse fetched) {
					UID uid = fetched.getItem(UID.class);
					BODY body = fetched.getItem(BODY.class);
					if (uid != null && body != null && entries.containsKey(uid.uid)) {
						MimeMessage headers = new MimeMessage(Session.getInstance(new Properties()), body.getByteArrayInputStream());
						Date sent = headers.getSentDate();
						entries.put(uid.uid, new Entry(uid.uid, sent == null ? 0 : sent.getTime()));
					}
				}
			}
		}
		return List.copyOf(entries.values());
	}

	private Response[] fetch(IMAPFolder folder, String command, String set, String items) throws MessagingException {
		MailboxConnectionManager.checkReadRequest();
		return (Response[]) folder.doCommand(protocol -> {
			Argument arguments = new Argument();
			arguments.writeAtom(set);
			Argument requested = new Argument();
			requested.writeAtom(items);
			arguments.writeArgument(requested);
			Response[] responses = protocol.command(command, arguments);
			protocol.notifyResponseHandlers(responses);
			protocol.handleResult(responses[responses.length - 1]);
			return responses;
		});
	}
}
