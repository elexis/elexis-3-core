package ch.elexis.core.services.holder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IConfigService;

public class ConfigServiceHolder {

	public static IConfigService get() {
		return PortableServiceLoader.get(IConfigService.class);
	}

	public static boolean isPresent() {
		return ConfigServiceHolder.get() != null;
	}

	// global access methods

	public static String getGlobal(String key, String defaultValue) {
		return ConfigServiceHolder.get().get(key, defaultValue);
	}

	public static String getGlobalCached(String key, String defaultValue) {
		return ConfigServiceHolder.get().get(key, defaultValue, false);
	}

	public static int getGlobal(String key, int defaultValue) {
		return ConfigServiceHolder.get().get(key, defaultValue);
	}

	public static boolean getGlobal(String key, boolean defaultValue) {
		return ConfigServiceHolder.get().get(key, defaultValue);
	}

	public static String[] getGlobalStringArray(String key) {
		String raw = getGlobal(key, null);
		if (StringUtils.isBlank(raw)) {
			return null;
		}
		return raw.split(",");
	}

	@Deprecated(forRemoval = true)
	public static boolean setGlobal(String key, String value) {
		return ConfigServiceHolder.get().set(key, value);
	}

	@Deprecated(forRemoval = true)
	public static boolean setGlobal(String key, boolean value) {
		return ConfigServiceHolder.get().set(key, value);
	}

	public static void setGlobalAsList(String key, List<String> values) {
		Optional<String> value = values.stream().map(o -> o.toString()).reduce((u, t) -> u + "," + t);
		if (value.isPresent()) {
			ConfigServiceHolder.get().set(key, value.get());
		} else {
			ConfigServiceHolder.get().set(key, null);
		}
	}

	public static boolean setGlobal(String key, int value) {
		return ConfigServiceHolder.get().set(key, value);
	}

	public static List<String> getSubNodes(String key) {
		return ConfigServiceHolder.get().getSubNodes(key);
	}

	// active user access methods

	public static String getUser(String key, String defaultValue) {
		return ConfigServiceHolder.get().getActiveUserContact(key, defaultValue);
	}

	public static String getUserCached(String key, String defaultValue) {
		return ConfigServiceHolder.get().getActiveUserContact(key, defaultValue, false);
	}

	public static boolean getUser(String key, boolean defaultValue) {
		return ConfigServiceHolder.get().getActiveUserContact(key, defaultValue);
	}

	public static Integer getUser(String key, int defaultValue) {
		return ConfigServiceHolder.get().getActiveUserContact(key, defaultValue);
	}

	public static boolean setUser(String key, String value) {
		return ConfigServiceHolder.get().setActiveUserContact(key, value);
	}

	public static boolean setUser(String key, boolean value) {
		return ConfigServiceHolder.get().setActiveUserContact(key, value);
	}

	public static boolean setUser(String key, int value) {
		return ConfigServiceHolder.get().setActiveUserContact(key, value);
	}

	public static List<String> getUserAsList(String key) {
		String string = getUser(key, (String) null);
		if (string != null) {
			String[] split = string.split(",");
			if (split != null && split.length > 0) {
				return Arrays.asList(split);
			}
		}
		return Collections.emptyList();
	}

	public static void setUserAsList(String key, List<String> values) {
		Optional<String> value = values.stream().map(o -> o.toString()).reduce((u, t) -> u + "," + t);
		if (value.isPresent()) {
			ConfigServiceHolder.get().setActiveUserContact(key, value.get());
		} else {
			ConfigServiceHolder.get().setActiveUserContact(key, null);
		}
	}

	public static void setUserFromMap(Map<Object, Object> map) {
		ConfigServiceHolder.get().setActiveUserContact(map);
	}

	public static Map<Object, Object> getUserAsMap() {
		return ConfigServiceHolder.get().getActiveUserContactAsMap();
	}

	// active mandator access methods

	public static String getMandator(String key, String defaultValue) {
		return ConfigServiceHolder.get().getActiveMandator(key, defaultValue);
	}

	public static String getMandatorCached(String key, String defaultValue) {
		return ConfigServiceHolder.get().getActiveMandator(key, defaultValue, false);
	}

	public static boolean getMandator(String key, boolean defaultValue) {
		return ConfigServiceHolder.get().getActiveMandator(key, defaultValue);
	}

	public static int getMandator(String key, int defaultValue) {
		return ConfigServiceHolder.get().getActiveMandator(key, defaultValue);
	}

	public static void setMandator(String key, String value) {
		ConfigServiceHolder.get().setActiveMandator(key, value);
	}

	public static void setMandator(String key, boolean value) {
		ConfigServiceHolder.get().setActiveMandator(key, value);
	}

	public static List<String> getMandatorAsList(String key) {
		String string = getMandator(key, (String) null);
		if (string != null) {
			String[] split = string.split(",");
			if (split != null && split.length > 0) {
				return Arrays.asList(split);
			}
		}
		return Collections.emptyList();
	}

	// local access methods

	public static boolean getLocal(String key, boolean defaultValue) {
		return ConfigServiceHolder.get().getLocal(key, defaultValue);
	}

	public static String getLocal(String key, String defaultValue) {
		return ConfigServiceHolder.get().getLocal(key, defaultValue);
	}

	public static int getLocal(String key, int defaultValue) {
		return ConfigServiceHolder.get().getLocal(key, defaultValue);
	}

	private static List<Runnable> waitForConfigService;

	public synchronized static void runIfConfigServiceAvailable(Runnable runnable) {
		if (ConfigServiceHolder.get() == null) {
			if (waitForConfigService == null) {
				CompletableFuture.runAsync(() -> {
					// wait for configService
					while (ConfigServiceHolder.get() == null) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							// ignore
						}
					}
					waitForConfigService.forEach(r -> r.run());
				});
				waitForConfigService = new ArrayList<>();
			}
			waitForConfigService.add(runnable);
		} else {
			runnable.run();
		}
	}
}
