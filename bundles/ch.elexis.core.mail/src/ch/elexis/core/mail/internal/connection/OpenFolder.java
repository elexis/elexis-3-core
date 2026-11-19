package ch.elexis.core.mail.internal.connection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.LongSupplier;

import org.eclipse.angus.mail.imap.IMAPFolder;
import org.eclipse.angus.mail.imap.IMAPStore;

import jakarta.mail.Folder;
import jakarta.mail.MessagingException;

/** Borrows a lane read folder or scopes a writable folder, never expunging on close. */
public final class OpenFolder implements AutoCloseable {

	private static final ThreadLocal<Cache> CURRENT = new ThreadLocal<>();
	private final IMAPFolder folder;
	private final boolean retained;

	static void use(Cache cache) {
		if (cache == null) {
			CURRENT.remove();
		} else {
			CURRENT.set(cache);
		}
	}

	/**
	 * Closes matching retained folders in the current lane cache and its peer, if a current cache exists.
	 * @param name the full folder name, or null to match all cached folders
	 * @param separator the folder hierarchy separator
	 * @param descendants whether to include descendant folders
	 * @throws MessagingException if closing a matching folder fails
	 */
	public static void invalidate(String name, char separator, boolean descendants) throws MessagingException {
		Cache cache = CURRENT.get();
		if (cache != null) {
			cache.closeMatching(name, separator, descendants);
			Cache peer = cache.peer;
			if (peer != null) {
				peer.closeMatching(name, separator, descendants);
			}
		}
	}

	static final class Cache {
		private final ReentrantLock lock = new ReentrantLock();
		private final Map<String, Entry> folders = new LinkedHashMap<>(4, 0.75f, true);
		private final LongSupplier now;
		Cache peer;
		boolean retainReads;

		Cache(LongSupplier now) {
			this.now = now;
		}

		private static String key(String name) {
			return "INBOX".equalsIgnoreCase(name) ? "INBOX" : name;
		}

		private IMAPFolder open(IMAPStore store, String name, boolean refresh) throws MessagingException {
			lock.lock();
			try {
				expire();
				String key = key(name);
				Entry entry = folders.get(key);
				if (entry != null && entry.folder.isOpen()) {
					if (refresh) {
						entry.folder.doCommand(protocol -> {
							protocol.noop();
							return null;
						});
					}
					if (!(entry.folder instanceof MailReadFolder retained) || !retained.hasExpunged()) {
						entry.used = now.getAsLong();
						return entry.folder;
					}
				}
				closeMatching(name, '\0', false);
				if (!"INBOX".equals(key)
						&& folders.keySet().stream().filter(k -> !"INBOX".equals(k)).count() >= 2) {
					for (String oldest : new ArrayList<>(folders.keySet())) {
						if (!"INBOX".equals(oldest)) {
							closeMatching(oldest, '\0', false);
							break;
						}
					}
				}
				IMAPFolder opened = (IMAPFolder) store.getFolder(name);
				if (opened.getClass() == IMAPFolder.class) {
					opened = new MailReadFolder(name, (IMAPStore) opened.getStore());
				}
				opened.open(Folder.READ_ONLY);
				folders.put(key, new Entry(opened, now.getAsLong()));
				return opened;
			} finally {
				lock.unlock();
			}
		}

		private void release(IMAPFolder folder) {
			lock.lock();
			try {
				Entry entry = folders.get(key(folder.getFullName()));
				if (entry != null && entry.folder == folder) {
					entry.used = now.getAsLong();
				}
			} finally {
				lock.unlock();
			}
		}

		void closeMatching(String name, char separator, boolean descendants) throws MessagingException {
			lock.lock();
			try {
				MessagingException failure = null;
				for (String key : new ArrayList<>(folders.keySet())) {
					if (name == null || key.equals(key(name))
							|| descendants && key.startsWith(name + separator)) {
						Entry closing = folders.remove(key);
						if (closing != null && closing.folder.isOpen()) {
							try {
								closing.folder.close(true);
							} catch (MessagingException e) {
								if (failure == null) {
									failure = e;
								} else {
									failure.addSuppressed(e);
								}
							}
						}
					}
				}
				if (failure != null) {
					throw failure;
				}
			} finally {
				lock.unlock();
			}
		}

		void expire() throws MessagingException {
			lock.lock();
			try {
				for (Map.Entry<String, Entry> entry : new ArrayList<>(folders.entrySet())) {
					if (now.getAsLong() - entry.getValue().used >= timeout(entry.getKey())) {
						closeMatching(entry.getKey(), '\0', false);
					}
				}
			} finally {
				lock.unlock();
			}
		}

		long delay() {
			lock.lock();
			try {
				long delay = Long.MAX_VALUE;
				for (Map.Entry<String, Entry> entry : folders.entrySet()) {
					delay = Math.min(delay, entry.getValue().used + timeout(entry.getKey()) - now.getAsLong());
				}
				return delay;
			} finally {
				lock.unlock();
			}
		}

		private long timeout(String key) {
			return "INBOX".equals(key) ? 300_000 : 60_000;
		}

		void close() throws MessagingException {
			closeMatching(null, '\0', false);
		}

		private static final class Entry {
			private final IMAPFolder folder;
			private long used;

			private Entry(IMAPFolder folder, long used) {
				this.folder = folder;
				this.used = used;
			}
		}
	}

	/**
	 * Opens an IMAP folder in read-only mode.
	 * @param store the connected store
	 * @param name the full folder name
	 * @throws MessagingException if opening fails
	 */
	public OpenFolder(IMAPStore store, String name) throws MessagingException {
		this(store, name, false);
	}

	/**
	 * Borrows a retained read-only lane folder or opens a writable folder for one operation.
	 * @param store the connected store
	 * @param name the full folder name
	 * @param writable whether changes are required
	 * @throws MessagingException if opening fails
	 */
	public OpenFolder(IMAPStore store, String name, boolean writable) throws MessagingException {
		this(store, name, writable, true);
	}

	OpenFolder(IMAPStore store, String name, boolean writable, boolean refresh) throws MessagingException {
		Cache cache = CURRENT.get();
		retained = !writable && cache != null && cache.retainReads;
		if (retained && cache != null) {
			folder = cache.open(store, name, refresh);
		} else {
			if (writable && cache != null) {
				invalidate(name, '\0', false);
			}
			folder = (IMAPFolder) store.getFolder(name);
			folder.open(writable ? Folder.READ_WRITE : Folder.READ_ONLY);
		}
	}

	/**
	 * Returns the open folder, which must only be used on its account executor.
	 * @return the open folder
	 */
	public IMAPFolder folder() {
		return folder;
	}

	/**
	 * Closes the folder without expunging messages.
	 * @throws MessagingException if closing fails
	 */
	@Override
	public void close() throws MessagingException {
		Cache cache = CURRENT.get();
		if (retained && cache != null) {
			cache.release(folder);
		} else if (!retained && folder.isOpen()) {
			folder.close(false);
		}
	}
}
