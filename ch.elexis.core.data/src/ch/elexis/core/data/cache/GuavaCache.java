package ch.elexis.core.data.cache;

import java.util.concurrent.TimeUnit;

import ch.elexis.data.PersistentObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

/**
 * A short-term guava cache automatically expiring objects after a certain
 * amount of time. Resembles the behaviour of the original {@link SoftCache};
 * however, not considering single object deviating cache times as propagated
 * by {@link PersistentObject#getCacheTime()}
 *
 * @param <K>
 */
public class GuavaCache<K> implements IPersistentObjectCache<K> {
	
	private Cache<K, Object> shortTermCache;
	
	public GuavaCache(long duration, TimeUnit unit){
		shortTermCache =
			CacheBuilder.newBuilder().recordStats().expireAfterWrite(duration, unit).build();
	}
	
	@Override
	public void put(K key, Object object, int timeToCacheInSeconds){
		shortTermCache.put(key, object);
	}
	
	@Override
	public Object get(K key){
		return shortTermCache.getIfPresent(key);
	}
	
	@Override
	public void remove(K key){
		shortTermCache.invalidate(key);
	}
	
	@Override
	public void clear(){
		shortTermCache.invalidateAll();
	}
	
	@Override
	public void stat(){
		CacheStats shortStats = shortTermCache.stats();
		System.out.println("--- GUAVA CACHE Statistics ---");
		System.out.println("Hits (count/rate): " + shortStats.hitCount() + "/"
			+ shortStats.hitRate());
		System.out.println("Misses (count/rate): " + shortStats.missCount() + "/"
			+ shortStats.missRate());
		System.out.println("Avg load penalty: " + shortStats.averageLoadPenalty());
		System.out.println("Load (count/successcount): " + shortStats.loadCount() + "/"
			+ shortStats.loadSuccessCount());
	}
	
	@Override
	public void purge(){
		shortTermCache.invalidateAll();
	}
	
	@Override
	public void reset(){
		shortTermCache.invalidateAll();
	}
	
}
