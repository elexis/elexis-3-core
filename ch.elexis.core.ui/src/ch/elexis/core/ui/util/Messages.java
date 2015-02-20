/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.util.messages";//$NON-NLS-1$
	
	public static String DatabaseCleaner_NoCaseForBill;
	public static String DatabaseCleaner_NoCaseForKons;
	public static String DatabaseCleaner_NoMandatorForKons;
	public static String DatabaseCleaner_concerning;
	public static String DefaultContentProvider_noData;
	public static String DefaultControlFieldProvider_enterFilter;
	public static String ImporterPage_allFiles;
	public static String ImporterPage_browse;
	public static String ImporterPage_couldntConnect;
	public static String ImporterPage_dir;
	public static String ImporterPage_enter;
	public static String ImporterPage_file;
	public static String ImporterPage_importError;
	public static String ImporterPage_odbcSource;
	public static String ImporterPage_pleaseEnterODBC;
	public static String ImporterPage_selectDB;
	public static String ImporterPage_source;
	public static String ImporterPage_unknownType;
	public static String KGDrucker_couldntShow;
	public static String KGDrucker_couldntprint;
	public static String KGDrucker_emr;
	public static String KGDrucker_errorPrinting;
	public static String KGDrucker_printEMR;
	public static String LabeledInputField_7;
	public static String LabeledInputField_blue;
	public static String LazyContentProvider_noData;
	public static String MoneyInput_InvalidAmountCaption;
	public static String MoneyInput_InvalidAmountContents;
	public static String NoDataAvailable;
	public static String SWTHelper_BadParameter;
	public static String SWTHelper_HasNoValidContents;
	public static String SWTHelper_blue;
	public static String SWTHelper_cancel;
	public static String SWTHelper_no;
	public static String SWTHelper_yes;
	public static String TemplateDrucker_couldntOpen;
	public static String TemplateDrucker_couldntPrint;
	public static String TemplateDrucker_docname;
	public static String TemplateDrucker_errorPrinting;
	public static String TemplateDrucker_printing;
	public static String TreeContentProvider_loadData;
	public static String ViewerConfigurer_createNew;
	public static String WikipediaSearchAction_DisplayName;
	public static String LimitedText_MaxLengthReached;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}