package ch.elexis.core.preferences;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IConfigService;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.CoreUtil.OS;

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

	public static String getOsSpecificPreference(String defaultPreference,
			IConfigService configService) {
		OS operatingSystem = CoreUtil.getOperatingSystemType();
		String osSpecificPreference = getOsSpecificPreferenceName(operatingSystem, defaultPreference);
		String value = configService.get(osSpecificPreference, null);
		if (StringUtils.isBlank(value)) {
			LoggerFactory.getLogger(PreferencesUtil.class)
					.warn("No OS specific path set, reverting to generic setting");
			value = configService.get(defaultPreference, null);
		}
		return value;
	}
}
