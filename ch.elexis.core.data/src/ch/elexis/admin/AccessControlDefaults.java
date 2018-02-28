/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.admin;

import java.util.Arrays;

import ch.elexis.core.model.RoleConstants;
import ch.elexis.data.Role;

/**
 * Hier werden Grundeinstellungen für Zugriffsrechte definiert. Diese werden nur beim allerersten
 * Programmstart (Beim Einrichten der Datenbank), und beim Auswählen des Buttons "Defaults" im
 * Zugriffs-Konfigurationsdialog eingelesen. Rechte, die mit ACTION beginnen, beziehen sich auf
 * Menu- Toolbar- und Shortcut- Actionen. Präfix READ_ ist ein Recht, eine bestimmte Property eines
 * Kontakts (aus den ExtInfo) zu lesen, WRITE_ ist das Recht, eine solche Property zu schreiben.
 * Andere Bezeichnungen sind unterschiedliche Rechte und sollten möglichst deskriptiv sein (Man muss
 * nicht lange überlegen, welches Recht wohl mit LEISTUNGEN_VERRECHNEN verliehen wird). Es werden
 * bei der Einrichtung 3 Gruppen angelegt: Alle, Anwender und Admin. Weitere Gruppen können
 * nachträglich beliebig erstellt werden.
 * 
 * @author gerry
 * 		
 */
public class AccessControlDefaults {
	
	public static final ACE ADMIN =
		new ACE(ACE.ACE_ROOT, "Admin", Messages.AccessControlDefaults_Administration); //$NON-NLS-1$
	public static final ACE ACCOUNTING =
		new ACE(ACE.ACE_ROOT, "Rechnungen", Messages.AccessControlDefaults_Bills); //$NON-NLS-1$ 
	public static final ACE ACCOUNTING_CREATE =
		new ACE(ACCOUNTING, "erstellen", Messages.AccessControlDefaults_create); //$NON-NLS-1$
	public static final ACE ACCOUNTING_MODIFY =
		new ACE(ACCOUNTING, "bearbeiten", Messages.AccessControlDefaults_edit); //$NON-NLS-1$
	public static final ACE ACCOUNTING_STATS = new ACE(ACCOUNTING, "statistiken", "statistics");
	
	public static final ACE ACCOUNTING_GLOBAL =
		new ACE(ACE.ACE_ROOT, "AccountingGlobal", Messages.AccessControlDefaults_accountingGlobal); //$NON-NLS-1$
	public static final ACE ACCOUNTING_READ =
		new ACE(ACCOUNTING_GLOBAL, "read", Messages.AccessControlDefaults_read); //$NON-NLS-1$
	public static final ACE ACCOUNTING_BILLCREATE =
		new ACE(ACCOUNTING_GLOBAL, "createBills", Messages.AccessControlDefaults_createBills); //$NON-NLS-1$
	public static final ACE ACCOUNTING_BILLMODIFY =
		new ACE(ACCOUNTING_GLOBAL, "modifyBills", Messages.AccessControlDefaults_modifyBills); //$NON-NLS-1$
		
	public final static ACE ADMIN_LABORATORY = new ACE(ADMIN, "laboratory", Messages.AccessControlDefaults_GroupAdminLaboratory);
	public final static ACE LABPARAM_EDIT = new ACE(ADMIN_LABORATORY, "edit_laboratory_parameter", Messages.AccessControlDefaults_EditLaboratoryParameter);
	public final static ACE LABITEM_MERGE = new ACE(ADMIN_LABORATORY, "unite_laboratory_parameter", //$NON-NLS-1$
		Messages.AccessControlDefaults_mergeLabItems);
		
	public static final ACE ACE_ACCESS =
		new ACE(ADMIN, "Zugriff", Messages.AccessControlDefaults_Access); //$NON-NLS-1$
	public static final ACE ACL_USERS =
		new ACE(ACE_ACCESS, "Rechte erteilen", Messages.AccessControlDefaults_grantRights); //$NON-NLS-1$
	public static final ACE ADMIN_ACE = new ACE(ACE_ACCESS, "ACE", "ACE modifizieren");
	
