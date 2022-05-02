package ch.elexis.core.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.DBConnection;
import ch.elexis.core.common.DBConnection.DBType;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jdt.NonNull;
import ch.rgw.io.Settings;
import ch.rgw.tools.StringTool;

public class CoreUtil {

	private static Logger logger = LoggerFactory.getLogger(CoreUtil.class);

	public static enum OS {
		UNSPECIFIED, MAC, LINUX, WINDOWS
	};

	private static final OS osType;

	static {
		String osName = System.getProperty("os.name");
		if (osName.startsWith("Linux")) {
			osType = OS.LINUX;
		} else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
			osType = OS.MAC;
		} else if (osName.startsWith("Windows")) {
			osType = OS.WINDOWS;
		} else {
			osType = OS.UNSPECIFIED;
		}
	}

	/**
	 * The system is started in basic test mode, this mode enforces:<br>
	 * <ul>
	 * <li>Connection against a in mem database</li>
	 * </ul>
	 * Requires boolean parameter.
	 */
	public static final String TEST_MODE = "elexis.test.mode";

	public static boolean isTestMode() {
		String testMode = System.getProperty(TEST_MODE);
		if (testMode != null && !testMode.isEmpty()) {
			if (testMode.equalsIgnoreCase(Boolean.TRUE.toString())) {
				return true;
			}
		}
		return false;
	}

	public static Path getElexisServerHomeDirectory() {
		String userHomeProp = System.getProperty("user.home");
		File homedir = new File(new File(userHomeProp), "elexis-server");
		if (!homedir.exists()) {
			homedir.mkdir();
		}
		return Paths.get(homedir.toURI());
	}

	/**
	 * Get a {@link DBConnection} form various sources. Sources are checked in
	 * following order.<br/>
	 * <li>System Property - RunFromScratch (initializes a fresh h2 database)</li>
	 * <li>System Property - ch.elexis.dbSpec, etc.</li>
	 * <li>Provided Settings</li> <br />
	 *
	 * @param settings
	 * @return
	 */
	public static Optional<DBConnection> getDBConnection(Settings settings) {
		if (ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH
				.equals(System.getProperty(ElexisSystemPropertyConstants.RUN_MODE))) {
			DBConnection ret = new DBConnection();
			ret.connectionString = "jdbc:h2:mem:elexisFromScratch;DB_CLOSE_DELAY=-1";
			String trace = System.getProperty("elexis.test.dbtrace");
			if (trace != null && "true".equalsIgnoreCase(trace)) {
				ret.connectionString += ";TRACE_LEVEL_SYSTEM_OUT=2";
			}
			ret.rdbmsType = DBType.H2;
			ret.username = "sa";
			ret.password = StringUtils.EMPTY;
			return Optional.of(ret);
		}

		if (System.getProperty(ElexisSystemPropertyConstants.CONN_DB_SPEC) != null) {
			DBConnection dbConnection = new DBConnection();
			dbConnection.username = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_USERNAME) != null
					? System.getProperty(ElexisSystemPropertyConstants.CONN_DB_USERNAME)
					: StringUtils.EMPTY;
			dbConnection.password = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_PASSWORD) != null
					? System.getProperty(ElexisSystemPropertyConstants.CONN_DB_PASSWORD)
					: StringUtils.EMPTY;

			if (System.getProperty(ElexisSystemPropertyConstants.CONN_DB_FLAVOR) != null) {
				String flavorString = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_FLAVOR);
				dbConnection.rdbmsType = DBType.valueOfIgnoreCase(flavorString).orElseThrow(
						() -> new IllegalStateException("Unknown ch.elexis.dbFlavor [" + flavorString + "]"));
			}
			dbConnection.connectionString = System.getProperty(ElexisSystemPropertyConstants.CONN_DB_SPEC) != null
					? System.getProperty(ElexisSystemPropertyConstants.CONN_DB_SPEC)
					: StringUtils.EMPTY;
			if (dbConnection.connectionString.startsWith("jdbc:h2:")
					&& System.getProperty(ElexisSystemPropertyConstants.CONN_DB_H2_AUTO_SERVER) != null) {
				logger.info("Adding AUTO_SERVER to " + dbConnection.connectionString);
				dbConnection.connectionString = dbConnection.connectionString + ";AUTO_SERVER=TRUE";

			}

			return Optional.of(dbConnection);
		}

		Hashtable<Object, Object> hConn = getConnectionHashtable(settings);
		if (hConn != null) {
			DBConnection ret = new DBConnection();
			if (!StringUtils.isEmpty((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING))) {
				String url = (String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_CONNECTSTRING);
				url = applyMySqlTimeZoneWorkaround(url);
				ret.connectionString = url;
				DBConnection.getHostName(url).ifPresent(h -> ret.hostName = h);
				DBConnection.getDatabaseName(url).ifPresent(db -> ret.databaseName = db);
			}
			if (!StringUtils.isEmpty((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_USER))) {
				ret.username = (String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_USER);
			}
			if (!StringUtils.isEmpty((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_PASS))) {
				ret.password = (String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_PASS);
			}
			if (!StringUtils.isEmpty((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_DRIVER))) {
				Optional<DBType> type = DBType
						.valueOfDriver((String) hConn.get(Preferences.CFG_FOLDED_CONNECTION_DRIVER));
				type.ifPresent(t -> ret.rdbmsType = t);
			}
			if (ret.allValuesSet()) {
				return Optional.of(ret);
			} else {
				StringBuilder sb = new StringBuilder();
				for (Object object : hConn.keySet()) {
					if (object instanceof String) {
						sb.append(StringUtils.LF).append(object).append("->").append(hConn.get(object));
					}
				}
				logger.error("Could not get a valid DBConnection from connection setting:" + sb.toString());
			}
		}
		return Optional.empty();
	}

	/**
	 * @since 3.8 due to mysql jdbc update a timezone problem may exist, see e.g.
	 *        https://github.com/elexis/elexis-3-core/issues/273 - we fix this by
	 *        adding this parameter if not yet included
	 */
	private static String applyMySqlTimeZoneWorkaround(String dbConnectString) {
		if (dbConnectString.startsWith("jdbc:mysql:") && !dbConnectString.contains("serverTimezone")) {
			if (dbConnectString.contains("?")) {
				dbConnectString += "&serverTimezone=Europe/Zurich";
			} else {
				dbConnectString += "?serverTimezone=Europe/Zurich";
			}
			logger.info("MySQL dbConnection string correction [{}]", dbConnectString);
		}
		return dbConnectString;
	}

	/**
	 *
	 * @return a {@link Hashtable} containing the connection parameters, use
	 *         {@link Preferences#CFG_FOLDED_CONNECTION} to retrieve the required
	 *         parameters, castable to {@link String}
	 */
	public static @NonNull Hashtable<Object, Object> getConnectionHashtable(Settings settings) {
		Hashtable<Object, Object> ret = new Hashtable<>();
		String cnt = settings.get(Preferences.CFG_FOLDED_CONNECTION, null);
		if (cnt != null) {
			ret = fold(StringTool.dePrintable(cnt));
		}
		return ret;
	}

	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 *
	 * @param flat the byte array
	 * @return the original Hashtable or null if no Hashtable could be created from
	 *         the array
	 */
	@SuppressWarnings("unchecked")
	private static Hashtable<Object, Object> fold(final byte[] flat) {
		return (Hashtable<Object, Object>) foldObject(flat);
	}

	/**
	 * Recreate a Hashtable from a byte array as created by flatten()
	 *
	 * @param flat the byte array
	 *
	 * @return the original Hashtable or null if no Hashtable could be created from
	 *         the array
	 */
	private static Object foldObject(final byte[] flat) {
		return foldObject(flat, null);
	}

	/**
	 * Interface for use with
	 * {@link PersistentObject#foldObject(byte[], IClassResolver)} to map classes on
	 * deserialisation using {@link ObjectInputStream}.
	 *
	 */
	private static interface IClassResolver {
		public Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException;
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
	private static Object foldObject(final byte[] flat, IClassResolver resolver) {
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
						if (resolver != null) {
							Class<?> resolved = resolver.resolveClass(desc);
							return (resolved != null) ? resolved : super.resolveClass(desc);
						} else {
							return super.resolveClass(desc);
						}
					};
				}) {
					return ois.readObject();
				}
			} else {
				return null;
			}
		} catch (Exception ex) {
			logger.error("Error unfolding object", ex);
			return null;
		}
	}

	/**
	 * return a directory suitable for plugin specific configuration data. If no
	 * such dir exists, it will be created. If it could not be created, the
	 * application will refuse to start.
	 *
	 * @return a directory that exists always and is always writable and readable
	 *         for plugins of the currently running elexis instance. Caution: this
	 *         directory is not necessarily shared among different OS-Users. In
	 *         Windows it is normally %USERPROFILE%\elexis, in Linux ~./elexis
	 */
	public static File getWritableUserDir() {
		String userhome = null;

		if (userhome == null) {
			userhome = System.getProperty("user.home"); //$NON-NLS-1$
		}
		if (StringTool.isNothing(userhome)) {
			userhome = System.getProperty("java.io.tempdir"); //$NON-NLS-1$
		}
		File userDir = new File(userhome, "elexis"); //$NON-NLS-1$
		if (!userDir.exists()) {
			if (!userDir.mkdirs()) {
				logger.error("Panic exit, could not create userdir " + userDir.getAbsolutePath());
				System.exit(-5);
			}
		}
		return userDir;
	}

	/**
	 * Return a directory suitable for temporary files. Most probably this will be a
	 * default tempdir provided by the os. If none such exists, it will be the user
	 * dir.
	 *
	 * @return always a valid and writable directory.
	 */
	public static File getTempDir() {
		File ret = null;
		String temp = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
		if (!StringTool.isNothing(temp)) {
			ret = new File(temp);
			if (ret.exists() && ret.isDirectory()) {
				return ret;
			} else {
				if (ret.mkdirs()) {
					return ret;
				}
			}
		}
		return getWritableUserDir();
	}

	/**
	 * @return the operating system type as integer. See {@link #MAC},
	 *         {@link #LINUX}, {@link #WINDOWS} or {@link #UNSPECIFIED}
	 */
	public static final OS getOperatingSystemType() {
		return osType;
	}

	public static final boolean isWindows() {
		return osType == OS.WINDOWS;
	}

	public static final boolean isMac() {
		return osType == OS.MAC;
	}

	public static final boolean isLinux() {
		return osType == OS.LINUX;
	}
}
