package ch.elexis.core.utils;

public class CoreUtil {
	/**
	 * The system is started in basic test mode, this mode enforces:<br>
	 * <ul>
	 * <li>Connection against a in mem database</li>
	 * </ul>
	 * Requires boolean parameter.
	 */
	public static final String TEST_MODE = "elexis.test.mode";
	
	public static boolean isTestMode(){
		String testMode = System.getProperty(TEST_MODE);
		if (testMode != null && !testMode.isEmpty()) {
			if (testMode.equalsIgnoreCase(Boolean.TRUE.toString())) {
				return true;
			}
		}
		return false;
	}
}
