package ch.elexis.core.services;

import java.util.Set;

public interface IConfigService {
	
	/**
	 * Set a value for a given key. Overwrites existing values. Performs save operation.
	 * 
	 * @param key
	 * @param value
	 *            to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if the value was successfully set
	 */
	public boolean set(String key, String value);
	
	/**
	 * Store a set of values to a configuration key. Performs save operation.
	 * 
	 * @param key
	 * @param values
	 *            to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if the values were successfully set
	 */
	public boolean setFromSet(String key, Set<String> values);
	
	/**
	 * Get a stored value for a given key, or return the value provided as default
	 * 
	 * @param key
	 * @param defaultValue
	 * @return the stored entries, defaultValue if not set
	 */
	public String get(String key, String defaultValue);
	
	/**
	 * Get a stored value for a given key as boolean, or return the value provided as default
	 * 
	 * @param key
	 * @return the stored entries, defaultValue if not set
	 */
	public boolean get(String key, boolean defaultValue);
	
	/**
	 * Retrieve a value as a set.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return the stored entries, defaultValue if not set
	 */
	public Set<String> getAsSet(String key, Set<String> defaultValue);
	
}
