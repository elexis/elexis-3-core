/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package ch.elexis.data;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {
	
	private static final String BUNDLE_NAME = "ch.elexis.data.messages";//$NON-NLS-1$
	
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private Messages(){
		// Do not instantiate
	}
	
	public static String BezugsKontakt_ContactDoesntExist;
	public static String Contact_SalutationF;
	public static String Contact_SalutationM;
	public static String Fall_Accident;
	public static String Fall_Birthdefect;
	public static String Fall_CaseClosedCaption;
	public static String Fall_CaseClosedText;
	public static String Fall_CLOSED;
	public static String Fall_Disease;
	public static String Fall_IV_Name;
	public static String Fall_KVG_Name;
	public static String Fall_KVGRequirements;
	public static String Fall_Maternity;
	public static String Fall_MV_Name;
	public static String Fall_NoMandatorCaption;
	public static String Fall_NoMandatorText;
	public static String Fall_Open;
	public static String Fall_Other;
	public static String Fall_Prevention;
	public static String Fall_Private_Name;
	public static String Fall_TarmedLeistung;
	public static String Fall_TarmedPrinter;
	public static String Fall_Undefined;
	public static String Fall_UVG_Name;
	public static String Fall_UVGRequirements;
	public static String Fall_VVG_Name;
	public static String GlobalActions_CantCreateKons;
	public static String GlobalActions_DoSelectCase;
	public static String GlobalActions_DoSelectPatient;
	public static String GlobalActions_casclosed;
	public static String GlobalActions_caseclosedexplanation;
	public static String GlobalActions_SecondForToday;
	public static String GlobalActions_SecondForTodayQuestion;
	
	public static String LabItem_defaultGroup;
	public static String LabItem_longOwnLab;
	public static String LabItem_shortOwnLab;
	public static String LabMapping_reasonDefinitionNotValid;
	public static String LabMapping_reasonLineNotValid;
	public static String LabMapping_reasonMoreContacts;
	public static String LabMapping_reasonMoreLabItems;
	public static String LabOrder_stateDone;
	public static String LabOrder_stateImported;
	public static String LabOrder_stateOrdered;
	public static String LabOrder_contactOwnLabName;
	
	static {
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	public static String getString(String key){
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException missingResourceException) {
			return '!' + key + '!';
		}
	}
}