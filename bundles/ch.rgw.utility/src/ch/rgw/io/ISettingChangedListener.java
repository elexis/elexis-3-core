package ch.rgw.io;

/**
 * Get notified about changes in {@link Settings}
 */
public interface ISettingChangedListener {
	
	default public void settingChanged(String key, String value) {}
	
	default public void settingRemoved(String key) {}
	
	/**
	 * performed during flush operation, effectively writing the entry to the settings store
	 * 
	 * @param key
	 * @param value
	 */
	default public void settingWritten(String key, String value) {}
	
	/**
	 * performed during flush operation, effectively removing the entry from the settings store
	 * 
	 * @param key
	 * @param value
	 */
	default public void settingDeleted(String key) {}
}
