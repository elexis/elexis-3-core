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
	public static String FixMediDisplay_AddDefaultSignature;
	public static String FixMediDisplay_AddDefaultSignature_Tooltip;
	
	public static String TherapieplanComposite_btnIsFixmedication_text;
	public static String TherapieplanComposite_tblclmnArticle_text;
	public static String TherapieplanComposite_txtArticle_message;
	public static String TherapieplanComposite_btnPRNMedication_toolTipText;
	public static String TherapieplanComposite_tblclmnDosage_text;
	public static String TherapieplanComposite_tblclmnDosage_toolTipText;
	public static String TherapieplanComposite_tblclmnComment_text;
	public static String TherapieplanComposite_tblclmnSupplied_text;
	public static String TherapieplanComposite_tblclmnAmount_text;
	public static String MedicationComposite_txtComment_message;
	public static String MedicationComposite_txtIntakeOrder_message;
	public static String MedicationComposite_lblNewLabel_text;
	public static String MedicationComposite_lblNewLabel_text_1;
	public static String MedicationComposite_btnNewButton_text;
	public static String MedicationComposite_btnConfirm;
	public static String MedicationComposite_btnStop;
	public static String MedicationComposite_btnPRNMedication_text;
	public static String MedicationComposite_decorConfirm;
	public static String MedicationComposite_search;
	public static String MedicationComposite_stopped;
	public static String MedicationComposite_stopReason;
	public static String MedicationComposite_intolerance;
	public static String MedicationComposite_lastReceived;
	public static String MedicationComposite_lastReceivedAt;
	public static String MedicationComposite_startedAt;
	public static String MedicationComposite_stopDateAndReason;
	public static String MedicationComposite_recipeFrom;
	public static String MedicationComposite_consMissing;
	public static String MedicationComposite_consFrom;
	public static String MedicationComposite_freetext;
	public static String MedicationComposite_tooltipDosageType;
	
	public static String MovePrescriptionPositionInTableUpAction_Label;
	public static String MovePrescriptionPositionInTableDownAction_Label;
	public static String MedicationComposite_btnCheckButton_text;
	public static String MedicationComposite_btnShowHistory_toolTipText;
	public static String MedicationComposite_btnIsFixmedication_toolTipText;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}