	public static final ACE DELETE =
		new ACE(ACE.ACE_ROOT, "Löschen", Messages.AccessControlDefaults_delete); //$NON-NLS-1$
	public final static ACE DELETE_FORCED =
		new ACE(DELETE, "Absolut", Messages.AccessControlDefaults_absolute); //$NON-NLS-1$
	public static final ACE DELETE_BILLS =
		new ACE(DELETE, "Rechnungen", Messages.AccessControlDefaults_bills); //$NON-NLS-1$
	public static final ACE DELETE_MEDICATION =
		new ACE(DELETE, "Dauermedikation", Messages.AccessControlDefaults_fixedMedication); //$NON-NLS-1$
	public static final ACE DELETE_LABITEMS =
		new ACE(DELETE, "Laborwerte", Messages.AccessControlDefaults_labValues); //$NON-NLS-1$
	public static final ACE DELETE_CASE = 
		new ACE(DELETE, "Delete_Case", Messages.AccessControlDefaults_deleteCase); //$NON-NLS-1$
	public static final ACE KONS_DELETE =
			new ACE(DELETE, "Konsultation", Messages.AccessControlDefaults_consultation); //$NON-NLS-1$
		
	public static final ACE DATA =
		new ACE(ACE.ACE_ROOT, "Daten", Messages.AccessControlDefaults_data); //$NON-NLS-1$
	public static final ACE KONTAKT =
		new ACE(DATA, "Kontakt", Messages.AccessControlDefaults_contact); //$NON-NLS-1$
	public static final ACE KONTAKT_DISPLAY =
		new ACE(KONTAKT, "Anzeigen", Messages.AccessControlDefaults_display); //$NON-NLS-1$
	public static final ACE KONTAKT_EXPORT =
		new ACE(KONTAKT, "Exportieren", Messages.AccessControlDefaults_export); //$NON-NLS-1$
	public static final ACE KONTAKT_INSERT =
		new ACE(KONTAKT, "Erstellen", Messages.AccessControlDefaults_create); //$NON-NLS-1$
	public static final ACE KONTAKT_MODIFY =
		new ACE(KONTAKT, "Ändern", Messages.AccessControlDefaults_edit2); //$NON-NLS-1$
	public static final ACE KONTAKT_DELETE =
		new ACE(DELETE, "Kontakt", Messages.AccessControlDefaults_contact); //$NON-NLS-1$
	public static final ACE KONTAKT_ETIKETTE =
		new ACE(KONTAKT, "etikettieren", Messages.AccessControlDefaults_modifySticker); //$NON-NLS-1$
		
	public static final ACE PATIENT =
		new ACE(DATA, "Patient", Messages.AccessControlDefaults_main_Patient); //$NON-NLS-1$
	public static final ACE PATIENT_DISPLAY =
		new ACE(PATIENT, "Anzeigen", Messages.AccessControlDefaults_show); //$NON-NLS-1$
	public static final ACE PATIENT_INSERT =
		new ACE(PATIENT, "Erstellen", Messages.AccessControlDefaults_create); //$NON-NLS-1$
	public static final ACE PATIENT_MODIFY =
		new ACE(PATIENT, "Ändern", Messages.AccessControlDefaults_modify); //$NON-NLS-1$
	public static final ACE MEDICATION_MODIFY =
		new ACE(PATIENT, "Medikation ändern", Messages.AccessControlDefaults_changeMedication); //$NON-NLS-1$
	public static final ACE LAB_SEEN =
		new ACE(PATIENT, "Labor abhaken", Messages.AccessControlDefaults_checkLabValues); //$NON-NLS-1$
		
	public static final ACE MANDANT =
		new ACE(DATA, "Mandant", Messages.AccessControlDefaults_mandator); //$NON-NLS-1$
	public static final ACE MANDANT_CREATE =
		new ACE(MANDANT, "Erstellen", Messages.AccessControlDefaults_create); //$NON-NLS-1$
		
	public static final ACE USER = new ACE(DATA, "Anwender", Messages.AccessControlDefaults_user); //$NON-NLS-1$
	public static final ACE USER_CREATE =
		new ACE(USER, "Erstellen", Messages.AccessControlDefaults_create); //$NON-NLS-1$
	public static final ACE USER_DELETE =
		new ACE(USER, "Löschen", Messages.AccessControlDefaults_delete);
		
	public static final ACE LEISTUNGEN =
		new ACE(ACE.ACE_ROOT, "Leistungen", Messages.AccessControlDefaults_services); //$NON-NLS-1$
	public static final ACE LSTG_VERRECHNEN =
		new ACE(LEISTUNGEN, "Verrechnen", Messages.AccessControlDefaults_doAccount); //$NON-NLS-1$
	public static final ACE LSTG_CHARGE_FOR_ALL =
		new ACE(LEISTUNGEN, "AlleVerrechnen", Messages.AccessControlDefaults_chargeAll);
		
