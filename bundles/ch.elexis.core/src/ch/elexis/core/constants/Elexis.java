package ch.elexis.core.constants;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.osgi.framework.FrameworkUtil;
import org.slf4j.LoggerFactory;

public final class Elexis {

	public static final String VERSION;
	public static final String APPLICATION_NAME = "Elexis Core"; //$NON-NLS-1$

	static {
		VERSION = initElexisBuildVersion();
	}

	private static String initElexisBuildVersion() {
		String version = FrameworkUtil.getBundle(Elexis.class).getVersion().toString();
		String url_name = "platform:/plugin/ch.elexis.core.data/rsc/version.properties";
		try (InputStream inputStream = new URL(url_name).openConnection().getInputStream()) {
			if (inputStream != null) {
				Properties prop = new Properties();
				prop.load(inputStream);
				version = prop.getProperty("elexis.version");
				LoggerFactory.getLogger(Elexis.class).trace("set version from " + url_name + " to " + version);
			}
		} catch (IOException e) {
			LoggerFactory.getLogger(Elexis.class).info(url_name + " not found", e);
		} ;
		return version;
	}
}
