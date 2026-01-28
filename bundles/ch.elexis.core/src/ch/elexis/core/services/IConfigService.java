package ch.elexis.core.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.Identifiable;

public interface IConfigService {

	/**
	 * Set a global configuration entry. Overwrites existing values. Performs save
	 * operation. Adds a trace entry.
	 *
	 * @param key   identifying the configuration entry
	 * @param value to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if the value was successfully set
	 */
	public boolean set(String key, String value);

	/**
	 * Set a global configuration entry. Overwrites existing values. Performs save
	 * operation.
	 *
	 * @param key
	 * @param value
	 * @param addTraceEntry add an entry to trace table to allow retracing changes
	 * @return if the value was successfully set
	 */
	public boolean set(String key, String value, boolean addTraceEntry);

	/**
	 * Set a local configuration entry. Overwrites existing values.
	 *
	 * @param key   identifying the configuration entry
	 * @param value to set, <code>null</code> to delete the entry
	 */
	default void setLocal(String key, String value) {
		getLocalConfigService().set(key, value);
	}

	/**
	 * Set a global configuration entry. Overwrites existing values. Performs save
	 * operation.
	 *
	 * @param key   identifying the configuration entry
	 * @param value to set
	 * @return <code>true</code> if the value was successfully set
	 */
	public boolean set(String key, boolean value);

	/**
	 * Set a local configuration entry. Overwrites existing values.
	 *
	 * @param key   identifying the configuration entry
	 * @param value to set
	 */
	default void setLocal(String key, boolean value) {
		getLocalConfigService().set(key, value);
	}

	/**
	 * Set a local configuration entry. Overwrites existing values.
	 *
	 * @param key   identifying the configuration entry
	 * @param value to set
	 */
	default void setLocal(String name, int value) {
		getLocalConfigService().set(name, value);
	}

	/**
	 * Set a global configuration entry. Overwrites existing values. Performs save
	 * operation. Adds a trace entry.
	 *
	 * @param key   identifying the configuration entry
	 * @param value to set
	 * @return <code>true</code> if the value was successfully set
	 */
	public boolean set(String key, int value);

	/**
	 * Set a contact specific configuration entry. Overwrites existing value.
	 * Performs save operation.
	 *
	 * @param contact the contact this configuration entry is accounted to, not
	 *                <code>null</code>
	 * @param key     identifying the configuration entry
	 * @param value   to set
	 * @return <code>true</code> if value was successfully set
	 */
	public boolean set(IContact contact, String key, int value);

	/**
	 * Set a contact specific configuration entry. Overwrites existing value.
	 * Performs save operation.
	 *
	 * @param contact the contact this configuration entry is accounted to, not
	 *                <code>null</code>
	 * @param key     identifying the configuration entry
	 * @param value   to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if value was successfully set
	 */
	public boolean set(IContact contact, String key, String value);

	/**
	 * Set a mandator contact specific configuration entry. Overwrites existing
	 * value. Performs save operation. The current mandator is resolved via the
	 * {@link IContextService}
	 *
	 * @param configMigrationdomainName
	 * @param name
	 */
	public void setActiveMandator(String key, String value);

	/**
	 * Set a mandator contact specific configuration entry. Overwrites existing
	 * value. Performs save operation. The current mandator is resolved via the
	 * {@link IContextService}
	 *
	 * @param key
	 * @param value
	 */
	public void setActiveMandator(String key, boolean value);

	/**
	 * Convenience method wrapping {@link #set(IContact, String, String)} fetching
	 * the current user contact from {@link IContextService}
	 *
	 * @param key   identifying the configuration entry
	 * @param value to set
	 * @return <code>true</code> if value was successfully set
	 */
	public boolean setActiveUserContact(String key, String value);

	/**
	 * Convenience method wrapping {@link #set(IContact, String, int)} fetching the
	 * current user contact from {@link IContextService}
	 *
	 * @param key   identifying the configuration entry
	 * @param value to set
	 * @return <code>true</code> if value was successfully set
	 */
	public boolean setActiveUserContact(String key, int value);

	/**
	 * Convenience method wrapping {@link #set(IContact, String, boolean)} fetching
	 * the current user contact from {@link IContextService}
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setActiveUserContact(String key, boolean value);

	/**
	 * Set a contact specific configuration entry. Overwrites existing value.
	 * Performs save operation. Adds a trace entry.
	 *
	 * @param contact the contact this configuration entry is accounted to, not
	 *                <code>null</code>
	 * @param key     identifying the configuration entry
	 * @param value   to set
	 * @return <code>true</code> if value was successfully set
	 */
	public boolean set(IContact contact, String key, boolean value);

	/**
	 * Set a global configuration entry. Overwrites existing value. Performs save
	 * operation. Cares for list order. Adds a trace entry.
	 *
	 * @param key    identifying the configuration entry
	 * @param values to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if the values were successfully set
	 */
	public boolean setFromList(String key, List<String> values);

