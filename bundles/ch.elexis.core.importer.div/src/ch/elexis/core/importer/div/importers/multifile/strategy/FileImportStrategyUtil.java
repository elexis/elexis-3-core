package ch.elexis.core.importer.div.importers.multifile.strategy;

import java.io.File;
import java.io.IOException;

import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.rgw.tools.TimeTool;

public class FileImportStrategyUtil {
	
	/**
	 * Move file to archive or error directory depending on the ok parameter.
	 * 
	 * @param ok
	 * @param file
	 * @throws IOException 
	 */
	public static void moveAfterImport(boolean ok, IVirtualFilesystemHandle fileHandle) throws IOException{
		IVirtualFilesystemHandle rootDir = fileHandle.getParent();
		IVirtualFilesystemHandle subDir = null;
		if (ok) {
			subDir = getOrCreateSubdir(rootDir, "archive");
		} else {
			subDir = getOrCreateSubdir(rootDir, "error");
		}
		if (subDir != null) {
			moveToDir(fileHandle, subDir);
		}
	}
	
	private static void moveToDir(IVirtualFilesystemHandle file, IVirtualFilesystemHandle subDir)
		throws IOException{
		File _subDir = subDir.toFile().orElse(null);
		
		IVirtualFilesystemHandle newFile =
				VirtualFilesystemServiceHolder.get().of(new File(_subDir, file.getName()));
		
		if (newFile.exists()) {
			// on multiple move to archive dir:
			// first time use own filename
			// n+ times use filename_timestamp
			String fnwts = file.getName() + "_" + new TimeTool().toString(TimeTool.TIMESTAMP);
			newFile = VirtualFilesystemServiceHolder.get().of(new File(_subDir, fnwts));
		}
		
		if (!file.moveTo(newFile)) {
			LoggerFactory.getLogger(FileImportStrategyUtil.class).error("Could not move file ["
				+ file.getAbsolutePath() + "] to [" + newFile.getAbsolutePath() + "]");
		}
	}
	
	private static IVirtualFilesystemHandle getOrCreateSubdir(IVirtualFilesystemHandle dir,
		String string) throws IOException{
		IVirtualFilesystemHandle subDir = dir.subDir(string);
		if (!subDir.exists()) {
			subDir.mkdir();
		}
		return subDir;
	}
}
