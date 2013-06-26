/*******************************************************************************
 * Copyright (c) 2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ch.elexis.core.data.Patient;
import ch.elexis.core.data.interfaces.text.IOpaqueDocument;
import ch.elexis.core.exceptions.ElexisException;
import ch.rgw.io.FileTool;
import ch.rgw.tools.StringTool;

/**
 * An IDocument implementation, based on a File
 * 
 * @author gerry
 * 
 */
public class GenericDocument implements IOpaqueDocument {
	String title;
	String category;
	byte[] contents;
	String date;
	Patient pat;
	String keywords;
	String mimetype;
	String guid = StringTool.unique("FileDocument");
	
	/**
	 * Create a new GenericDocument from a File.
	 * 
	 * @param pat
	 *            The patient this document belongs to. Can be null
	 * @param title
	 *            Title for the document. Never Null and Never empty
	 * @param category
	 *            Category for the document. Can be null or empty
	 * @param file
	 *            File to import in this document
	 * @param date
	 *            date of creation
	 * @param keywords
	 *            space- or comma- separated list of keywords. May be empty or null
	 */
	public GenericDocument(Patient pat, String title, String category, File file, String date,
		String keywords, String mimetype) throws IOException{
		this.title = title;
		this.category = category;
		this.date = date;
		this.pat = pat;
		this.keywords = keywords;
		this.mimetype = mimetype == null ? file.getName() : mimetype;
		FileInputStream fis = new FileInputStream(file);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		FileTool.copyStreams(fis, baos);
		fis.close();
		contents = baos.toByteArray();
		baos.close();
	}
	
	/**
	 * Create a new GenericDocument from a File.
	 * 
	 * @param pat
	 *            The patient this document belongs to. Can be null
	 * @param title
	 *            Title for the document. Never Null and Never empty
	 * @param category
	 *            Category for the document. Can be null or empty
	 * @param content
	 *            Content as byte array
	 * @param date
	 *            date of creation
	 * @param keywords
	 *            space- or comma- separated list of keywords. May be empty or null
	 */
	public GenericDocument(Patient pat, String title, String category, byte[] content, String date,
		String keywords, String mimetype) throws IOException{
		this.title = title;
		this.category = category;
		this.date = date;
		this.pat = pat;
		this.keywords = keywords;
		this.mimetype = mimetype;
		// make a copy of the content, as we do not know if source will get changed ...
		contents = new byte[content.length];
		System.arraycopy(content, 0, contents, 0, content.length);
	}
	
	@Override
	public String getTitle(){
		return title;
	}
	
	@Override
	public String getMimeType(){
		return mimetype == null ? "binary/octet-stream" : mimetype;
	}
	
	/**
	 * Return the contents of this document as Stream Note: The caller must ensure that the stream
	 * is closed after using it.
	 */
	@Override
	public InputStream getContentsAsStream() throws ElexisException{
		ByteArrayInputStream bais = new ByteArrayInputStream(contents);
		return bais;
	}
	
	public byte[] getContentsAsBytes() throws ElexisException{
		return contents;
	}
	
	@Override
	public String getKeywords(){
		return keywords;
	}
	
	@Override
	public String getCategory(){
		return category;
	}
	
	@Override
	public String getCreationDate(){
		return date;
	}
	
	@Override
	public Patient getPatient(){
		return pat;
	}
	
	@Override
	public String getGUID(){
		return guid;
	}
	
}
