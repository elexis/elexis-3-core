package ch.elexis.core.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.rgw.tools.TimeTool;

/**
 * Main FileUtily class, if possible use org.apache.commons.io.FileUtils or if
 * not supported implement the functionality in this class.
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
	public static String removeInvalidChars(String filename) {
		if (filename != null) {
			return filename.replaceAll("[\\\\/:*?\"<>|]", StringUtils.EMPTY);
		}
		return filename;
	}

	/**
	 * Move the file to a new parent directory of the current file path
	 *
	 * @param file
	 * @param parentDirName
	 */
	public static void moveFileToParentDir(File file, String parentDirName) {
		File rootDir = file.getParentFile();
		File subDir = getOrCreateSubdir(rootDir, parentDirName);
		if (subDir != null) {
			moveToDir(file, subDir);
		}
	}

	/**
	 * Move the file to a new parent directory of the current file path
	 *
	 * @param fileHandle
	 * @param subDirName name of the subdir relative to fileHandle location
	 * @return fileHandle representing the new location
	 * @throws IOException
	 */
	public static IVirtualFilesystemHandle moveFileToSubDir(IVirtualFilesystemHandle fileHandle, String subDirName)
			throws IOException {
		IVirtualFilesystemHandle rootDir = fileHandle.getParent();
		IVirtualFilesystemHandle subDir = getOrCreateSubdir(rootDir, subDirName);
		return moveToDir(fileHandle, subDir);
	}

	private static void moveToDir(File file, File subDir) {
		File newFile = new File(subDir, file.getName());

		if (newFile.exists()) {
			// on multiple move to archive dir:
			// first time use own filename
			// n+ times use filename_timestamp
			String fnwts = file.getName() + "_" + new TimeTool().toString(TimeTool.TIMESTAMP);
			newFile = new File(subDir, fnwts);
		}

		if (!file.renameTo(newFile)) {
			LoggerFactory.getLogger(FileUtil.class).error(
					"Could not move file [" + file.getAbsolutePath() + "] to [" + newFile.getAbsolutePath() + "]");
		} else {
			LoggerFactory.getLogger(FileUtil.class).debug("The new file location is: " + newFile.getAbsolutePath());
		}

	}

	private static File getOrCreateSubdir(File dir, String string) {
		File subDir = new File(dir, string);
		if (!subDir.exists()) {
			subDir.mkdir();
		}
		return subDir;
	}

	/**
	 * Move a file to an existing subDir, if the file already exists
	 *
	 * @param file
	 * @param subDir
	 * @return the new handle of the moved file
	 * @throws IOException
	 */
	public static IVirtualFilesystemHandle moveToDir(IVirtualFilesystemHandle file, IVirtualFilesystemHandle subDir)
			throws IOException {

		IVirtualFilesystemHandle newFile = subDir.subFile(file.getName());

		if (newFile.exists()) {
			// on multiple move to archive dir:
			// first time use own filename
			// n+ times use filename_timestamp
			String fnwts = file.getName() + "_" + new TimeTool().toString(TimeTool.TIMESTAMP);
			newFile = subDir.subFile(fnwts);
		}

		file.moveTo(newFile);

		return newFile;
	}

	private static IVirtualFilesystemHandle getOrCreateSubdir(IVirtualFilesystemHandle dir, String string)
			throws IOException {
		IVirtualFilesystemHandle subDir = dir.subDir(string);
		if (!subDir.exists()) {
			subDir.mkdir();
		}
		return subDir;
	}
}
