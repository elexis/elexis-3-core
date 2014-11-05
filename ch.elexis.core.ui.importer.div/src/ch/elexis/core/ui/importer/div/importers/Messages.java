/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.ui.importer.div.importers;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.importer.div.importers.messages"; //$NON-NLS-1$
	public static String ExcelWrapper_ErrorUnknownCellType;
	public static String GenericImporter_ErrorImporting;
	public static String GenericImporter_General;
	public static String GenericImporter_ImportGeneralText;
	public static String GenericImporterBlatt_ChangeIfNeeded;
	public static String GenericImporterBlatt_FieldsElexis;
	public static String GenericImporterBlatt_FieldsExcel;
	public static String GenericImporterBlatt_File;
	public static String GenericImporterBlatt_FileType;
	public static String GenericImporterBlatt_GeneralFileImport;
	public static String GenericImporterBlatt_ImportAllDataNew;
	public static String GenericImporterBlatt_ImportAllDataNewCaption;
	public static String GenericImporterBlatt_ImportAllValues;
	public static String GenericImporterBlatt_LowerFieldSelectedAutoamtcally;
	public static String GenericImporterBlatt_MatchData;
	public static String GenericImporterBlatt_PleaseSelect;
	public static String GenericImporterBlatt_PleaseSelectFile;
	public static String GenericImporterBlatt_PleaseSelectType;
	public static String GenericImporterBlatt_SelectFile;
	public static String GenericImporterBlatt_update;
	public static String GenericImporterBlatt_UpdateSelectedData;
	public static String HL7_CannotReadFile;
	public static String HL7_Database;
	public static String HL7_ExceptionWhileReading;
	public static String HL7_Hostologie;
	public static String HL7_Lab;
	public static String HL7_NameConflictWithID;
	public static String HL7_PatientNotInDatabase;
	public static String HL7_SelectPatient;
	public static String HL7_WhoIs;
	public static String HL7Parser_AskOverwrite;
	public static String HL7Parser_AutomaticAddedGroup;
	public static String HL7Parser_CommentCode;
	public static String HL7Parser_CommentGroup;
	public static String HL7Parser_CommentName;
	public static String HL7Parser_CouldNotMoveToArchive;
	public static String HL7Parser_ErrorArchiving;
	public static String HL7Parser_ErrorReading;
	public static String HL7Parser_LabAlreadyImported;
	public static String HL7Parser_LabNotFound;
	public static String HL7Parser_TheFile;
	public static String HL7Parser_NoLab;
	public static String HL7Parser_AskUseOwnLab;
	public static String KontaktImporter_AskSameAnd;
	public static String KontaktImporter_AskSameText1;
	public static String KontaktImporter_AskSameText2;
	public static String KontaktImporter_AskSameTitle;
	public static String KontaktImporter_ErrorImport;
	public static String KontaktImporter_ExplanationImport;
	public static String KontaktImporter_Title;
	public static String KontaktImporterBlatt_ChoseFile;
	public static String KontaktImporterBlatt_csvImportNotSupported;
	public static String KontaktImporterBlatt_DatatypeErrorExplanation;
	public static String KontaktImporterBlatt_DatatypeErrorHeading;
	public static String KontaktImporterBlatt_DatatypeErrorText;
	public static String KontaktImporterBlatt_Datei;
	public static String KontaktImporterBlatt_DateiTyp;
	public static String KontaktImporterBlatt_Importing;
	public static String KontaktImporterBlatt_KeepID;
	public static String KontaktImporterBlatt_KKKuerzel;
	public static String KontaktImporterBlatt_kklistHeading;
	public static String KontaktImporterBlatt_PleaseChooseTypeAndFile;
	public static String KontaktImporterBlatt_xmlImportNotSupported;
	public static String KontaktImporterDialog_ImporterCaption;
	public static String KontaktImporterDialog_ImportingContact;
	public static String KontaktImporterDialog_PleaseEnterFileTypeAndFile;
	public static String Presets_ImportingContacts;
	public static String Presets_Insurance;
	public static String Presets_InsuranceNumber;
	public static String Presets_KKKuerzel;
	public static String Presets_KVGAbkuerzung;
	public static String Presets_PreviousID;
	public static String Presets_Switzerland;
	public static String Presets_Unfall;
	public static String Presets_Unfallnummer;
	public static String Presets_UVGAbkuerzung;
	public static String LabImporterUtil_Select;
	public static String LabImporterUtil_SelectLab;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
