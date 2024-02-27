package ch.elexis.core.services.holder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IConfigService;
import ch.elexis.core.utils.OsgiServiceUtil;

@Component
public class ConfigServiceHolder {

	private static IConfigService configService;

	@Reference
	public void setConfigService(IConfigService configService) {
		ConfigServiceHolder.configService = configService;
	}

	public static IConfigService get() {
		if (configService == null) {
			configService = OsgiServiceUtil.getService(IConfigService.class)
					.orElseThrow(() -> new IllegalStateException("No IConfigService available"));
		}
		return configService;
	}

	public static boolean isPresent() {
		return get() != null;
	}

	// global access methods

	public static String getGlobal(String key, String defaultValue) {
		return get().get(key, defaultValue);
	}

	public static String getGlobalCached(String key, String defaultValue) {
		return get().get(key, defaultValue, false);
	}

	public static int getGlobal(String key, int defaultValue) {
		return get().get(key, defaultValue);
	}

	public static boolean getGlobal(String key, boolean defaultValue) {
		return get().get(key, defaultValue);
	}

	public static List<String> getGlobalAsList(String key) {
		String string = getGlobal(key, (String) null);
		if (string != null) {
			String[] split = string.split(",");
			if (split != null && split.length > 0) {
				return Arrays.asList(split);
			}
		}
		return Collections.emptyList();
	}

	public static String[] getGlobalStringArray(String key) {
		String raw = getGlobal(key, null);
		if (StringUtils.isBlank(raw)) {
			return null;
		}
		return raw.split(",");
	}

	@Deprecated
	public static boolean setGlobal(String key, String value) {
		return get().set(key, value);
	}

	@Deprecated
	public static boolean setGlobal(String key, boolean value) {
		return get().set(key, value);
	}

	public static void setGlobalAsList(String key, List<String> values) {
		Optional<String> value = values.stream().map(o -> o.toString()).reduce((u, t) -> u + "," + t);
		if (value.isPresent()) {
			get().set(key, value.get());
		} else {
			get().set(key, null);
		}
	}

	public static boolean setGlobal(String key, int value) {
		return get().set(key, value);
	}

	public static List<String> getSubNodes(String key) {
		return get().getSubNodes(key);
	}

	// active user access methods

	public static String getUser(String key, String defaultValue) {
		return get().getActiveUserContact(key, defaultValue);
	}

	public static String getUserCached(String key, String defaultValue) {
		return get().getActiveUserContact(key, defaultValue, false);
	}

	public static boolean getUser(String key, boolean defaultValue) {
		return get().getActiveUserContact(key, defaultValue);
	}

	public static Integer getUser(String key, int defaultValue) {
		return get().getActiveUserContact(key, defaultValue);
	}

	public static boolean setUser(String key, String value) {
		return get().setActiveUserContact(key, value);
	}

	public static boolean setUser(String key, boolean value) {
		return get().setActiveUserContact(key, value);
	}

	public static boolean setUser(String key, int value) {
		return get().setActiveUserContact(key, value);
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
			get().setActiveUserContact(key, value.get());
		} else {
			get().setActiveUserContact(key, null);
		}
	}

	public static void setUserFromMap(Map<Object, Object> map) {
		get().setActiveUserContact(map);
	}

	public static Map<Object, Object> getUserAsMap() {
		return get().getActiveUserContactAsMap();
	}

	// active mandator access methods

	public static String getMandator(String key, String defaultValue) {
		return get().getActiveMandator(key, defaultValue);
	}

	public static String getMandatorCached(String key, String defaultValue) {
		return get().getActiveMandator(key, defaultValue, false);
	}

	public static boolean getMandator(String key, boolean defaultValue) {
		return get().getActiveMandator(key, defaultValue);
	}

	public static int getMandator(String key, int defaultValue) {
		return get().getActiveMandator(key, defaultValue);
	}

	public static void setMandator(String key, String value) {
		get().setActiveMandator(key, value);
	}

	public static void setMandator(String key, boolean value) {
		get().setActiveMandator(key, value);
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
		return get().getLocal(key, defaultValue);
	}

	public static String getLocal(String key, String defaultValue) {
		return get().getLocal(key, defaultValue);
	}

	private static List<Runnable> waitForConfigService;

	public synchronized static void runIfConfigServiceAvailable(Runnable runnable) {
		if (configService == null) {
			if (waitForConfigService == null) {
				CompletableFuture.runAsync(() -> {
					// wait for configService
					while (configService == null) {
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
