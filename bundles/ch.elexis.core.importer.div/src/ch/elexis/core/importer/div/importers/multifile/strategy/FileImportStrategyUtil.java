package ch.elexis.core.importer.div.importers.multifile.strategy;

import java.io.IOException;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.utils.FileUtil;

public class FileImportStrategyUtil {
	
	public static final String DIRECTORY_NAME_ON_ERROR = "error";
	public static final String DIRECTORY_NAME_ARCHIVE = "archive";
	
	/**
	 * Move file to archive or error directory depending on the ok parameter.
	 * 
	 * @param ok
	 *            <code>true</code> if the import was successful
	 * @param file
	 * @return fileHandle representing the new location
	 * @throws IOException
	 */
	public static IVirtualFilesystemHandle moveAfterImport(boolean ok,
		IVirtualFilesystemHandle fileHandle) throws IOException{
		
		IVirtualFilesystemHandle baseDir = fileHandle.getParent();
		String name = baseDir.getName();
		if (DIRECTORY_NAME_ON_ERROR.equals(name) || DIRECTORY_NAME_ARCHIVE.equals(name)) {
			// we are already in an import result subdirectory, switch to parent
			baseDir = fileHandle.getParent().getParent();
		}
		
		return FileUtil.moveToDir(fileHandle,
			baseDir.subDir(ok ? DIRECTORY_NAME_ARCHIVE : DIRECTORY_NAME_ON_ERROR));
	}
}
