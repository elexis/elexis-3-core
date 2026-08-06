package ch.elexis.core.preferences;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.ConfigurationScope;
import ch.elexis.core.model.IContact;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.CoreUtil.OS;


public class PreferencesUtil {

	private static final Logger LOG = LoggerFactory.getLogger(PreferencesUtil.class);
	private static final String UNSUPPORTED_SCOPE_MSG = "Unsupported scope: ";

	public static String getOsSpecificPreferenceName(OS system, String defaultPreference) {
		if (system == OS.WINDOWS || system == OS.MAC || system == OS.LINUX) {
			return defaultPreference + "_" + system.name();
		}
		return defaultPreference;
	}

	public static String getOsSpecificPreference(String defaultPreference, IConfigService configService) {
		String osSpecificPreference = getOsSpecificPreferenceName(CoreUtil.getOperatingSystemType(), defaultPreference);
		String value = configService.get(osSpecificPreference, null);

		if (StringUtils.isBlank(value)) {
			LOG.debug("No OS specific value for [{}], reverting to generic setting", osSpecificPreference);
			value = configService.get(defaultPreference, StringUtils.EMPTY);
		}
		return value;
	}

	public static String getOsSpecificGlobalPreference(String defaultPreference, IConfigService configService) {
		String osSpecificPreference = osSpecificName(defaultPreference);
		String value = read(osSpecificPreference, ConfigurationScope.GLOBAL, null, configService);
		value = orElse(value, defaultPreference, ConfigurationScope.GLOBAL, null, configService);
		value = orElse(value, defaultPreference, ConfigurationScope.LOCAL, null, configService);

		return blankToNull(value);
	}

	public static String getOsSpecificLocalPreference(String defaultPreference, IConfigService configService) {
		String osSpecificPreference = osSpecificName(defaultPreference);
		String value = read(osSpecificPreference, ConfigurationScope.LOCAL, null, configService);
		value = orElse(value, defaultPreference, ConfigurationScope.LOCAL, null, configService);
		value = orElse(value, defaultPreference, ConfigurationScope.GLOBAL, null, configService);

		return blankToNull(value);
	}

	public static String getOsSpecificContactPreference(String defaultPreference, IContact contact, IConfigService configService) {
		String osSpecificPreference = osSpecificName(defaultPreference);
		String value = read(osSpecificPreference, ConfigurationScope.CONTACT, contact, configService);
		value = orElse(value, defaultPreference, ConfigurationScope.CONTACT, contact, configService);
		value = orElse(value, osSpecificPreference, ConfigurationScope.GLOBAL, null, configService);
		value = orElse(value, defaultPreference, ConfigurationScope.GLOBAL, null, configService);

		return blankToNull(value);
	}

	public static boolean migrateToOsSpecificGlobalPreference(String defaultPreference, IConfigService configService) {
		return migrate(defaultPreference, ConfigurationScope.GLOBAL, null, configService);
	}

	public static boolean migrateToOsSpecificLocalPreference(String defaultPreference, IConfigService configService) {
		return migrate(defaultPreference, ConfigurationScope.LOCAL, null, configService);
	}

	public static boolean migrateToOsSpecificContactPreference(String defaultPreference, IContact contact, IConfigService configService) {
		return migrate(defaultPreference, ConfigurationScope.CONTACT, contact, configService);
	}

	private static boolean migrate(String defaultPreference, ConfigurationScope scope, IContact contact, IConfigService configService) {
		String osSpecificPreference = osSpecificName(defaultPreference);

		if (defaultPreference.equals(osSpecificPreference) ||
			StringUtils.isNotBlank(read(osSpecificPreference, scope, contact, configService))) {
			return false;
		}

		String legacyValue = read(defaultPreference, scope, contact, configService);
		if (StringUtils.isBlank(legacyValue)) {
			return false;
		}

		write(osSpecificPreference, legacyValue, scope, contact, configService);
		LOG.info("Migrated [{}] to [{}] in scope [{}]", defaultPreference, osSpecificPreference, scope);
		return true;
	}

	private static String osSpecificName(String defaultPreference) {
		return getOsSpecificPreferenceName(CoreUtil.getOperatingSystemType(), defaultPreference);
	}

	private static String blankToNull(String value) {
		return StringUtils.isBlank(value) ? null : value;
	}

	private static String orElse(String value, String key, ConfigurationScope scope, IContact contact, IConfigService configService) {
		return StringUtils.isNotBlank(value) ? value : read(key, scope, contact, configService);
	}

	private static String read(String key, ConfigurationScope scope, IContact contact, IConfigService configService) {
		switch (scope) {
		case GLOBAL:
			return configService.get(key, null);
		case LOCAL:
			return configService.getLocal(key, null);
		case CONTACT:
			return contact != null ? configService.get(contact, key, null) : null;
		default:
			throw new IllegalArgumentException(UNSUPPORTED_SCOPE_MSG + scope);
		}
	}

	private static void write(String key, String value, ConfigurationScope scope, IContact contact, IConfigService configService) {
		switch (scope) {
		case GLOBAL:
			configService.set(key, value);
			break;
		case LOCAL:
			configService.setLocal(key, value);
			break;
		case CONTACT:
			if (contact != null) {
				configService.set(contact, key, value);
			}
			break;
		default:
			throw new IllegalArgumentException(UNSUPPORTED_SCOPE_MSG + scope);
		}
	}
}
