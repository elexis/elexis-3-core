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

import ch.rgw.tools.StringTool;
public class UtilFile {
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
	 * Retourniert Pfad ohne Dateinamen als String
	 */
	public static String getFilepath(final String filenamePath){
		String correctFilenamePath = getCorrectSeparators(filenamePath);
		
		if (correctFilenamePath.indexOf(DIRECTORY_SEPARATOR) < 0) {
			return StringTool.leer;
		}
		return correctFilenamePath.substring(0,
			correctFilenamePath.lastIndexOf(DIRECTORY_SEPARATOR));
	}
}
