package ch.elexis.core.services;

import java.util.List;

import ch.elexis.core.model.IContact;

public interface IConfigService {
	
	/**
	 * Set a global configuration entry. Overwrites existing values. Performs save operation.
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param value
	 *            to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if the value was successfully set
	 */
	public boolean set(String key, String value);
	
	/**
	 * Set a contact specific configuration entry. Overwrites existing value. Performs save
	 * operation.
	 * 
	 * @param contact
	 *            the contact this configuration entry is accounted to, not <code>null</code>
	 * @param key
	 *            identifying the configuration entry
	 * @param value
	 *            to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if value was successfully set
	 */
	public boolean set(IContact contact, String key, String value);
	
	/**
	 * Set a global configuration entry. Overwrites existing value. Performs save operation. Cares
	 * for list order.
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param values
	 *            to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if the values were successfully set
	 */
	public boolean setFromList(String key, List<String> values);
	
	/**
	 * Set a contact specific configuration entry. Overwrites existing value. Performs save
	 * operation. Cares for list order.
	 * 
	 * @param contact
	 *            the contact this configuration entry is accounted to, not <code>null</code>
	 * @param key
	 *            identifying the configuration entry
	 * @param value
	 *            to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if value was successfully set
	 */
	public boolean setFromList(IContact contact, String key, List<String> values);
	
	/**
	 * Get a stored value for a given global configuration entry, or return the value provided as
	 * default if not found.
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return
	 */
	public String get(String key, String defaultValue);
	
	/**
	 * Convenience method wrapping {@link #get(String, String)}
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return
	 */
	public boolean get(String key, boolean defaultValue);

	/**
	 * Convenience method wrapping {@link #get(String, String)}
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return
	 */
	public int get(String key, int defaultValue);

	/**
	 * Get a stored value for a given contact specific configuration entry, or return the value
	 * provided as default if not found.
	 * 
	 * @param contact
	 *            the contact this configuration entry is accounted to, not <code>null</code>
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return
	 */
	public String get(IContact contact, String key, String defaultValue);
	
	/**
	 * Convenience method wrapping {@link #get(IContact, String, String)}
	 * 
	 * @param contact
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return
	 */
	public boolean get(IContact contact, String key, boolean defaultValue);
	
	/**
	 * Convenience method wrapping {@link #get(IContact, String, String)}
	 * 
	 * @param contact
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return
	 */
	public int get(IContact contact, String key, int defaultValue);
	
	/**
	 * Retrieve a value as a list for a global configuration entry. Retains the order it was
	 * persisted.
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return the stored entries, defaultValue if not set
	 */
	public List<String> getAsList(String key, List<String> defaultValue);
	
	/**
	 * Retrieve a value as a list for a contact specific configuration entry. Retains the order it
	 * was persisted.
	 * 
	 * @param contact
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public List<String> getAsList(IContact contact, String key, List<String> defaultValue);
	
}
