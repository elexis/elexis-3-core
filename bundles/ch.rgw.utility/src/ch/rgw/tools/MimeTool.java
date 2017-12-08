/*******************************************************************************
 * Copyright (c) 2008, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.rgw.tools;

public class MimeTool {
	final static String[][] mapping = {
		{
			"pdf", "application/pdf"
		}, {
			"odt", "application/vnd.oasis.opendocument.text"
		}, {
			"jpg", "image/jpeg"
		}, {
			"pps", "application/mspowerpoint"
		}, {
			"png", "image/png"
		}
	};
	
	/**
	 * find the mimetype for a given extension
	 * 
	 * @param ext
	 *            an extension with or without dot. Case doesn't matter
	 * @return the matching mimetype or application/octet-stream if none was found, or the empty
	 *         string if ext was null
	 */
	public static String getMimeType(String ext){
		if (ext == null) {
			return "";
		}
		if (ext.startsWith(".")) {
			ext = ext.substring(1);
		}
		for (String[] line : mapping) {
			if (line[0].equalsIgnoreCase(ext)) {
				return line[1];
			}
		}
		return "application/octet-stream";
	}
	
	/**
	 * find the file extension for a given mimetype. If more than one extension is known for a
	 * mimetyoe, only the first one will be chosen
	 * 
	 * @param mimetype
	 *            a mimetype. Case doesn't matter
	 * @return the matching extension or the empty string if none was found
	 */
	public static String getExtension(String mimetype){
		if (mimetype == null) {
			return "";
		}
		for (String[] line : mapping) {
			if (line[1].equalsIgnoreCase(mimetype)) {
				return line[0];
			}
		}
		return "";
	}
}
