package ch.elexis.core.services;

/**
 * @since 3.13
 */
public interface ILocalConfigService {

	boolean set(String key, String value);

	boolean set(String key, boolean value);

	String get(String key, String defaultValue);

	boolean get(String key, boolean defaultValue);

	int get(String key, int defaultValue);

	boolean set(String name, int value);

}
