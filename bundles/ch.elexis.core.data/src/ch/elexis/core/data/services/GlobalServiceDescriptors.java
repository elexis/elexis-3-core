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
package ch.elexis.core.data.services;

/**
 * Descriptors of some standardized services.
 * 
 * @see ch.elexis.util.Extensions#findBestService(String)
 * @author Gerry Weirich
 * 
 */
public class GlobalServiceDescriptors {
	/** Scan Documents */
	public static final String SCANNING = "ScannerService";
	/** Scan Documetns directly do pdf */
	public static final String SCAN_TO_PDF = "ScanToPDFService";
	/** Document manager */
	public static final String DOCUMENT_MANAGEMENT = "DocumentManagement";
	/** IRangeHandlers */
	public static final String TEXT_CONTENTS_EXTENSION = "TextContentsExtension";
}
