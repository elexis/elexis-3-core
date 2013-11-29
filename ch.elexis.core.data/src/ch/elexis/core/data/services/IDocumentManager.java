/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.data.services;

import java.io.InputStream;
import java.util.List;

import ch.elexis.core.data.interfaces.text.IOpaqueDocument;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.rgw.tools.TimeSpan;

/**
 * A Service acting as DocumentManagement must implement this Interfache
 * 
 * @author gerry
 * 
 */
public interface IDocumentManager {
	
	/** List Categories for Documents */
	public String[] getCategories();
	
	/** Add a Categorie */
	public boolean addCategorie(String categorie);
	
	/**
	 * Ad a document
	 * 
	 * @param doc
	 *            The IDocument to add
	 * @return ID of the newly created internal representation
	 * @throws ElexisException
	 */
	public String addDocument(IOpaqueDocument doc) throws ElexisException;
	
	/**
	 * remove and delete a document
	 * 
	 * @param guid
	 *            the guid of the doucment to remove
	 * @return true on success
	 */
	public boolean removeDocument(String guid);
	
	/**
	 * Render a Document to a Stream
	 * 
	 * @param id
	 *            ID of the Object (as generated with addDocument)
	 * @return
	 */
	public InputStream getDocument(String id);
	
	/**
	 * Retrieve documents with matching criteria. If a parameter is null, it will be considered as
	 * "any". If a String parameter is enclosed in slashes, it will be considered as regex:
	 * "/m[ae]h/" will match mah and meh, while "m[ae]h" will only match the literal string m[ae]h.
	 * 
	 * @param pat
	 *            The patient the documents belong to
	 * @param categoryMatch
	 *            the category or categories to match
	 * @param titleMatch
	 *            title
	 * @param keywordMatch
	 *            keyword to find. Will match if at least one of the documents keywords match the
	 *            parameter
	 * @param dateMatch
	 *            match only documents woth dates within the given timespan
	 * @param contentsMatch
	 *            find a match in the contents of the document. Note: This is not supported by all
	 *            documentmanagers and it might be very inefficient! If the parameter is not null
	 *            and the implementation does not support contentMatch, it throws an ElexisException
	 *            EE_NOT_SUPPORTED.
	 * @return lust of all IDocuments matching the goven criteria
	 */
	public List<IOpaqueDocument> listDocuments(Patient pat, String categoryMatch,
		String titleMatch, String keywordMatch, TimeSpan dateMatch, String contentsMatch)
		throws ElexisException;
}
