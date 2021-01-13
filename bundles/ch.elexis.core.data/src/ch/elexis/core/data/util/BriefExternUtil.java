package ch.elexis.core.data.util;

import java.io.File;

import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.data.Brief;

/**
 * Utility class for handling extern storage of {@link Brief}.
 * 
 * @author thomas
 *
 */
public class BriefExternUtil {

	public static String getExternFilePath(){
		return getAsExternFilePath(
			ConfigServiceHolder.getGlobal(Preferences.P_TEXT_EXTERN_FILE_PATH, null));
	}
	
	public static String getAsExternFilePath(String path){
		if (path != null && path.contains("[home]")) {
			path = path.replace("[home]", CoreHub.getWritableUserDir().getAbsolutePath());
			LoggerFactory.getLogger(BriefExternUtil.class)
				.warn("Replaced [home] -> [" + CoreHub.getWritableUserDir().getAbsolutePath()
					+ "] in extern file path result is [" + path + "]");
		}
		return path;
	}
	
	/**
	 * Test if the configured Path is available.
	 * 
	 * @param string
	 * 
	 * @return
	 */
	public static boolean isValidExternPath(String path, boolean log){
		if (path != null) {
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory() && dir.canWrite()) {
				return true;
			} else {
				if (log) {
					LoggerFactory.getLogger(BriefExternUtil.class)
						.warn("Configured path [" + path + "] not valid e=" + dir.exists() + " d="
							+ dir.isDirectory() + " w=" + dir.canWrite());
				}
			}
		} else if (log) {
			LoggerFactory.getLogger(BriefExternUtil.class).warn("No path configured");
		}
		return false;
	}
	

}
