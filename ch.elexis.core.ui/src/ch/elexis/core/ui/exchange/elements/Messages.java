/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.exchange.elements;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.exchange.elements.messages";//$NON-NLS-1$
	// Created by JInto - www.guh-software.de
	// Mon Nov 30 16:02:12 CET 2009
	public static String ContactElement_Name;
	public static String ContactElement_gebdat;
	public static String ContactElement_vorname;
	public static String MedicalElement_Documents;
	public static String MedicalElement_EMREntries;
	public static String MedicalElement_Findings;
	public static String MedicalElement_Medcaments;
	public static String RecordElement_CreatedBy;
	public static String RecordElement_EntryDate;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}