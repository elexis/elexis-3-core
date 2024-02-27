package ch.elexis.core.services;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.Platform;
import org.slf4j.LoggerFactory;

import ch.elexis.Desk;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
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
		localCfg.write_xml(CoreUtil.getWritableUserDir() + File.separator + getLocalConfigFileName());
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
