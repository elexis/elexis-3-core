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

package ch.elexis.core.data.interfaces.text;

import java.io.InputStream;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;

/**
 * A Contract for a piece of Information in an arbitrary format - a document - that can be stored
 * and retrieved in the system. The IOpaqueDocument holds the reference to the original contents and
 * some metadata. The Interface has no means to analyze the contents of the document. thus Opaque.
 * 
 * @author gerry
 * 
 */
public interface IOpaqueDocument {
	
	/**
	 * Title for the document. Not more than 80 characters. Must not be null and must not be empty
	 */
	public String getTitle();
	
	/**
	 * Mime-Type of the contents.
	 * 
	 * @return The MimeType (see rfc2045) of the document. May be null or empty if not known
	 */
	public String getMimeType();
	
	/**
	 * A representation of the contents a stream
	 * 
	 * @return an InpputStream
	 * @throws ElexisException
	 */
	public InputStream getContentsAsStream() throws ElexisException;
	
	/**
	 * A representation of the contents as byte array
	 * 
	 * @return a byte array with the original contents
	 * @throws ElexisException
	 */
	public byte[] getContentsAsBytes() throws ElexisException;
	
	/**
	 * Arbitrary keywords for this document
	 * 
	 * @return null or empty or a list of keywords, separated by comma or space
	 */
	public String getKeywords();
	
	/**
	 * Category of the document. This is an arbitrary, user-defined String
	 * 
	 * @return null or empty or the category for this document
	 */
	public String getCategory();
	
	/**
	 * Date this document was created
	 * 
	 * @return a date in ISO Format. Never null
	 */
	public String getCreationDate();
	
	/**
	 * The Patient this document belongs to.
	 * 
	 * @return A Patient or null if the document does not belong to a patient.
	 */
	public Patient getPatient();
	
	/**
	 * A globally unique identifier for this document. The implementation must ensure that (1) No
	 * two documents in the world ever exist with the same GUID, and (2) A document will always have
	 * the same GUID assigned within this application, i.e. the call getGUID on the same document
	 * will always return the same value.
	 * 
	 * @return
	 */
	public String getGUID();
}
