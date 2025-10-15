package ch.elexis.core.ee.openapi;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.hc.client5.http.cache.HttpCacheCASOperation;
import org.apache.hc.client5.http.cache.HttpCacheEntry;
import org.apache.hc.client5.http.cache.HttpCacheStorage;
import org.apache.hc.client5.http.cache.ResourceIOException;
import org.apache.hc.client5.http.impl.cache.CacheConfig;
import org.apache.hc.client5.http.impl.cache.InternalCacheStorage;
import org.apache.hc.core5.annotation.Contract;
import org.apache.hc.core5.annotation.ThreadingBehavior;
import org.apache.hc.core5.util.Args;

/**
 * Copy of org.apache.hc.client5.http.impl.cache.BasicHttpCacheStorage with
 * adaptation to allow access to underlying internal cache
 */
@Contract(threading = ThreadingBehavior.SAFE)
public class ExtendedBasicHttpCacheStorage implements HttpCacheStorage {

	private final InternalCacheStorage entries;

	private final ReentrantLock lock;

	public ExtendedBasicHttpCacheStorage(final CacheConfig config) {
        super();
        this.entries = new InternalCacheStorage(config.getMaxCacheEntries(), null);
        this.lock = new ReentrantLock();
    }

	/**
	 * Places a HttpCacheEntry in the cache
	 *
	 * @param url   Url to use as the cache key
	 * @param entry HttpCacheEntry to place in the cache
	 */
	@Override
	public void putEntry(final String url, final HttpCacheEntry entry) throws ResourceIOException {
		lock.lock();
		try {
			entries.put(url, entry);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Gets an entry from the cache, if it exists
	 *
	 * @param url Url that is the cache key
	 * @return HttpCacheEntry if one exists, or null for cache miss
	 */
	@Override
	public HttpCacheEntry getEntry(final String url) throws ResourceIOException {
		lock.lock();
		try {
			return entries.get(url);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * Removes a HttpCacheEntry from the cache
	 *
	 * @param url Url that is the cache key
	 */
	@Override
	public void removeEntry(final String url) throws ResourceIOException {
		lock.lock();
		try {
			entries.remove(url);
		} finally {
			lock.unlock();
		}

	}

	@Override
	public void updateEntry(final String url, final HttpCacheCASOperation casOperation) throws ResourceIOException {
		lock.lock();
		try {
			final HttpCacheEntry existingEntry = entries.get(url);
			entries.put(url, casOperation.execute(existingEntry));
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Map<String, HttpCacheEntry> getEntries(final Collection<String> keys) throws ResourceIOException {
		Args.notNull(keys, "Key");
		final Map<String, HttpCacheEntry> resultMap = new HashMap<>(keys.size());
		for (final String key : keys) {
			final HttpCacheEntry entry = getEntry(key);
			if (entry != null) {
				resultMap.put(key, entry);
			}
		}
		return resultMap;
	}

	public void clearCache() {
		System.out.println("clearing http cache");
		entries.clear();
	}

}
