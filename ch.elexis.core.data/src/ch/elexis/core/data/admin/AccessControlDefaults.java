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

package ch.elexis.core.data.admin;

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
	
	public static final ACE ADMIN = new ACE(ACE.ACE_ROOT,
		"Admin", Messages.getString("AccessControlDefaults.Administration")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACCOUNTING = new ACE(ACE.ACE_ROOT,
		"Rechnungen", Messages.getString("AccessControlDefaults.Bills")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACCOUNTING_CREATE = new ACE(ACCOUNTING,
		"erstellen", Messages.getString("AccessControlDefaults.create"));; //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACCOUNTING_MODIFY = new ACE(ACCOUNTING,
		"bearbeiten", Messages.getString("AccessControlDefaults.edit")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACCOUNTING_STATS = new ACE(ACCOUNTING, "statistiken", "statistics");
	public static final ACE ACE_ACCESS = new ACE(ADMIN,
		"Zugriff", Messages.getString("AccessControlDefaults.Access")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACCOUNTING_GLOBAL = new ACE(ACE.ACE_ROOT,
		"AccountingGlobal", Messages.getString("AccessControlDefaults.accountingGlobal")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACCOUNTING_READ = new ACE(ACCOUNTING_GLOBAL,
		"read", Messages.getString("AccessControlDefaults.read")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACCOUNTING_BILLCREATE = new ACE(ACCOUNTING_GLOBAL,
		"createBills", Messages.getString("AccessControlDefaults.createBills")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACCOUNTING_BILLMODIFY = new ACE(ACCOUNTING_GLOBAL,
		"modifyBills", Messages.getString("AccessControlDefaults.modifyBills")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ADMIN_ACE = new ACE(ADMIN, "ACE", "ACE modifizieren");
	
	public static final ACE ACL_USERS = new ACE(ACE_ACCESS,
		"Rechte erteilen", Messages.getString("AccessControlDefaults.grantRights")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE DELETE = new ACE(ACE.ACE_ROOT,
		"Löschen", Messages.getString("AccessControlDefaults.delete")); //$NON-NLS-1$ //$NON-NLS-2$
	public final static ACE DELETE_FORCED = new ACE(DELETE,
		"Absolut", Messages.getString("AccessControlDefaults.absolute")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE DELETE_BILLS = new ACE(DELETE,
		"Rechnungen", Messages.getString("AccessControlDefaults.bills")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE DELETE_MEDICATION = new ACE(DELETE,
		"Dauermedikation", Messages.getString("AccessControlDefaults.fixedMedication")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE DELETE_LABITEMS = new ACE(DELETE,
		"Laborwerte", Messages.getString("AccessControlDefaults.labValues")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE DATA = new ACE(ACE.ACE_ROOT,
		"Daten", Messages.getString("AccessControlDefaults.data")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONTAKT = new ACE(DATA,
		"Kontakt", Messages.getString("AccessControlDefaults.contact")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONTAKT_DISPLAY = new ACE(KONTAKT,
		"Anzeigen", Messages.getString("AccessControlDefaults.display")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONTAKT_EXPORT = new ACE(KONTAKT,
		"Exportieren", Messages.getString("AccessControlDefaults.export")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONTAKT_INSERT = new ACE(KONTAKT,
		"Erstellen", Messages.getString("AccessControlDefaults.create")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONTAKT_MODIFY = new ACE(KONTAKT,
		"Ändern", Messages.getString("AccessControlDefaults.edit2")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONTAKT_DELETE = new ACE(DELETE,
		"Kontakt", Messages.getString("AccessControlDefaults.contact")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONTAKT_ETIKETTE = new ACE(KONTAKT,
		"etikettieren", Messages.getString("AccessControlDefaults.modifySticker")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE PATIENT = new ACE(DATA,
		"Patient", Messages.getString("AccessControlDefaults.main_Patient")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE PATIENT_DISPLAY = new ACE(PATIENT,
		"Anzeigen", Messages.getString("AccessControlDefaults.show")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE PATIENT_INSERT = new ACE(PATIENT,
		"Erstellen", Messages.getString("AccessControlDefaults.create")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE PATIENT_MODIFY = new ACE(PATIENT,
		"Ändern", Messages.getString("AccessControlDefaults.modify")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE MEDICATION_MODIFY = new ACE(PATIENT,
		"Medikation ändern", Messages.getString("AccessControlDefaults.changeMedication")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE LAB_SEEN = new ACE(PATIENT,
		"Labor abhaken", Messages.getString("AccessControlDefaults.checkLabValues")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE MANDANT = new ACE(DATA,
		"Mandant", Messages.getString("AccessControlDefaults.mandator")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE MANDANT_CREATE = new ACE(MANDANT,
		"Erstellen", Messages.getString("AccessControlDefaults.create")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE USER = new ACE(DATA,
		"Anwender", Messages.getString("AccessControlDefaults.user")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE USER_CREATE = new ACE(USER,
		"Erstellen", Messages.getString("AccessControlDefaults.create")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE LEISTUNGEN = new ACE(ACE.ACE_ROOT,
		"Leistungen", Messages.getString("AccessControlDefaults.services")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE LSTG_VERRECHNEN = new ACE(LEISTUNGEN,
		"Verrechnen", Messages.getString("AccessControlDefaults.doAccount")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE LSTG_CHARGE_FOR_ALL = new ACE(LEISTUNGEN, "AlleVerrechnen",
		Messages.getString("AccessControlDefaults.chargeAll"));
	
	public static final ACE KONS = new ACE(ACE.ACE_ROOT,
		"Konsultation",Messages.getString("AccessControlDefaults.main_consultation")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONS_CREATE = new ACE(KONS,
		"Erstellen", Messages.getString("AccessControlDefaults.create")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONS_EDIT = new ACE(KONS,
		"Bearbeiten", Messages.getString("AccessControlDefaults.modify2")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONS_DELETE = new ACE(DELETE,
		"Konsultation", Messages.getString("AccessControlDefaults.consultation")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE KONS_REASSIGN = new ACE(KONS,
		"zuordnen", Messages.getString("AccessControlDefaults.assign")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE SCRIPT = new ACE(ACE.ACE_ROOT,
		"Script", Messages.getString("AccessControlDefaults.script")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE SCRIPT_EXECUTE = new ACE(SCRIPT,
		"ausführen", Messages.getString("AccessControlDefaults.execute")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE SCRIPT_EDIT = new ACE(SCRIPT,
		"bearbeiten", Messages.getString("AccessControlDefaults.modify3")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE CASE = new ACE(ACE.ACE_ROOT,
		"Fall", Messages.getString("AccessControlDefaults.main_case")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE CASE_MODIFY = new ACE(CASE,
		"Ändern", Messages.getString("AccessControlDefaults.change")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE CASE_SPECIALS = new ACE(CASE,
		"Specials", Messages.getString("AccessControlDefaults.CaseSpecialFields")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE CASE_DEFINE_SPECIALS = new ACE(CASE_SPECIALS,
		"Define_specials", Messages.getString("AccessControlDefaults.DefineCaseSpecialFields")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE CASE_MODIFY_SPECIALS = new ACE(CASE_SPECIALS,
		"Ändern_specials", Messages.getString("AccessControlDefaults.ChangeCaseSpecialFields")); //$NON-NLS-1$ //$NON-NLS-2$
	
	// allows to change the text of an already billed consultation
	// TODO: maybe we should just use KONS_EDIT
	public static final ACE ADMIN_KONS = new ACE(ADMIN,
		"Konsultation", Messages.getString("AccessControlDefaults.main_consultation")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ADMIN_REMINDERS = new ACE(ADMIN,
		"Reminders", Messages.getString("AccessControlDefaults.reminders")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ADMIN_BILLS = new ACE(ADMIN,
		"Rechnungen", Messages.getString("AccessControlDefaults.bills")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE ADMIN_KONS_EDIT_IF_BILLED = new ACE(ADMIN_KONS,
		"change_billed", Messages.getString("AccessControlDefaults.changeBilled")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ADMIN_VIEW_ALL_REMINDERS = new ACE(ADMIN_REMINDERS,
		"viewAll", Messages.getString("AccessControlDefaults.viewAll")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ADMIN_CHANGE_BILLSTATUS_MANUALLY = new ACE(ADMIN_BILLS,
		"changeManually", Messages.getString("AccessControlDefaults.changeStateManually")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE DOCUMENT = new ACE(ACE.ACE_ROOT,
		"Dokumente", Messages.getString("AccessControlDefaults.documents")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE DOCUMENT_CREATE = new ACE(DOCUMENT,
		"Erstellen", Messages.getString("AccessControlDefaults.create")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE DOCUMENT_TEMPLATE = new ACE(DOCUMENT,
		"Vorlagen ändern", Messages.getString("AccessControlDefaults.changeTemplates")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE DOCUMENT_SYSTEMPLATE = new ACE(DOCUMENT,
		"Systemvorlagen ändern", Messages.getString("AccessControlDefaults.changeSysTemplates")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE ACTIONS = new ACE(ACE.ACE_ROOT,
		"Aktionen", Messages.getString("AccessControlDefaults.actions")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_EXIT = new ACE(ACTIONS,
		"Beenden", Messages.getString("AccessControlDefaults.terminate")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_ABOUT = new ACE(ACTIONS,
		"Über", Messages.getString("AccessControlDefaults.about")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_HELP = new ACE(ACTIONS,
		"Hilfe", Messages.getString("AccessControlDefaults.help")); //$NON-NLS-1$ //$NON-NLS-2$
	
	public static final ACE AC_IMORT = new ACE(ACTIONS,
		"Fremddatenimport", Messages.getString("AccessControlDefaults.dataImport")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_PREFS = new ACE(ACTIONS,
		"Einstellungen", Messages.getString("AccessControlDefaults.settings")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_LOGIN = new ACE(ACTIONS,
		"Anmelden", Messages.getString("AccessControlDefaults.logIn")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_CONNECT = new ACE(ACTIONS,
		"Datenbankverbindung", Messages.getString("AccessControlDefaults.databaseConnection")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_PURGE = new ACE(ACTIONS,
		"Datenbankbereinigung", Messages.getString("AccessControlDefaults.databaseUtilities")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_CHANGEMANDANT = new ACE(ACTIONS,
		"Mandantwechsel", Messages.getString("AccessControlDefaults.changeMandator")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_NEWWINDOW = new ACE(ACTIONS,
		"NeuesFenster", Messages.getString("AccessControlDefaults.newWindow")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_SHOWPERSPECTIVE = new ACE(ACTIONS,
		"Perspektivenauswahl", Messages.getString("AccessControlDefaults.selectPerspective")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE AC_SHOWVIEW = new ACE(ACTIONS,
		"Viewauswahl", Messages.getString("AccessControlDefaults.selectView")); //$NON-NLS-1$ //$NON-NLS-2$
	public static final ACE ACE_DOCUMENTS = new ACE(ACE.ACE_ROOT, "Dokumente",
		Messages.getString("AccessControlDefaults.documents"));
	public static final ACE ACE_DOC_CREATE = new ACE(ACE_DOCUMENTS, "Erstellen",
		Messages.getString("AccessControlDefaults.documentsCreate"));
	public static final ACE ACE_DOC_DELETE = new ACE(ACE_DOCUMENTS, "Loeschen",
		Messages.getString("AccessControlDefaults.documentsDelete"));
	public static final ACE ACE_DOC_CATCREATE = new ACE(ACE_DOCUMENTS, "KatErstellen",
		Messages.getString("AccessControlDefaults.documentsCreateCat"));
	public static final ACE ACE_DOC_CATDELETE = new ACE(ACE_DOCUMENTS, "KatLoeschen",
		Messages.getString("AccessControlDefaults.documentsDeleteCat"));
	
	private static final ACE[] Alle = {
		AC_EXIT,
		AC_ABOUT,
		AC_HELP,
		AC_LOGIN,
		new ACE(ACE.ACE_ROOT,
			"LoadInfoStore", Messages.getString("AccessControlDefaults.loadInfoStore")) //$NON-NLS-1$ //$NON-NLS-2$
		};
	
	private static final ACE[] Anwender = {
		DATA, ACTIONS, DOCUMENT, KONS, LEISTUNGEN, ACCOUNTING
	};
	
	public static ACE[] getAlle(){
		return Alle;
	}
	
	public static ACE[] getAnwender(){
		return Anwender;
	}
	
}
