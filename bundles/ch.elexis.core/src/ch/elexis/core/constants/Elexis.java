package ch.elexis.core.constants;

import org.osgi.framework.FrameworkUtil;

public final class Elexis {

	public static final String VERSION;
	public static final String APPLICATION_NAME = "Elexis Core"; //$NON-NLS-1$

	static {
		VERSION = initElexisBuildVersion();
	}

	private static String initElexisBuildVersion() {
		return FrameworkUtil.getBundle(Elexis.class).getVersion().toString();
	}
}
