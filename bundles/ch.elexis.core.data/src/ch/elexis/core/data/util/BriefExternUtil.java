package ch.elexis.core.data.util;

import java.io.File;

import org.slf4j.LoggerFactory;

import ch.elexis.data.Brief;

/**
 * Utility class for handling extern storage of {@link Brief}.
 * 
 * @author thomas
 *
 */
public class BriefExternUtil {
	
	
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
