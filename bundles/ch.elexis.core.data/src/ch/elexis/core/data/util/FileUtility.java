/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.data.util;

import java.io.File;

import ch.elexis.core.utils.FileUtil;

/**
 * @deprecated Use org.apache.commons.io.FileUtils or {@link FileUtil}
 * @author med1
 *
 */
import ch.rgw.tools.StringTool;
public class FileUtility {
	public static String DIRECTORY_SEPARATOR = File.separator;
	
	public static final String ZIP_EXTENSION = ".gz"; //$NON-NLS-1$
	
	private static String getCorrectSeparators(final String pathOrFilename){
		return pathOrFilename.replace("\\", DIRECTORY_SEPARATOR).replace("//", //$NON-NLS-1$ //$NON-NLS-2$
			DIRECTORY_SEPARATOR).replace(StringTool.slash, DIRECTORY_SEPARATOR); //$NON-NLS-1$
	}
	
	private static String removeMultipleSeparators(String pathOrFilename){
		String doubleSeparator = DIRECTORY_SEPARATOR + DIRECTORY_SEPARATOR;
		if (pathOrFilename.indexOf(doubleSeparator) >= 0) {
			pathOrFilename = pathOrFilename.replace(doubleSeparator, DIRECTORY_SEPARATOR);
		}
		return pathOrFilename;
	}
	
	/**
	 * Überprüft ob Verzeichnis korrekt ist. Falls nicht, wird das Verzeichnis korrigiert.
	 * 
	 * @param path
	 *            oder null
	 */
	public static String getCorrectPath(String path) throws IllegalArgumentException{
		if (path == null) {
			return StringTool.leer; //$NON-NLS-1$
		}
		path = getCorrectSeparators(path);
		path = removeMultipleSeparators(path);
		if (!path.endsWith(DIRECTORY_SEPARATOR)) {
			path += DIRECTORY_SEPARATOR;
		}
		return path;
	}
	
	/**
	 * Überprüft, ob eine Datei existiert
	 */
	public static boolean doesFileExist(final String filePathName){
		File file = new File(filePathName);
		return file.isFile() && file.exists();
	}
	
	/**
	 * Überprüft, ob es sich um ein absolutes Verzeichnis handelt
	 */
	public static boolean isRootDir(String dir){
		return (dir.startsWith(DIRECTORY_SEPARATOR) || dir.indexOf(":") > 0);// Linux & Windows Root //$NON-NLS-1$
	}
	
	/**
	 * Löscht Datei
	 */
	public static void deleteFile(final String filePathName) throws IllegalArgumentException{
		if (doesFileExist(filePathName)) {
			File file = new File(filePathName);
			file.delete();
		}
	}
	
	/**
	 * Retourniert Pfad ohne Dateinamen als String
	 */
	public static String getFilepath(final String filenamePath){
		String correctFilenamePath = getCorrectSeparators(filenamePath);
		
		if (correctFilenamePath.indexOf(DIRECTORY_SEPARATOR) < 0) {
			return StringTool.leer; //$NON-NLS-1$
		}
		return correctFilenamePath.substring(0,
			correctFilenamePath.lastIndexOf(DIRECTORY_SEPARATOR));
	}
	
	/**
	 * Retourniert Dateinamen ohne Pfad als String
	 */
	public static String getFilename(final String filenamePath){
		String correctFilenamePath = getCorrectSeparators(filenamePath);
		
		if (correctFilenamePath.indexOf(DIRECTORY_SEPARATOR) < 0) {
			return filenamePath;
		}
		return correctFilenamePath.substring(
			correctFilenamePath.lastIndexOf(DIRECTORY_SEPARATOR) + 1, correctFilenamePath.length());
	}
	
	/**
	 * Retourniert Dateinamen ohne Pfad und Endung. Falls keine Endung vorhanden ist, wird der
	 * Dateinamen retourniert.
	 */
	public static String getNakedFilename(final String filenamePath){
		String filename = getFilename(filenamePath);
		
		if (filename.lastIndexOf(".") > 0) { //$NON-NLS-1$
			return filename.substring(0, filename.lastIndexOf(".")); //$NON-NLS-1$
		}
		
		return filename;
	}
	
	/**
	 * Retourniert Dateiendung (mit Punkt). Falls keine Endung gefunden wird, wird ein leerer String
	 * retourniert.
	 */
	public static String getFileExtension(final String filenamePath){
		String filename = getFilename(filenamePath);
		
		if (filename.lastIndexOf(".") > 0) { //$NON-NLS-1$
			return filename.substring(filename.lastIndexOf("."), filename.length()); //$NON-NLS-1$
			
		}
		
		return StringTool.leer; //$NON-NLS-1$
	}
}
