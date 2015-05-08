/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.data.cache;

import java.lang.ref.SoftReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import ch.rgw.tools.Log;

/**
 * A Cache with soft references and optional expiring items The cache keeps count on numbers of items
 * that are added, removed or expired and can display its statistic
 * 
 * @author Gerry
 */
@SuppressWarnings("unchecked")
public class SoftCache<K> implements IPersistentObjectCache<K> {
	private static boolean enabled = true;
	protected Map<K, CacheEntry> cache;
	protected long hits, misses, removed, inserts, expired;
	protected Log log = Log.get(SoftCache.class.getName());
	
	public SoftCache(){
		// must be thread-safe
		cache = Collections.synchronizedMap(new HashMap<K, CacheEntry>());
	}
	
	/**
	 * 
	 * @param num the initial cache capcity
	 * @param load the load factor
	 */
	public SoftCache(final int num, final float load){
		cache = Collections.synchronizedMap(new HashMap<K, CacheEntry>(num, load));
	}
	
	public SoftCache(final int num){
		cache = Collections.synchronizedMap(new HashMap<K, CacheEntry>(num));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#put(K, java.lang.Object, int)
	 */
	public void put(final K key, final Object object, final int timeToCacheInSeconds){
		if (enabled) {
			cache.put(key, new CacheEntry(object, timeToCacheInSeconds));
			inserts++;
		}
	}
	
	public Object get(final K key, final int timeToCacheInSeconds) {
		return get(key);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#get(K)
	 */
	public Object get(final K key){
		if (!enabled) {
			return null;
		}
		synchronized (cache) {
			CacheEntry ref = cache.get(key);
			if (ref == null) {
				misses++;
				return null;
			}
			Object ret = ref.get();
			if (ret == null) {
				remove(key);
				return null;
			} else {
				hits++;
				return ret;
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#remove(K)
	 */
	public void remove(final K key){
		synchronized (cache) {
			cache.remove(key);
			removed++;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#clear()
	 */
	public void clear(){
		synchronized (cache) {
			purge();
			cache.clear();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#stat()
	 */
	public void stat(){
		long total = hits + misses + removed + expired;
		if (total != 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("--------- cache statistics ------\n").append("Total read:\t").append(total)
				.append("\n").append("cache hits:\t").append(hits).append(" (")
				.append(hits * 100 / total).append("%)\n").append("object expired:\t")
				.append(expired).append(" (").append(expired * 100 / total).append("%)\n")
				.append("cache missed:\t").append(misses).append(" (").append(misses * 100 / total)
				.append("%)\n").append("object removed:\t").append(removed).append(" (")
				.append(removed * 100 / total).append("%)\n").append("Object inserts:\t")
				.append(inserts).append("\n");
			log.log(sb.toString(), Log.INFOS);
		}
		
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#purge()
	 */
	public void purge(){
		synchronized (cache) {
			Iterator<Entry<K, CacheEntry>> it = cache.entrySet().iterator();
			long freeBefore = Runtime.getRuntime().freeMemory();
			while (it.hasNext()) {
				Entry<K, CacheEntry> e = it.next();
				CacheEntry ce = e.getValue();
				ce.expires = 0;
				ce.get();
				it.remove();
			}
			// TODO change logging for debug mode
			// if (Hub.plugin.DEBUGMODE) {
			// long freeAfter = Runtime.getRuntime().freeMemory();
			// StringBuilder sb = new StringBuilder();
			// sb.append("Cache purge: Free memore before: ").append(freeBefore)
			// .append(", free memory after: ").append(freeAfter).append("\n");
			// Hub.log.log(sb.toString(), Log.INFOS);
			// }
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.elexis.data.cache.IPersistentObjectCache#reset()
	 */
	public synchronized void reset(){
		purge();
		cache.clear();
	}
	
	public class CacheEntry extends SoftReference {
		long expires;
		
		public CacheEntry(final Object obj, final int timeInSeconds){
			super(obj);
			expires = System.currentTimeMillis() + timeInSeconds * 1000;
		}
		
		@Override
		public synchronized Object get(){
			Object ret = super.get();
			if (System.currentTimeMillis() > expires) {
				expired++;
				super.clear();
				ret = null;
			}
			return ret;
		}
	}
	
}