	/**
	 * Set a contact specific configuration entry. Overwrites existing value.
	 * Performs save operation. Cares for list order. Adds a trace entry.
	 *
	 * @param contact the contact this configuration entry is accounted to, not
	 *                <code>null</code>
	 * @param key     identifying the configuration entry
	 * @param value   to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if value was successfully set
	 */
	public boolean setFromList(IContact contact, String key, List<String> values);

	/**
	 * Set all contact specific configuration entries from the provided map.
	 * Overwrites existing value. Performs save operation.
	 *
	 * @param map
	 */
	public void setActiveUserContact(Map<Object, Object> map);

	/**
	 * Get a stored value for a given global configuration entry, or return the
	 * value provided as default if not found.
	 *
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	public default String get(String key, String defaultValue) {
		return get(key, defaultValue, true);
	}

	/**
	 * Get a stored value for a given global configuration entry, or return the
	 * value provided as default if not found.
	 *
	 * If refreshCache is true the value is always fetched from the database, else
	 * the cached value is used if present.
	 *
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @param refreshCache
	 * @return
	 */
	public String get(String key, String defaultValue, boolean refreshCache);

	/**
	 * (SQL Transaction) Either get or insert the value if not found.
	 * 
	 * @param contact     if set: the contact the contact this configuration entry
	 *                    is accounted to (userconfig);<br>
	 *                    if <code>null</code> global config
	 * @param key         identifying the configuration entry
	 * @param insertValue a Supplier to provide the value to insert, if no value
	 *                    found for the key
	 * @return
	 * @since 3.9
	 */
	public String getOrInsert(IContact contact, String key, Supplier<String> insertValue);

	/**
	 * Get a stored value for a given local configuration entry, or return the value
	 * provided as default if not found.
	 *
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	default String getLocal(String key, String defaultValue) {
		return getLocalConfigService().get(key, defaultValue);
	}

	/**
	 * Convenience method wrapping {@link #get(String, String)}
	 *
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	public boolean get(String key, boolean defaultValue);

	public ILocalConfigService getLocalConfigService();

	/**
	 * Convenience method wrapping {@link #getLocal(String, String)}
	 *
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	default boolean getLocal(String key, boolean defaultValue) {
		return getLocalConfigService().get(key, defaultValue);
	}

	/**
	 * Convenience method wrapping {@link #getLocal(String, String)}
	 *
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	default int getLocal(String key, int defaultValue) {
		return getLocalConfigService().get(key, defaultValue);
	}

	/**
	 * Get a stored value for the current active {@link IMandator} configuration.
	 * The current mandator is resolved via the {@link IContextService}
	 *
	 * @param key
	 * @param defaultValue to return if no active mandator found or entry does not
	 *                     exist
	 * @return
	 */
	public default String getActiveMandator(String key, String defaultValue) {
		return getActiveMandator(key, defaultValue, true);
	}

	/**
	 * Get a stored value for the current active {@link IMandator} configuration.
	 * The current mandator is resolved via the {@link IContextService}
	 *
	 * If refreshCache is true the value is always fetched from the database, else
	 * the cached value is used if present.
	 *
	 * @param key
	 * @param defaultValue to return if no active mandator found or entry does not
	 *                     exist
	 * @param refreshCache
	 * @return
	 */
	public String getActiveMandator(String key, String defaultValue, boolean refreshCache);

	/**
	 * Convenience method wrapping {@link #getActiveMandator(String, String)}
	 *
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	public boolean getActiveMandator(String key, boolean defaultValue);

	/**
	 * Convenience method wrapping {@link #get(IContact, String, String)}, fetching
	 * the current mandator via {@link IContextService}
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getActiveMandator(String key, int defaultValue);

	/**
	 * Convenience method wrapping {@link #get(IContact, String, String)}, fetching
	 * the current user via {@link IContextService}
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public default String getActiveUserContact(String key, String defaultValue) {
		return getActiveUserContact(key, defaultValue, true);
	}

	/**
	 * Convenience method wrapping {@link #get(IContact, String, String)}, fetching
	 * the current user via {@link IContextService}.
	 *
	 * If refreshCache is true the value is always fetched from the database, else
	 * the cached value is used if present.
	 *
	 * @param key
	 * @param defaultValue
	 * @param refreshCache
	 * @return
	 */
	public String getActiveUserContact(String key, String defaultValue, boolean refreshCache);

	/**
	 * Convenience method wrapping {@link #get(IContact, String, String)}, fetching
	 * the current user via {@link IContextService}
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public int getActiveUserContact(String key, int defaultValue);

	/**
	 * Convenience method wrapping {@link #get(IContact, String, String)}, fetching
	 * the current user via {@link IContextService}
	 *
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public boolean getActiveUserContact(String key, boolean defaultValue);

	/**
	 * Convenience method wrapping {@link #get(String, String)}
	 *
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	public int get(String key, int defaultValue);

	/**
	 * Get a stored value for a given contact specific configuration entry, or
	 * return the value provided as default if not found.
	 *
	 * @param contact      the contact this configuration entry is accounted to, not
	 *                     <code>null</code>
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	public default String get(IContact contact, String key, String defaultValue) {
		return get(contact, key, defaultValue, true);
	}

	/**
	 * Get a stored value for a given contact specific configuration entry, or
	 * return the value provided as default if not found.
	 *
	 * If refreshCache is true the value is always fetched from the database, else
	 * the cached value is used if present.
	 *
	 * @param contact      the contact this configuration entry is accounted to, not
	 *                     <code>null</code>
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @param refreshCache
	 * @return
	 */
	public String get(IContact contact, String key, String defaultValue, boolean refreshCache);

