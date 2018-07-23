package ch.elexis.core.jpa.entities;

import java.util.Hashtable;
import java.util.Map;

public interface EntityWithExtInfo {
	
	public Map<Object, Object> getExtInfo();
	
	public void setExtInfo(Map<Object, Object> extInfo);
	
	/**
	 * 
	 * @param key
	 *            a non-null key value
	 * @param value
	 *            the value to store, or <code>null</code> to remove the respective key
	 */
	public default void setExtInfoValue(Object key, Object value){
		if (key == null) {
			return;
		}
		// we have to create a new object on change
		// otherwise JPA won't pick-up the change
		Hashtable<Object, Object> ht = new Hashtable<Object, Object>(getExtInfo());
		if (value != null) {
			ht.put(key, value);
		} else {
			ht.remove(key);
		}
		setExtInfo(ht);
	}
	
	public default Object getExtInfo(Object key){
		return getExtInfo().get(key);
	}
}
