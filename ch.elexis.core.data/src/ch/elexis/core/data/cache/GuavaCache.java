package ch.elexis.core.data.cache;

import java.util.concurrent.TimeUnit;

import ch.elexis.data.PersistentObject;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

/**
 * A short-term guava cache automatically expiring objects after a certain amount of time. Resembles
 * the behaviour of the original {@link SoftCache}; however, not considering single object deviating
 * cache times as propagated by {@link PersistentObject#getCacheTime()}
 *
 * @param <K>
 */
public class GuavaCache<K> implements IPersistentObjectCache<K> {
	
	private Cache<K, Object> shortTermCache;
	
	public GuavaCache(long duration, TimeUnit unit){
		shortTermCache =
			CacheBuilder.newBuilder().softValues().recordStats().expireAfterWrite(duration, unit).build();
	}
	
	@Override
	public void put(K key, Object object, int timeToCacheInSeconds){
		if (key == null || object == null)
			return;
		shortTermCache.put(key, object);
	}
	
	@Override
	public Object get(K key, int cacheTime){
		return shortTermCache.getIfPresent(key);
	}
	
	
	@Override
	public Object get(K key){
		return get(key, 0);
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
		System.out.println("--------- GUAVA CACHE Statistics ---------");
		System.out.println("Hits (count/rate): " + shortStats.hitCount() + " / "
			+ String.format("%.2f%%", shortStats.hitRate() * 100));
		System.out.println("Misses (count/rate): " + shortStats.missCount() + " / "
			+ String.format("%.2f%%", shortStats.missRate() * 100));
		System.out.println("------------------------------------------");
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
