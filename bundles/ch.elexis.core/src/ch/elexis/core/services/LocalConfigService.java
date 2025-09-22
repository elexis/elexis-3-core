package ch.elexis.core.services;

import static ch.elexis.core.constants.Preferences.SETTINGS_PREFERENCE_STORE_DEFAULT;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Hashtable;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Platform;
import org.slf4j.LoggerFactory;

import ch.elexis.Desk;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.utils.CoreUtil;
import ch.rgw.io.Settings;
import ch.rgw.io.SysSettings;

/**
 * Implements direct access to locally stored configuration values, without the
 * requirement of an IConfigService instance. This is e.g. required when reading
 * database connection values to subsequently initiate the IConfigService
 * itself.
 * 
 * @since 3.12
 */
public class LocalConfigService {

	private static Settings localConfig;

	static {
		SysSettings cfg = SysSettings.getOrCreate(SysSettings.USER_SETTINGS, Desk.class);
		cfg.read_xml(CoreUtil.getWritableUserDir() + File.separator + getLocalConfigFileName());
		localConfig = cfg;
		initializeDefaultPreferences();
	}

	private static String getLocalConfigFileName() {
		String[] args = Platform.getApplicationArgs();
		String config = "default"; //$NON-NLS-1$
		for (String s : args) {
			if (s.startsWith("--use-config=")) { //$NON-NLS-1$
				String[] c = s.split("="); //$NON-NLS-1$
				config = c[1];
			}
		}
		if (ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH
				.equals(System.getProperty(ElexisSystemPropertyConstants.RUN_MODE))) {
			config = UUID.randomUUID().toString();
		}
		return "localCfg_" + config + ".xml";
	}

	public static void clear() {
		localConfig.clear();
	}

	public static void flush() {
		localConfig.flush();
	}

	public static String get(String key, String defaultValue) {
		return localConfig.get(key, defaultValue);
	}

	public static boolean get(String key, boolean defaultValue) {
		return localConfig.get(key, defaultValue);
	}

	public static int get(String key, int defaultValue) {
		return localConfig.get(key, defaultValue);
	}

	public static boolean set(String key, String value) {
		boolean result;
		if (value == null) {
			localConfig.remove(key);
			result = true;
		} else {
			result = localConfig.set(key, value);
		}
		localConfig.flush();
		persist();
		return result;
	}

	public static boolean set(String key, boolean value) {
		boolean result = localConfig.set(key, value);
		localConfig.flush();
		persist();
		return result;
	}

	public static boolean set(String key, int value) {
		localConfig.set(key, value);
		localConfig.flush();
		persist();
		return true;
	}

	private static synchronized void persist() {
		SysSettings localCfg = (SysSettings) localConfig;
		String xmlFileName = CoreUtil.getWritableUserDir() + File.separator + getLocalConfigFileName();
		localCfg.write_xml(xmlFileName);
		LoggerFactory.getLogger(LocalConfigService.class).info("LocalConfig persisted to [{}]", xmlFileName);
	}

	private static void initializeDefaultPreferences() {

		// default database
		localConfig.set(Preferences.DB_NAME + SETTINGS_PREFERENCE_STORE_DEFAULT, "h2");
		String base = CoreUtil.getDefaultDBPath();

		localConfig.set(Preferences.DB_CONNECT + SETTINGS_PREFERENCE_STORE_DEFAULT,
				"jdbc:h2:" + base + "/db;MODE=MySQL"); //$NON-NLS-1$ //$NON-NLS-2$
		localConfig.set(Preferences.DB_USERNAME + SETTINGS_PREFERENCE_STORE_DEFAULT, "sa"); //$NON-NLS-1$
		localConfig.set(Preferences.DB_PWD + SETTINGS_PREFERENCE_STORE_DEFAULT, StringUtils.EMPTY);
		localConfig.set(Preferences.DB_TYP + SETTINGS_PREFERENCE_STORE_DEFAULT, "mysql"); //$NON-NLS-1$

		// create default elexis homedir
		File userhome = CoreUtil.getWritableUserDir();
		if (!userhome.exists()) {
			userhome.mkdirs();
		}

		localConfig.set(Preferences.ABL_LOGALERT + SETTINGS_PREFERENCE_STORE_DEFAULT, 1);
		localConfig.set(Preferences.ABL_LOGLEVEL + SETTINGS_PREFERENCE_STORE_DEFAULT, 2);
		localConfig.set(Preferences.ABL_BASEPATH + SETTINGS_PREFERENCE_STORE_DEFAULT, userhome.getAbsolutePath());
		localConfig.set(Preferences.ABL_CACHELIFETIME + SETTINGS_PREFERENCE_STORE_DEFAULT, 15);
		localConfig.set(Preferences.ABL_HEARTRATE + SETTINGS_PREFERENCE_STORE_DEFAULT, 30);
		localConfig.set(Preferences.ABL_BASEPATH + SETTINGS_PREFERENCE_STORE_DEFAULT, userhome.getAbsolutePath());

		String string = get(Preferences.STATION_IDENT_ID, null);
		if (string == null) {
			localConfig.set(Preferences.STATION_IDENT_ID,
					Long.toString(Timestamp.valueOf(LocalDateTime.now()).toInstant().toEpochMilli()));
		}

		// default text module
		if (get(Preferences.P_TEXTMODUL, null) == null) {
			localConfig.set(Preferences.P_TEXTMODUL, Preferences.P_TEXTMODUL_DEFAULT);
		}

		localConfig.flush();
		persist();
	}

	/**
	 * From PersistentObject#fold
	 * 
	 * @param dePrintable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static Hashtable<Object, Object> fold(byte[] flat) {
		return (Hashtable<Object, Object>) foldObject(flat);
	}

	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 *
	 * @param flat     the byte array
	 * @param resolver {@link IClassResolver} implementation used for class
	 *                 resolving / mapping
	 * @return the original Hashtable or null if no Hashtable could be created from
	 *         the array
	 */
	public static Object foldObject(final byte[] flat) {
		if (flat.length == 0) {
			return null;
		}
		try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(flat))) {
			ZipEntry entry = zis.getNextEntry();
			if (entry != null) {
				try (ObjectInputStream ois = new ObjectInputStream(zis) {
					@Override
					protected java.lang.Class<?> resolveClass(java.io.ObjectStreamClass desc)
							throws IOException, ClassNotFoundException {
						return super.resolveClass(desc);
					};
				}) {
					return ois.readObject();
				}
			} else {
				return null;
			}
		} catch (Exception ex) {
			LoggerFactory.getLogger(LocalConfigService.class).error("Error unfolding object", ex);
			return null;
		}
	}
}