	public static final ACE KONS =
		new ACE(ACE.ACE_ROOT, "Konsultation", Messages.AccessControlDefaults_main_consultation); //$NON-NLS-1$
	public static final ACE KONS_CREATE =
		new ACE(KONS, "Erstellen", Messages.AccessControlDefaults_create); //$NON-NLS-1$
	public static final ACE KONS_EDIT =
		new ACE(KONS, "Bearbeiten", Messages.AccessControlDefaults_modify2); //$NON-NLS-1$
	public static final ACE KONS_REASSIGN =
		new ACE(KONS, "zuordnen", Messages.AccessControlDefaults_assign); //$NON-NLS-1$
		
	public static final ACE SCRIPT =
		new ACE(ACE.ACE_ROOT, "Script", Messages.AccessControlDefaults_script); //$NON-NLS-1$
	public static final ACE SCRIPT_EXECUTE =
		new ACE(SCRIPT, "ausführen", Messages.AccessControlDefaults_execute); //$NON-NLS-1$
	public static final ACE SCRIPT_EDIT =
		new ACE(SCRIPT, "bearbeiten", Messages.AccessControlDefaults_modify3); //$NON-NLS-1$
		
	public static final ACE CASE =
		new ACE(ACE.ACE_ROOT, "Fall", Messages.AccessControlDefaults_main_case); //$NON-NLS-1$
	public static final ACE CASE_MODIFY =
		new ACE(CASE, "Ändern", Messages.AccessControlDefaults_change); //$NON-NLS-1$
	public static final ACE CASE_REOPEN =
		new ACE(CASE, "Reopen", Messages.AccessControlDefaults_CaseReopen); //$NON-NLS-1$
	public static final ACE CASE_COPY =
		new ACE(CASE, "copy", Messages.AccessControlDefaults_CaseCopy); //$NON-NLS-1$
	public static final ACE CASE_SPECIALS =
		new ACE(CASE, "Specials", Messages.AccessControlDefaults_CaseSpecialFields); //$NON-NLS-1$
	public static final ACE CASE_DEFINE_SPECIALS = new ACE(CASE_SPECIALS, "Define_specials", //$NON-NLS-1$
		Messages.AccessControlDefaults_DefineCaseSpecialFields);
	public static final ACE CASE_MODIFY_SPECIALS = new ACE(CASE_SPECIALS, "Ändern_specials", //$NON-NLS-1$
		Messages.AccessControlDefaults_ChangeCaseSpecialFields);
		
	// allows to change the text of an already billed consultation
	// TODO: maybe we should just use KONS_EDIT
	public static final ACE ADMIN_KONS =
		new ACE(ADMIN, "Konsultation", Messages.AccessControlDefaults_main_consultation); //$NON-NLS-1$
	public static final ACE ADMIN_REMINDERS =
		new ACE(ADMIN, "Reminders", Messages.AccessControlDefaults_reminders); //$NON-NLS-1$
	public static final ACE ADMIN_BILLS =
		new ACE(ADMIN, "Rechnungen", Messages.AccessControlDefaults_bills); //$NON-NLS-1$
		
	public static final ACE ADMIN_KONS_EDIT_IF_BILLED =
		new ACE(ADMIN_KONS, "change_billed", Messages.AccessControlDefaults_changeBilled); //$NON-NLS-1$
	public static final ACE ADMIN_VIEW_ALL_REMINDERS =
		new ACE(ADMIN_REMINDERS, "viewAll", Messages.AccessControlDefaults_viewAll); //$NON-NLS-1$
	public static final ACE ADMIN_CHANGE_BILLSTATUS_MANUALLY =
		new ACE(ADMIN_BILLS, "changeManually", Messages.AccessControlDefaults_changeStateManually); //$NON-NLS-1$
		
	public static final ACE DOCUMENT =
		new ACE(ACE.ACE_ROOT, "Dokumente", Messages.AccessControlDefaults_documents); //$NON-NLS-1$
	public static final ACE DOCUMENT_CREATE =
		new ACE(DOCUMENT, "create", Messages.AccessControlDefaults_create); //$NON-NLS-1$
	public static final ACE DOCUMENT_DELETE =
		new ACE(DOCUMENT, "delete", Messages.AccessControlDefaults_documentsDelete);
	public static final ACE DOCUMENT_CATCREATE = new ACE(DOCUMENT, "createCategory", //$NON-NLS-1$
		Messages.AccessControlDefaults_documentsCreateCat);
	public static final ACE DOCUMENT_CATDELETE = new ACE(DOCUMENT, "deleteCategory", //$NON-NLS-1$
		Messages.AccessControlDefaults_documentsDeleteCat);
		
	public static final ACE DOCUMENT_TEMPLATE =
		new ACE(DOCUMENT, "Vorlagen ändern", Messages.AccessControlDefaults_changeTemplates); //$NON-NLS-1$
	public static final ACE DOCUMENT_SYSTEMPLATE = new ACE(DOCUMENT, "Systemvorlagen ändern", //$NON-NLS-1$
		Messages.AccessControlDefaults_changeSysTemplates);
		
