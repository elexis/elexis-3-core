package ch.elexis.core.utils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.rgw.tools.StringTool;

public class CoreUtil {

	public static enum OS {
		UNSPECIFIED, MAC, LINUX, WINDOWS
	};

	/**
	 * The system is started in basic test mode, this mode enforces:<br>
	 * <ul>
	 * <li>Connection against a in mem database</li>
	 * </ul>
	 * Requires boolean parameter.
	 */
	public static final String TEST_MODE = "elexis.test.mode"; //$NON-NLS-1$
	public static final String HOME_MODE = "ch.elexis.home"; //$NON-NLS-1$
	private static Logger logger = LoggerFactory.getLogger(CoreUtil.class);

	private static final OS osType;
	private static final boolean testMode;

	static {
		String osName = System.getProperty("os.name"); //$NON-NLS-1$
		if (osName.startsWith("Linux")) { //$NON-NLS-1$
			osType = OS.LINUX;
		} else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) { //$NON-NLS-1$ //$NON-NLS-2$
			osType = OS.MAC;
		} else if (osName.startsWith("Windows")) { //$NON-NLS-1$
			osType = OS.WINDOWS;
		} else {
			osType = OS.UNSPECIFIED;
		}

		testMode = Boolean.valueOf(System.getProperty(TEST_MODE));
	}

	public static boolean isTestMode() {
		return testMode;
	}

	public static Path getElexisServerHomeDirectory() {
		String userHomeProp = System.getProperty("user.home"); //$NON-NLS-1$
		File homedir = new File(new File(userHomeProp), "elexis-server"); //$NON-NLS-1$
		if (!homedir.exists()) {
			homedir.mkdir();
		}
		return Paths.get(homedir.toURI());
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
		String homeProp = System.getProperty(HOME_MODE); // $NON-NLS-1$
		File userDir;
		if (StringUtils.isNotBlank(homeProp)) {
			File baseDir = new File(System.getProperty("user.dir")); //$NON-NLS-1$
			userDir = new File(baseDir, homeProp);
		} else {
			String userhome = System.getProperty("user.home"); //$NON-NLS-1$
			if (StringUtils.isBlank(userhome)) {
				userhome = System.getProperty("java.io.tmpdir"); //$NON-NLS-1$
			}
			userDir = new File(userhome, "elexis"); //$NON-NLS-1$
		}

		if (!userDir.exists()) {
			if (!userDir.mkdirs()) {
				logger.error("Panic exit, could not create userdir " + userDir.getAbsolutePath()); //$NON-NLS-1$
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

	public static String getDefaultDBPath() {
		String base = System.getProperty("user.home") + "/elexisdata"; //$NON-NLS-1$ //$NON-NLS-2$
		File f = new File(base);
		if (!f.exists()) {
			f.mkdirs();
		}
		return base;
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
