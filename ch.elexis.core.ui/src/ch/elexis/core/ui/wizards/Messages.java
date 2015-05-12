/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package ch.elexis.core.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.wizards.messages";//$NON-NLS-1$
	// Created by JInto - www.guh-software.de
	// Mon Jun 21 15:42:04 CEST 2010
	public static String DBConnectFirstPage_Connection;
	public static String DBConnectFirstPage_connectioNDetails;
	public static String DBConnectFirstPage_databaseName;
	public static String DBConnectFirstPage_enterType;
	public static String DBConnectFirstPage_selectType;
	public static String DBConnectFirstPage_serevrAddress;
	public static String DBConnectFirstPage_theDescripotion;
	public static String DBConnectFirstPage_theDescription;
	public static String DBConnectFirstPage_typeOfDB;
	public static String DBConnectSecondPage_0;
	public static String DBConnectSecondPage_Credentials;
	public static String DBConnectSecondPage_databasePassword;
	public static String DBConnectSecondPage_databaseUsername;
	public static String DBConnectSecondPage_username1;
	public static String DBConnectSecondPage_username2;
	public static String DBConnectWizard_Credentials;
	public static String DBConnectWizard_connectDB;
	public static String DBConnectWizard_couldntConnect;
	public static String DBConnectWizard_typeOfDB;
	public static String DBConnectWizard_newConnection;
	public static String DBConnectWizardPage_enterSettings;
	public static String DBImportFirstPage_Connection;
	public static String DBImportFirstPage_EnterType;
	public static String DBImportFirstPage_connection;
	public static String DBImportFirstPage_databaseName;
	public static String DBImportFirstPage_enterNameODBC;
	public static String DBImportFirstPage_selectType;
	public static String DBImportFirstPage_serverAddress;
	public static String DBImportFirstPage_theDesrciption;
	public static String DBImportFirstPage_typeOfDB;
	public static String DBImportSecondPage_enterPassword;
	public static String DBImportSecondPage_enterUsername;
	public static String DBImportSecondPage_userDetails;
	public static String DBImportWizard_connectDB;
	public static String DBImportWizard_typeOfDB;
	public static String DBConnectWizardPage_lblGespeicherteVerbindungen_text;
	public static String DBConnectWizardPage_btnNewButton_text;
	public static String DBConnectWizardPage_lblUsername_text;
	public static String DBConnectWizardPage_lblPassword_text;
	public static String DBConnectWizardPage_btnCheckButton_text;
	public static String DBConnectWizardPage_btnDatenbankErstellen_text;
	public static String DBConnectWizardPage_grpAdministratorRoot_text;
	public static String DBConnectWizardPage_lblUsername_1_text;
	public static String DBConnectWizardPage_lblPasswort_text;
	public static String DBConnectWizardPage_grpCurrentConnection_text;
	public static String DBConnectWizardPage_grpStatCurrentConnection_text;
	public static String DBConnectSelectionConnectionWizardPage_lblOderAufDer_text;
	public static String DBConnectSelectionConnectionWizardPage_this_message;
	public static String DBConnectNewOrEditConnectionWizardPage_this_message;
	public static String DBConnectNewOrEditConnectionWizardPage_this_title;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}