package ch.rgw.io;

/**
 * Get notified about changes in {@link Settings}
 */
public interface ISettingChangedListener {

	default void settingChanged(String key, String value) {
	}

	/**
	 * performed during flush operation, effectively removing the entry from the
	 * settings store
	 *
	 * @param key
	 * @param value
	 */
	default void settingRemoved(String key) {
	}

	/**
	 * performed during flush operation, effectively writing the entry to the
	 * settings store
	 *
	 * @param key
	 * @param value
	 */
	default void settingWritten(String key, String value) {
	}

}
