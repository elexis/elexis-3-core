package ch.elexis.core.importer.div.importers.multifile.strategy;

import java.io.IOException;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.utils.FileUtil;

public class FileImportStrategyUtil {
	
	/**
	 * Move file to archive or error directory depending on the ok parameter.
	 * 
	 * @param ok
	 * @param file
	 * @throws IOException
	 */
	public static void moveAfterImport(boolean ok, IVirtualFilesystemHandle fileHandle)
		throws IOException{
		FileUtil.moveFileToParentDir(fileHandle, ok ? "archive" : "error");
	}
}