	public static final ACE ACTIONS =
		new ACE(ACE.ACE_ROOT, "Aktionen", Messages.AccessControlDefaults_actions); //$NON-NLS-1$
	public static final ACE AC_EXIT =
		new ACE(ACTIONS, "Beenden", Messages.AccessControlDefaults_terminate); //$NON-NLS-1$
	public static final ACE AC_ABOUT =
		new ACE(ACTIONS, "Über", Messages.AccessControlDefaults_about); //$NON-NLS-1$
	public static final ACE AC_HELP =
		new ACE(ACTIONS, "Hilfe", Messages.AccessControlDefaults_help); //$NON-NLS-1$
		
	public static final ACE AC_IMORT =
		new ACE(ACTIONS, "Fremddatenimport", Messages.AccessControlDefaults_dataImport); //$NON-NLS-1$
	public static final ACE AC_PREFS =
		new ACE(ACTIONS, "Einstellungen", Messages.AccessControlDefaults_settings); //$NON-NLS-1$
	public static final ACE AC_LOGIN =
		new ACE(ACTIONS, "Anmelden", Messages.AccessControlDefaults_logIn); //$NON-NLS-1$
	public static final ACE AC_CONNECT =
		new ACE(ACTIONS, "Datenbankverbindung", Messages.AccessControlDefaults_databaseConnection); //$NON-NLS-1$
	public static final ACE AC_PURGE =
		new ACE(ACTIONS, "Datenbankbereinigung", Messages.AccessControlDefaults_databaseUtilities); //$NON-NLS-1$
	public static final ACE AC_CHANGEMANDANT =
		new ACE(ACTIONS, "Mandantwechsel", Messages.AccessControlDefaults_changeMandator); //$NON-NLS-1$
	public static final ACE AC_NEWWINDOW =
		new ACE(ACTIONS, "NeuesFenster", Messages.AccessControlDefaults_newWindow); //$NON-NLS-1$
	public static final ACE AC_SHOWPERSPECTIVE =
		new ACE(ACTIONS, "Perspektivenauswahl", Messages.AccessControlDefaults_selectPerspective); //$NON-NLS-1$
	public static final ACE AC_SHOWVIEW =
		new ACE(ACTIONS, "Viewauswahl", Messages.AccessControlDefaults_selectView); //$NON-NLS-1$
		
	public static ACE[] getAlle(){
		return new ACE[] {
			AC_EXIT, AC_ABOUT, AC_HELP, AC_LOGIN,
			new ACE(ACE.ACE_ROOT, "LoadInfoStore", Messages.AccessControlDefaults_loadInfoStore) //$NON-NLS-1$
		};
	}
	
	public static ACE[] getAnwender(){
		return new ACE[] {
			ACCOUNTING_READ, ADMIN_REMINDERS, ACCOUNTING_BILLCREATE, ACTIONS, ACCOUNTING,
			CASE_DEFINE_SPECIALS, CASE_COPY, CASE_MODIFY, DELETE_MEDICATION, DELETE_LABITEMS, DOCUMENT_CREATE,
			DOCUMENT_CATCREATE, DOCUMENT_DELETE, DOCUMENT_SYSTEMPLATE, DOCUMENT_TEMPLATE, KONTAKT,
			KONS, KONS_DELETE, LEISTUNGEN, LABITEM_MERGE, PATIENT, SCRIPT
		};
	}
	
	public static void initializeDefaults(AbstractAccessControl ac){
		Role ur = Role.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
		ACE[] anwender = AccessControlDefaults.getAnwender();
		Arrays.asList(anwender).stream().forEachOrdered(ace -> ac.grant(ur, ace));
		ACE[] alle = AccessControlDefaults.getAlle();
		Arrays.asList(alle).stream().forEachOrdered(ace -> ac.grant(ur, ace));
		
		ac.grant(RoleConstants.SYSTEMROLE_LITERAL_ASSISTANT, LSTG_CHARGE_FOR_ALL);
		ac.grant(RoleConstants.SYSTEMROLE_LITERAL_ASSISTANT, LSTG_VERRECHNEN);
		
		ac.grant(RoleConstants.SYSTEMROLE_LITERAL_DOCTOR, USER);
		ac.grant(RoleConstants.SYSTEMROLE_LITERAL_DOCTOR, MANDANT);
		ac.grant(RoleConstants.SYSTEMROLE_LITERAL_DOCTOR, ADMIN_KONS_EDIT_IF_BILLED);
		
		ac.grant(RoleConstants.SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR, ACE_ACCESS);
	}
	
}
