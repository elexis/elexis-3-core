/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.laboratory.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.laboratory.views.messages";//$NON-NLS-1$
	
	public static String LabNotSeenView_date;
	public static String LabNotSeenView_loading;
	public static String LabNotSeenView_markAll;
	public static String LabNotSeenView_markAllOfPatientToolTip;
	public static String LabNotSeenView_markAllToolTip;
	public static String LabNotSeenView_markAllofPatient;
	public static String LabNotSeenView_normRange;
	public static String LabNotSeenView_parameter;
	public static String LabNotSeenView_patient;
	public static String LabNotSeenView_reallyMarkAll;
	public static String LabNotSeenView_reallyMarkCaption;
	public static String LabNotSeenView_value;
	public static String LaborView_Documents;
	public static String LaborView_ErrorCaption;
	public static String LaborView_NoPatientSelected;
	public static String LaborView_Open;
	public static String LaborView_Refresh;
	public static String LaborView_couldntwrite;
	public static String LaborView_import;
	public static String LaborView_importToolTip;
	public static String LaborView_labImporterCaption;
	public static String LaborView_labImporterText;
	public static String LaborView_newDate;
	public static String LaborView_nextPage;
	public static String LaborView_parameter;
	public static String LaborView_pathologic;
	public static String LaborView_prevPage;
	public static String LaborView_print;
	public static String LaborView_printOrders;
	public static String LaborView_reference;
	public static String LaborView_selectDataSource;
	public static String LaborView_textResultTitle;
	public static String LaborView_xmlExport;
	public static String LaborblattView_LabTemplateName;
	public static String LaborblattView_created;
	
	public static String LabOrderView_Order;
	public static String LabOrderView_RefValue;
	public static String LabOrderView_DateTime;
	public static String LabOrderView_Value;
	public static String LabOrderView_OrderNumber;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}