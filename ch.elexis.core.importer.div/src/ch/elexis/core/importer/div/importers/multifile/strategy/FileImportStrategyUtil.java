package ch.elexis.core.importer.div.importers.multifile.strategy;

import java.io.File;

import org.slf4j.LoggerFactory;

import ch.rgw.tools.TimeTool;

public class FileImportStrategyUtil {
	
	/**
	 * Move file to archive or error directory depending on the ok parameter.
	 * 
	 * @param ok
	 * @param file
	 */
	public static void moveAfterImport(boolean ok, File file){
		File rootDir = file.getParentFile();
		File subDir = null;
		if (ok) {
			subDir = getOrCreateSubdir(rootDir, "archive");
		} else {
			subDir = getOrCreateSubdir(rootDir, "error");
		}
		if (subDir != null) {
			moveToDir(file, subDir);
		}
	}
	
	private static void moveToDir(File file, File subDir){
		File newFile = new File(subDir, file.getName());
		
		if (newFile.exists()) {
			// on multiple move to archive dir:
			// first time use own filename
			// n+ times use filename_timestamp
			String fnwts = file.getName() + "_" + new TimeTool().toString(TimeTool.TIMESTAMP);
			newFile = new File(subDir, fnwts);
		}
		
		if (!file.renameTo(newFile)) {
			LoggerFactory.getLogger(FileImportStrategyUtil.class).error("Could not move file ["
				+ file.getAbsolutePath() + "] to [" + newFile.getAbsolutePath() + "]");
		}
	}
	
	private static File getOrCreateSubdir(File dir, String string){
		File subDir = new File(dir, string);
		if (!subDir.exists()) {
			subDir.mkdir();
		}
		return subDir;
	}
}