	/**
	 * Convenience method wrapping {@link #get(IContact, String, String)}
	 *
	 * @param contact
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	public boolean get(IContact contact, String key, boolean defaultValue);

	/**
	 * Convenience method wrapping {@link #get(IContact, String, String)}
	 *
	 * @param contact
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return
	 */
	public int get(IContact contact, String key, int defaultValue);

	/**
	 * Retrieve a value as a list for a global configuration entry. Retains the
	 * order it was persisted.
	 *
	 * @param key          identifying the configuration entry
	 * @param defaultValue to return if configuration entry does not exist
	 * @return the stored entries, defaultValue if not set
	 */
	public List<String> getAsList(String key, List<String> defaultValue);

	/**
	 * Retrieve a value as a list for a global configuration entry. Retains the
	 * order it was persisted.
	 *
	 * @param key identifying the configuration entry
	 * @return the stored entries, or an empty, immutable list
	 */
	public default List<String> getAsList(String key) {
		return getAsList(key, Collections.emptyList());
	}

	/**
	 * Retrieve a value as a list for a contact specific configuration entry.
	 * Retains the order it was persisted.
	 *
	 * @param contact
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public List<String> getAsList(IContact contact, String key, List<String> defaultValue);

	/**
	 * Get the whole config for a given contact as hierarchy of {@link Map}s.
	 *
	 * @param contact not <code>null</code>
	 * @return
	 */
	public Map<Object, Object> getAsMap(IContact contact);

	/**
	 * Get the whole config for the active user contact as hierarchy of
	 * {@link Map}s.
	 *
	 * @return
	 */
	public Map<Object, Object> getActiveUserContactAsMap();

	/**
	 * Set all contact specific configuration entries from the provided map.
	 * Overwrites existing value. Performs save operation.
	 *
	 * @param person
	 * @param map
	 */
	public void setFromMap(IContact person, Map<Object, Object> map);

	/**
	 * Get a list of all possible sub nodes for the provided key.
	 *
	 * @param key
	 * @return
	 */
	default public List<String> getSubNodes(String key) {
		return getSubNodes(key, false);
	}

	/**
	 * Get a list of all possible sub nodes for the provided key.
	 *
	 * If refreshCache is true the value is always fetched from the database, else
	 * the cached value is used if present.
	 *
	 * @param key
	 * @return
	 */
	public List<String> getSubNodes(String key, boolean refreshCache);

	/**
	 * Get a new {@link ILocalLock} instance for the provided object. Currently
	 * {@link String} and {@link Identifiable} can be locked.
	 *
	 * @return
	 */
	public ILocalLock getLocalLock(Object object);

	/**
	 * Test if this instance has a lock on the object. Does <b>not</b> query the DB,
	 * only a lookup in a local {@link HashMap} is performed. If lookup in the DB is
	 * needed use {@link LocalLock#tryLock()}.
	 *
	 * @param object
	 * @return
	 */
	public Optional<ILocalLock> getManagedLock(Object object);

	/**
	 * This is a lock implementation based on entries in the config DB table.
	 * <b>This implementation is not guaranteed to give only one lock for an object
	 * if two instances try to lock at the same time.</b> If there is a elexis
	 * server based locking implementation available, that implementation should be
	 * used instead.
	 */
	public interface ILocalLock {

		/**
		 * Get the message from the DB. Currently username only.
		 *
		 * @return the username or ? if there is no information
		 */
		public String getLockMessage();

		/**
		 * Get the value of {@link System#currentTimeMillis()} when the Lock was
		 * created.
		 *
		 * @return the value of -1 if there is no information
		 */
		public long getLockCurrentMillis();

		/**
		 * Delete the lock from the DB. <b>Always</b> deletes the lock, even if another
		 * instance created the lock. Can be used to remove pending locks.
		 */
		public void unlock();

		/**
		 * Checks if the user has the lock
		 *
		 * @return true if the user has the lock otherwise return false
		 */
		public boolean hasLock(String userName);

		/**
		 * Try to write the lock to the DB if there is not already a lock on the object.
		 *
		 * @return true if lock written, false if there is already a lock in the DB.
		 */
		public boolean tryLock();
	}

	/** methods from ConfigServiceHolder **/

	default String getUser(String key, String defaultValue) {
		return getActiveUserContact(key, defaultValue);
	}

	default boolean getUser(String key, boolean defaultValue) {
		return getActiveUserContact(key, defaultValue);
	}

}
