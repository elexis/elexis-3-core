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

	/**
	 * Read an OS specific value from the global configuration. Lookup order:
	 * <ol>
	 * <li>global <code>&lt;key&gt;_&lt;OS&gt;</code></li>
	 * <li>global <code>&lt;key&gt;</code></li>
	 * </ol>
	 *
	 * @param defaultPreference the key without OS suffix
	 * @param configService
	 * @return the value found, or an empty String
	 */
	public static String getOsSpecificPreference(String defaultPreference, IConfigService configService) {
		String osSpecificPreference = getOsSpecificPreferenceName(CoreUtil.getOperatingSystemType(), defaultPreference);
		String value = configService.get(osSpecificPreference, null);

		if (StringUtils.isBlank(value)) {
			LOG.debug("No OS specific value for [{}], reverting to generic setting", osSpecificPreference);
			value = configService.get(defaultPreference, StringUtils.EMPTY);
		}
		return value;
	}

	/**
	 * Same as {@link #getOsSpecificGlobalPreference(String, IConfigService, boolean)}
	 * with <code>fallbackToLocal</code> <code>true</code>.
	 */
	public static String getOsSpecificGlobalPreference(String defaultPreference, IConfigService configService) {
		return getOsSpecificGlobalPreference(defaultPreference, configService, true);
	}

	/**
	 * Read an OS specific value from the global configuration. Lookup order:
	 * <ol>
	 * <li>global <code>&lt;key&gt;_&lt;OS&gt;</code></li>
	 * <li>global <code>&lt;key&gt;</code></li>
	 * <li>local <code>&lt;key&gt;</code>, only if <code>fallbackToLocal</code>
	 * is <code>true</code></li>
	 * </ol>
	 *
	 * @param defaultPreference the key without OS suffix
	 * @param configService
	 * @param fallbackToLocal   read the local configuration if nothing is set
	 *                          globally
	 * @return the value found, or <code>null</code>
	 */
	public static String getOsSpecificGlobalPreference(String defaultPreference, IConfigService configService,
			boolean fallbackToLocal) {
		String osSpecificPreference = osSpecificName(defaultPreference);
		String value = read(osSpecificPreference, ConfigurationScope.GLOBAL, null, configService);
		if (StringUtils.isBlank(value)) {
			value = read(defaultPreference, ConfigurationScope.GLOBAL, null, configService);
		}
		if (fallbackToLocal && StringUtils.isBlank(value)) {
			value = read(defaultPreference, ConfigurationScope.LOCAL, null, configService);
		}

		return blankToNull(value);
	}

	/**
	 * Same as {@link #getOsSpecificLocalPreference(String, IConfigService, boolean)}
	 * with <code>fallbackToGlobal</code> <code>true</code>.
	 */
	public static String getOsSpecificLocalPreference(String defaultPreference, IConfigService configService) {
		return getOsSpecificLocalPreference(defaultPreference, configService, true);
	}

	/**
	 * Read an OS specific value from the local configuration. Lookup order:
	 * <ol>
	 * <li>local <code>&lt;key&gt;_&lt;OS&gt;</code></li>
	 * <li>local <code>&lt;key&gt;</code></li>
	 * <li>global <code>&lt;key&gt;</code>, only if <code>fallbackToGlobal</code>
	 * is <code>true</code></li>
	 * </ol>
	 *
	 * @param defaultPreference the key without OS suffix
	 * @param configService
	 * @param fallbackToGlobal  read the global configuration if nothing is set
	 *                          locally
	 * @return the value found, or <code>null</code>
	 */
	public static String getOsSpecificLocalPreference(String defaultPreference, IConfigService configService,
			boolean fallbackToGlobal) {
		String osSpecificPreference = osSpecificName(defaultPreference);
		String value = read(osSpecificPreference, ConfigurationScope.LOCAL, null, configService);
		if (StringUtils.isBlank(value)) {
			value = read(defaultPreference, ConfigurationScope.LOCAL, null, configService);
		}
		if (fallbackToGlobal && StringUtils.isBlank(value)) {
			value = read(defaultPreference, ConfigurationScope.GLOBAL, null, configService);
		}

		return blankToNull(value);
	}

	/**
	 * Same as
	 * {@link #getOsSpecificContactPreference(String, IContact, IConfigService, boolean)}
	 * with <code>fallbackToGlobal</code> <code>true</code>.
	 */
	public static String getOsSpecificContactPreference(String defaultPreference, IContact contact,
			IConfigService configService) {
		return getOsSpecificContactPreference(defaultPreference, contact, configService, true);
	}

	/**
	 * Read an OS specific value from the configuration of a contact. Lookup
	 * order:
	 * <ol>
	 * <li>contact <code>&lt;key&gt;_&lt;OS&gt;</code></li>
	 * <li>contact <code>&lt;key&gt;</code></li>
	 * <li>global <code>&lt;key&gt;_&lt;OS&gt;</code>, only if
	 * <code>fallbackToGlobal</code> is <code>true</code></li>
	 * <li>global <code>&lt;key&gt;</code>, only if <code>fallbackToGlobal</code>
	 * is <code>true</code></li>
	 * </ol>
	 *
	 * @param defaultPreference the key without OS suffix
	 * @param contact
	 * @param configService
	 * @param fallbackToGlobal  read the global configuration if nothing is set for
	 *                          the contact
	 * @return the value found, or <code>null</code>
	 */
	public static String getOsSpecificContactPreference(String defaultPreference, IContact contact,
			IConfigService configService, boolean fallbackToGlobal) {
		String osSpecificPreference = osSpecificName(defaultPreference);
		String value = read(osSpecificPreference, ConfigurationScope.CONTACT, contact, configService);
		if (StringUtils.isBlank(value)) {
			value = read(defaultPreference, ConfigurationScope.CONTACT, contact, configService);
		}
		if (fallbackToGlobal && StringUtils.isBlank(value)) {
			value = read(osSpecificPreference, ConfigurationScope.GLOBAL, null, configService);
		}
		if (fallbackToGlobal && StringUtils.isBlank(value)) {
			value = read(defaultPreference, ConfigurationScope.GLOBAL, null, configService);
		}

		return blankToNull(value);
	}

	private static String osSpecificName(String defaultPreference) {
		return getOsSpecificPreferenceName(CoreUtil.getOperatingSystemType(), defaultPreference);
	}

	private static String blankToNull(String value) {
		return StringUtils.isBlank(value) ? null : value;
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
}
