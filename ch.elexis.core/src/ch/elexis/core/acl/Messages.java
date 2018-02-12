/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.acl;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.acl.messages";//$NON-NLS-1$
	
	public static String AccessControl_GroupAll;
	public static String AccessControlDefaults_about;
	public static String AccessControlDefaults_absolute;
	public static String AccessControlDefaults_Access;
	public static String AccessControlDefaults_accountingGlobal;
	public static String AccessControlDefaults_actions;
	public static String AccessControlDefaults_Administration;
	public static String AccessControlDefaults_assign;
	public static String AccessControlDefaults_bills;
	public static String AccessControlDefaults_Bills;
	public static String AccessControlDefaults_CaseCopy;
	public static String AccessControlDefaults_CaseSpecialFields;
	public static String AccessControlDefaults_CaseReopen;
	public static String AccessControlDefaults_ChangeCaseSpecialFields;
	public static String AccessControlDefaults_change;
	public static String AccessControlDefaults_changeBilled;
	public static String AccessControlDefaults_changeMandator;
	public static String AccessControlDefaults_changeMedication;
	public static String AccessControlDefaults_changeStateManually;
	public static String AccessControlDefaults_changeSysTemplates;
	public static String AccessControlDefaults_changeTemplates;
	public static String AccessControlDefaults_chargeAll;
	public static String AccessControlDefaults_checkLabValues;
	public static String AccessControlDefaults_consultation;
	public static String AccessControlDefaults_contact;
	public static String AccessControlDefaults_create;
	public static String AccessControlDefaults_createBills;
	public static String AccessControlDefaults_data;
	public static String AccessControlDefaults_databaseConnection;
	public static String AccessControlDefaults_databaseUtilities;
	public static String AccessControlDefaults_dataImport;
	public static String AccessControlDefaults_documents;
	public static String AccessControlDefaults_documentsCreate;
	public static String AccessControlDefaults_documentsDelete;
	public static String AccessControlDefaults_documentsCreateCat;
	public static String AccessControlDefaults_documentsDeleteCat;
	public static String AccessControlDefaults_DefineCaseSpecialFields;
	public static String AccessControlDefaults_delete;
	public static String AccessControlDefaults_deleteCase;
	public static String AccessControlDefaults_display;
	public static String AccessControlDefaults_doAccount;
	public static String AccessControlDefaults_edit;
	public static String AccessControlDefaults_edit2;
	public static String AccessControlDefaults_execute;
	public static String AccessControlDefaults_export;
	public static String AccessControlDefaults_fixedMedication;
	public static String AccessControlDefaults_grantRights;
	public static String AccessControlDefaults_help;
	public static String AccessControlDefaults_labValues;
	public static String AccessControlDefaults_loadInfoStore;
	public static String AccessControlDefaults_logIn;
	public static String AccessControlDefaults_main_case;
	public static String AccessControlDefaults_main_consultation;
	public static String AccessControlDefaults_main_Patient;
	public static String AccessControlDefaults_mandator;
	public static String AccessControlDefaults_mergeLabItems;
	public static String AccessControlDefaults_modify;
	public static String AccessControlDefaults_modify2;
	public static String AccessControlDefaults_modify3;
	public static String AccessControlDefaults_modifyBills;
	public static String AccessControlDefaults_modifySticker;
	public static String AccessControlDefaults_newWindow;
	public static String AccessControlDefaults_read;
	public static String AccessControlDefaults_reminders;
	public static String AccessControlDefaults_script;
	public static String AccessControlDefaults_selectPerspective;
	public static String AccessControlDefaults_selectView;
	public static String AccessControlDefaults_services;
	public static String AccessControlDefaults_settings;
	public static String AccessControlDefaults_show;
	public static String AccessControlDefaults_terminate;
	public static String AccessControlDefaults_user;
	public static String AccessControlDefaults_viewAll;
	public static String ACE_implicit;
	public static String ACE_root;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}