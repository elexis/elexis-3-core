package ch.elexis.core.services.util;

import java.util.List;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

public class ConfigUtil {
	
	/**
	 * Get the String value of the matching {@link IConfig} entry. If no {@link Config} is present
	 * defaultValue is returned.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getGlobalConfig(String key, String defaultValue){
		Optional<IConfig> loaded = CoreModelServiceHolder.get().load(key, IConfig.class);
		if (loaded.isPresent()) {
			String value = loaded.get().getValue();
			return value != null ? value : defaultValue;
		}
		return defaultValue;
	}
	
	public static int getGlobalConfig(String key, int defaultValue){
		return Integer.parseInt(getGlobalConfig(key, Integer.toString(defaultValue)));
	}
	
	/**
	 * Test if there is a matching {@link IConfig} entry with a value that can be interpreted as
	 * true. If no {@link IConfig} is present defaultValue is returned.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean isGlobalConfig(String key, boolean defaultValue){
		Optional<IConfig> loaded = CoreModelServiceHolder.get().load(key, IConfig.class);
		if (loaded.isPresent()) {
			String value = loaded.get().getValue();
			return value != null && (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
		}
		return defaultValue;
	}
	
	/**
	 * Set the String value of the {@link IConfig} with a matching key. If there is no such
	 * {@link IConfig} a new one is created.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static void setGlobalConfig(String key, String value){
		Optional<IConfig> loaded = CoreModelServiceHolder.get().load(key, IConfig.class);
		IConfig config = loaded.orElseGet(() -> {
			IConfig ret = CoreModelServiceHolder.get().create(IConfig.class);
			ret.setKey(key);
			return ret;
		});
		config.setValue(value);
		CoreModelServiceHolder.get().save(config);
	}
	
	/**
	 * Get the String value of a matching {@link IUserConfig} entry for the owner. If no entry is
	 * present defaultValue is returned.
	 * 
	 * @param owner
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static String getUserConfig(IContact owner, String key, String defaultValue){
		if (owner != null) {
			INamedQuery<IUserConfig> configQuery =
				CoreModelServiceHolder.get().getNamedQuery(IUserConfig.class, true, "owner",
					"param");
			List<IUserConfig> configs = configQuery
				.executeWithParameters(
					CoreModelServiceHolder.get().getParameterMap("owner", owner, "param", key));
			if (!configs.isEmpty()) {
				if (configs.size() > 1) {
					LoggerFactory.getLogger(ConfigUtil.class)
						.warn("Multiple user config entries for [" + key + "] using first.");
				}
				String value = configs.get(0).getValue();
				return value != null ? value : defaultValue;
			}
		}
		return defaultValue;
	}
	
	/**
	 * Test if there is a matching {@link IUserConfig} entry with a value that can be interpreted as
	 * true. If no {@link IUserConfig} is present defaultValue is returned.
	 * 
	 * @param key
	 * @param defaultValue
	 * @return
	 */
	public static boolean isUserConfig(IContact owner, String key, boolean defaultValue){
		if (owner != null) {
			INamedQuery<IUserConfig> configQuery = CoreModelServiceHolder.get()
				.getNamedQuery(IUserConfig.class, true, "owner", "param");
			List<IUserConfig> configs = configQuery.executeWithParameters(
				CoreModelServiceHolder.get().getParameterMap("owner", owner, "param", key));
			if (!configs.isEmpty()) {
				if (configs.size() > 1) {
					LoggerFactory.getLogger(ConfigUtil.class)
						.warn("Multiple user config entries for [" + key + "] using first.");
				}
				String value = configs.get(0).getValue();
				return value != null
					&& (value.equalsIgnoreCase("true") || value.equalsIgnoreCase("1"));
			}
		}
		return defaultValue;
	}
	
	/**
	 * Set the String value of the {@link IUserConfig} with a matching key and owner. If there is no
	 * such {@link IUserConfig} a new one is created.
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public static void setUserConfig(IContact owner, String key, String value){
		Optional<IUserConfig> loaded = Optional.empty();
		if (owner != null) {
			INamedQuery<IUserConfig> configQuery = CoreModelServiceHolder.get()
				.getNamedQuery(IUserConfig.class, true, "owner", "param");
			List<IUserConfig> configs = configQuery.executeWithParameters(
				CoreModelServiceHolder.get().getParameterMap("owner", owner, "param", key));
			if (!configs.isEmpty()) {
				if (configs.size() > 1) {
					LoggerFactory.getLogger(ConfigUtil.class)
						.warn("Multiple user config entries for [" + key + "] using first.");
				}
				loaded = Optional.of(configs.get(0));
			}
			IUserConfig config = loaded.orElseGet(() -> {
				IUserConfig ret = CoreModelServiceHolder.get().create(IUserConfig.class);
				ret.setOwner(owner);
				ret.setKey(key);
				return ret;
			});
			config.setValue(value);
			CoreModelServiceHolder.get().save(config);
		}
	}
}
