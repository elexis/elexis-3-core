/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.medication.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.medication.views.messages";//$NON-NLS-1$
	
	public static String DauerMediView_copy;
	public static String DauerMediView_copyToClipboard;
	public static String FixMediDisplay_AddItem;
	public static String FixMediDisplay_Change;
	public static String FixMediDisplay_Copy;
	public static String FixMediDisplay_DailyCost;
	public static String FixMediDisplay_Delete;
	public static String FixMediDisplay_DeleteUnrecoverable;
	public static String FixMediDisplay_FixMedikation;
	public static String FixMediDisplay_Modify;
	public static String FixMediDisplay_Prescription;
	public static String FixMediDisplay_Stop;
	public static String FixMediDisplay_StopThisMedicament;
	public static String FixMediDisplay_UsageList;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}