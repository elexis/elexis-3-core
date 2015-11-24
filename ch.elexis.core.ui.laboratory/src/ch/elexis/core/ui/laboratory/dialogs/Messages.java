/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.laboratory.dialogs;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.laboratory.dialogs.messages";//$NON-NLS-1$
	
	public static String ImportLabMapping_errorNotFound;
	public static String ImportLabMapping_errorProblems;
	public static String ImportLabMapping_message;
	public static String ImportLabMapping_selectFile;
	public static String ImportLabMapping_shellTitle;
	public static String ImportLabMapping_title;
	public static String ImportLabMapping_titleProblemDialog;
	
	public static String EditLabResultDialog_errorNoResult;
	public static String EditLabResultDialog_errorRefFemaleNotNumber;
	public static String EditLabResultDialog_errorRefMaleNotNumber;
	public static String EditLabResultDialog_errorResultNotNumber;
	public static String EditLabItemDialog_errorRefFemaleNotAbsolute;
	public static String EditLabItemDialog_errorRefMaleNotAbsolute;
	public static String EditLabItemDialog_errorResultNotAbsolute;
	public static String EditLabResultDialog_labelAnalyse;
	public static String EditLabResultDialog_labelLab;
	public static String EditLabResultDialog_labelObservation;
	public static String EditLabResultDialog_labelRefFemale;
	public static String EditLabResultDialog_labelRefMale;
	public static String EditLabResultDialog_labelTime;
	public static String EditLabResultDialog_labelTransmission;
	public static String EditLabResultDialog_labelUnit;
	public static String EditLabResultDialog_labelValue;
	public static String EditLabResultDialog_message;
	public static String EditLabResultDialog_shellTitle;
	public static String EditLabResultDialog_title;
	
	public static String LaborVerordnungDialog_alreadyOrdered;
	public static String LaborVerordnungDialog_errorOrderNumber;
	public static String LaborVerordnungDialog_labelOrderNumber;
	public static String LaborVerordnungDialog_labelResponsible;
	public static String LaborVerordnungDialog_message;
	public static String LaborVerordnungDialog_shellTitle;
	public static String LaborVerordnungDialog_title;
	public static String LaborVerordnungDialog_printOrders;
	
	public static String MergeLabItemDialog_errorNoFromLabItemSelected;
	public static String MergeLabItemDialog_errorNoToLabItemSelected;
	public static String MergeLabItemDialog_errorSameSelected;
	public static String MergeLabItemDialog_labelMergeFrom;
	public static String MergeLabItemDialog_labelMergeTo;
	public static String MergeLabItemDialog_messageWarningDialog;
	public static String MergeLabItemDialog_titleWarningDialog;
	public static String MergeLabItemDialog_toolTipResultsCount;
	public static String MergeLabItemDialog_pleaseMergeParam;
	public static String MergeLabItemDialog_title;
	
	public static String EditLabItem_shellTitle;
	public static String EditLabItem_labelExportTag;
	public static String EditLabItem_message;
	public static String EditLabItem_errorNoTitle;
	public static String EditLabItem_labelDecimalPlace;
	public static String EditLabItem_labelVisible;
	public static String EditLabItem_labelShortLabel;
	public static String EditLabItem_labelTitle;
	public static String EditLabItem_labelType;
	public static String EditLabItem_labelTypNumber;
	public static String EditLabItem_labelTypText;
	public static String EditLabItem_labelTypAbsolute;
	public static String EditLabItem_labelTypFormula;
	public static String EditLabItem_titleScriptEditor;
	public static String EditLabItem_labelRefMale;
	public static String EditLabItem_labelRefFemale;
	public static String EditLabItem_labelUnit;
	public static String EditLabItem_labelGroup;
	public static String EditLabItem_tooltipGroup;
	public static String EditLabItem_labelGroupSequence;
	public static String EditLabItem_labelGroupPosition;
	public static String EditLabItem_labelTypDocument;
	public static String EditLabItem_title;
	public static String EditLabItem_labelHintExportTag;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}