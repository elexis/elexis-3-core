package ch.elexis.core.data.cache;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import ch.elexis.data.DBConnection;

public class MultiGuavaCache<K> implements IPersistentObjectCache<K> {
	
	private Cache<K, Object> shortTermCache;
	private Cache<K, Object> longTermCache;
	
	// enable for debugging ...
	private static boolean STAT_ENABLED = false;
	
	public MultiGuavaCache(long duration, TimeUnit unit){
		if (STAT_ENABLED) {
			shortTermCache = CacheBuilder.newBuilder().softValues().recordStats()
				.expireAfterWrite(duration, unit).build();
			longTermCache = CacheBuilder.newBuilder().softValues().recordStats()
				.maximumSize(Long.MAX_VALUE).build();
		} else {
			shortTermCache =
				CacheBuilder.newBuilder().softValues().expireAfterWrite(duration, unit).build();
			longTermCache =
				CacheBuilder.newBuilder().softValues().maximumSize(Long.MAX_VALUE).build();
		}
	}
	
	@Override
	public void put(K key, Object object, int timeToCacheInSeconds){
		if (key == null || object == null)
			return;
		if (timeToCacheInSeconds <= DBConnection.CACHE_DEFAULT_LIFETIME) {
			shortTermCache.put(key, object);
		} else {
			longTermCache.put(key, object);
		}
	}
	
	@Override
	public Object get(K key, int cacheTime){
		if (cacheTime <= DBConnection.CACHE_DEFAULT_LIFETIME) {
			return shortTermCache.getIfPresent(key);
		}
		return longTermCache.getIfPresent(key);
	}
	
	@Override
	public Object get(K key){
		Object ret = shortTermCache.getIfPresent(key);
		if (ret != null)
			return ret;
		return longTermCache.getIfPresent(key);
	}
	
	@Override
	public void remove(K key){
		shortTermCache.invalidate(key);
		longTermCache.invalidate(key);
	}
	
	@Override
	public void clear(){
		shortTermCache.invalidateAll();
		longTermCache.invalidateAll();
	}
	
	@Override
	public void stat(){
		if (STAT_ENABLED) {
			CacheStats shortStats = shortTermCache.stats();
			CacheStats longStats = longTermCache.stats();
			System.out.println("--------- GUAVA CACHE Statistics ---------");
			System.out.println("|>--- SHORT-TERM");
			System.out.println("| Hits (count/rate): " + shortStats.hitCount() + " / "
				+ String.format("%.2f%%", shortStats.hitRate() * 100));
			System.out.println("| Misses (count/rate): " + shortStats.missCount() + " / "
				+ String.format("%.2f%%", shortStats.missRate() * 100));
			System.out.println("|>--- LONG-TERM ");
			System.out.println("| Hits (count/rate): " + longStats.hitCount() + " / "
				+ String.format("%.2f%%", longStats.hitRate() * 100));
			System.out.println("| Misses (count/rate): " + longStats.missCount() + " / "
				+ String.format("%.2f%%", longStats.missRate() * 100));
			System.out.println("------------------------------------------");
		}
	}
	
	@Override
	public void purge(){
		clear();
	}
	
	@Override
	public void reset(){
		clear();
	}
	
}
