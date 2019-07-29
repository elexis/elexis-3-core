package ch.elexis.core.importer.div.importers.multifile.strategy;

import java.io.File;

import ch.elexis.core.utils.FileUtil;

public class FileImportStrategyUtil {
	
	/**
	 * Move file to archive or error directory depending on the ok parameter.
	 * 
	 * @param ok
	 * @param file
	 */
	public static void moveAfterImport(boolean ok, File file){
		FileUtil.moveFileToParentDir(file, ok ? "archive" : "error");
	}
}
