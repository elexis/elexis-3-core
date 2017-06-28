package ch.elexis.core.utils;

/**
 * Main FileUtily class, if possible use org.apache.commons.io.FileUtils or if not supported
 * implement the functionality in this class.
 * 
 * @author med1
 *
 */
public class FileUtil {
	
	/**
	 * Removes invalid chars from a filename
	 * 
	 * @param filename
	 * @return
	 */
	public static String removeInvalidChars(String filename){
		if (filename != null) {
			return filename.replaceAll("[\\\\/:*?\"<>|]", "");
		}
		return filename;
	}
}
