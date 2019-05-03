package ch.elexis.core.services;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.Identifiable;

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
	 * Set a local configuration entry. Overwrites existing values.
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param value
	 *            to set, <code>null</code> to delete the entry
	 * @return <code>true</code> if the value was successfully set
	 */
	public boolean setLocal(String key, String value);
	
	/**
	 * Set a global configuration entry. Overwrites existing values. Performs save operation.
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param value
	 *            to set
	 * @return <code>true</code> if the value was successfully set
	 */
	public boolean set(String key, boolean value);
	
	/**
	 * Set a local configuration entry. Overwrites existing values.
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param value
	 *            to set
	 * @return <code>true</code> if the value was successfully set
	 */
	public boolean setLocal(String key, boolean value);
	
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
	 * Set a contact specific configuration entry. Overwrites existing value. Performs save
	 * operation.
	 * 
	 * @param contact
	 *            the contact this configuration entry is accounted to, not <code>null</code>
	 * @param key
	 *            identifying the configuration entry
	 * @param value
	 *            to set
	 * @return <code>true</code> if value was successfully set
	 */
	public boolean set(IContact contact, String key, boolean value);
	
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
	 * Get a stored value for a given local configuration entry, or return the value provided as
	 * default if not found.
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return
	 */
	public String getLocal(String key, String defaultValue);
	
	/**
	 * Get a stored value for the current active {@link IMandator} configuration.
	 * The current mandator is resolved via the {@link IContextService}
	 * 
	 * @param key
	 * @param defaultValue to return if no active mandator found or entry does not
	 *                     exist
	 * @return
	 */
	public String getActiveMandator(String key, String defaultValue);
	
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
	 * Convenience method wrapping {@link #getLocal(String, String)}
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return
	 */
	public boolean getLocal(String key, boolean defaultValue);
	
	/**
	 * Convenience method wrapping {@link #getActiveMandator(String, String)}
	 * 
	 * @param key
	 *            identifying the configuration entry
	 * @param defaultValue
	 *            to return if configuration entry does not exist
	 * @return
	 */
	public boolean getActiveMandator(String key, boolean defaultValue);
	
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
	
	/**
	 * Get a new {@link ILocalLock} instance for the provided object. Currently {@link String} and
	 * {@link Identifiable} can be locked.
	 * 
	 * @return
	 */
	public ILocalLock getLocalLock(Object object);
	
	/**
	 * Test if this instance has a lock on the object. Does <b>not</b> query the DB, only a lookup
	 * in a local {@link HashMap} is performed. If lookup in the DB is needed use
	 * {@link LocalLock#tryLock()}.
	 * 
	 * @param object
	 * @return
	 */
	public Optional<ILocalLock> getManagedLock(Object object);
	
	/**
	 * This is a lock implementation based on entries in the config DB table. <b>This implementation
	 * is not guaranteed to give only one lock for an object if two instances try to lock at the
	 * same time.</b> If there is a elexis server based locking implementation available, that
	 * implementation should be used instead.
	 */
	public interface ILocalLock {
		
		/**
		 * Get the message from the DB. Currently username only.
		 * 
		 * @return the username or ? if there is no information
		 */
		public String getLockMessage();
		
		/**
		 * Get the value of {@link System#currentTimeMillis()} when the Lock was created.
		 * 
		 * @return the value of -1 if there is no information
		 */
		public long getLockCurrentMillis();
		
		/**
		 * Delete the lock from the DB. <b>Always</b> deletes the lock, even if another instance
		 * created the lock. Can be used to remove pending locks.
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
}
