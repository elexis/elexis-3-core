package ch.elexis.core.preferences;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IConfigService;
import ch.elexis.core.utils.CoreUtil;

public class PreferencesUtil {

	public static String getOsSpecificPreferenceName(CoreUtil.OS system, String defaultPreference) {
		switch (system) {
		case WINDOWS:
			return defaultPreference + "_WINDOWS";
		case MAC:
			return defaultPreference + "_MAC";
		case LINUX:
			return defaultPreference + "_LINUX";
		default:
			return defaultPreference;
		}
	}

	public static String getOsSpecificPreference(String defaultPreference, IConfigService configService) {
		// deliberately not delegating: this variant must keep looking at the global
		// scope only, adding the legacy fallback would change behaviour for existing
		// callers
		String osSpecificPreference = getOsSpecificPreferenceName(CoreUtil.getOperatingSystemType(), defaultPreference);
		String value = configService.get(osSpecificPreference, null);
		if (StringUtils.isBlank(value)) {
			LoggerFactory.getLogger(PreferencesUtil.class)
					.debug("No OS specific value for [{}], reverting to generic setting", osSpecificPreference); //$NON-NLS-1$
			// never return null
			value = configService.get(defaultPreference, StringUtils.EMPTY);
		}
		return value;
	}

	public static String getOsSpecificPreference(String defaultPreference, boolean global,
			IConfigService configService) {
		String osSpecificPreference = getOsSpecificPreferenceName(CoreUtil.getOperatingSystemType(), defaultPreference);

		String value = read(osSpecificPreference, global, configService);
		if (StringUtils.isBlank(value)) {
			LoggerFactory.getLogger(PreferencesUtil.class)
					.debug("No OS specific value for [{}], reverting to generic setting", osSpecificPreference); //$NON-NLS-1$
			value = read(defaultPreference, global, configService);
		}
		if (StringUtils.isBlank(value)) {
			// the setting may predate the global / local switch
			value = read(defaultPreference, !global, configService);
		}
		return StringUtils.isBlank(value) ? null : value;
	}

	public static boolean migrateToOsSpecificPreference(String defaultPreference, boolean global,
			IConfigService configService) {
		String osSpecificPreference = getOsSpecificPreferenceName(CoreUtil.getOperatingSystemType(), defaultPreference);
		if (defaultPreference.equals(osSpecificPreference)) {
			// OS.UNSPECIFIED - the base key already is the operating system specific key
			return false;
		}
		if (StringUtils.isNotBlank(read(osSpecificPreference, global, configService))) {
			return false;
		}
		String legacyValue = read(defaultPreference, global, configService);
		if (StringUtils.isBlank(legacyValue)) {
			return false;
		}
		write(osSpecificPreference, legacyValue, global, configService);
		LoggerFactory.getLogger(PreferencesUtil.class).info("Migrated [{}] to [{}]", defaultPreference, //$NON-NLS-1$
				osSpecificPreference);
		return true;
	}

	private static String read(String key, boolean global, IConfigService configService) {
		return global ? configService.get(key, null) : configService.getLocal(key, null);
	}

	private static void write(String key, String value, boolean global, IConfigService configService) {
		if (global) {
			configService.set(key, value);
		} else {
			configService.setLocal(key, value);
		}
	}
}
