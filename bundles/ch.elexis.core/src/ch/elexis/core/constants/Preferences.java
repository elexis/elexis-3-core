/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.constants;

import ch.elexis.core.preferences.Messages;

/**
 * Konstanten für die Namen der verschiedenen Einstellungen
 */
public class Preferences {
	// Preference store settings default prefix
	public static final String SETTINGS_PREFERENCE_STORE_DEFAULT = "_default"; //$NON-NLS-1$
	
	// Datenbank
	public static final String DB_CLASS = "verbindung/Connector"; //$NON-NLS-1$
	public static final String DB_CONNECT = "verbindung/Connectstring"; //$NON-NLS-1$
	public static final String DB_USERNAME = "verbindung/Username"; //$NON-NLS-1$
	public static final String DB_PWD = "verbindung/Passwort"; //$NON-NLS-1$
	public static final String DB_TYP = "verbindung/Datenbanktyp"; //$NON-NLS-1$
	public static final String DB_NAME = "verbindung/Datenbankname"; //$NON-NLS-1$
	public final static String DB_WIZARD = "verbindung/ass"; //$NON-NLS-1$
	/**
	 * contains folded Hashtable with db connection data
	 */
	public static final String CFG_FOLDED_CONNECTION = "verbindung/folded_string";
	public static final String CFG_FOLDED_CONNECTION_DRIVER = "driver";
	public static final String CFG_FOLDED_CONNECTION_USER = "user";
	public static final String CFG_FOLDED_CONNECTION_PASS = "pwd";
	public static final String CFG_FOLDED_CONNECTION_TYPE = "typ";
	public static final String CFG_FOLDED_CONNECTION_CONNECTSTRING = "connectionstring";
	public static final String CFG_STORED_JDBC_CONN = "verbindung/storedJDBCConnections/connection";
	
	// Locale
	/** 5292 assert network wide locale equality **/
	public static final String CFG_LOCALE = "locale"; //$NON-NLS-1$
	
	// Station
	public static final String STATION_IDENT_ID = "station/identId"; //$NON-NLS-1$
	public static final String STATION_IDENT_TEXT = "station/identText"; //$NON-NLS-1$
	
	/**
	 * The contact representing the customer organization itself
	 * @since 3.6
	 */
	public static final String SELFCONTACT_ID = "mainContactId"; //$NON-NLS-1$
	
	/**
	 * Time stamp of the first time an Elexis was connected to the database. Used as identifier of
	 * the installation.
	 * 
	 * @since 3.3
	 */
	public static final String INSTALLATION_TIMESTAMP = "installation/timestamp";
	
	/**
	 * OID of the running software. Can be uninitialized.
	 * 
	 * @since 3.3
	 */
	public static final String SOFTWARE_OID = "software/oid";
	/**
	 * OID subdomain that will be used in Elexis for patient master data.
	 */
	public static final String OID_SUBDOMAIN_PATIENTMASTERDATA = "100";
	
	// Ablauf
	public static final String ABL_LANGUAGE = "ablauf/sprache"; //$NON-NLS-1$
	public static final String ABL_LOGFILE = "ablauf/Log-Datei"; //$NON-NLS-1$
	public static final String ABL_LOGLEVEL = "ablauf/LogLevel"; //$NON-NLS-1$
	public static final String ABL_LOGALERT = "ablauf/LogAlertLevel"; //$NON-NLS-1$
	public static final String ABL_TRACE = "ablauf/Trace"; //$NON-NLS-1$
	public static final String ABL_BASEPATH = "ablauf/basepath"; //$NON-NLS-1$
	public static final String ABL_CACHELIFETIME = "ablauf/cachelifetime"; //$NON-NLS-1$
	public static final String ABL_UPDATESITE = "ablauf/updatesite"; //$NON-NLS-1$
	public static final String ABL_HEARTRATE = "ablauf/heartrate"; //$NON-NLS-1$
	
	// Sample
	public static final String P_PATH = "sample/pathPreference"; //$NON-NLS-1$
	public static final String P_BOOLEAN = "sample/booleanPreference"; //$NON-NLS-1$
	public static final String P_CHOICE = "sample/choicePreference"; //$NON-NLS-1$
	public static final String P_STRING = "sample/stringPreference"; //$NON-NLS-1$
	
	// Texterstellung
	public static final String P_TEXTMODUL = "briefe/Textmodul"; //$NON-NLS-1$
	public static final String P_TEXT_SUPPORT_LEGACY = "briefe/Textmodul_Support_Legacy"; //$NON-NLS-1$
	public static final String P_TEXT_RENAME_WITH_F2 = "briefe/rename_with_f2"; //$NON-NLS-1$
	public final static String P_OOBASEDIR = "briefe/OOBasis"; //$NON-NLS-1$
	public static final String P_TEXT_EDIT_LOCAL = "briefe/Textmodul_Edit_Local"; //$NON-NLS-1$
	public static final String P_TEXT_EXTERN_FILE = "briefe/Textmodul_Extern_File"; //$NON-NLS-1$
	public static final String P_TEXT_EXTERN_FILE_PATH = "briefe/Textmodul_Extern_File_Path"; //$NON-NLS-1$
	
	// Gruppen und Rechte
	public static final String ACC_GROUPS = "groupNames"; //$NON-NLS-1$
	
	// Zugriffsrechte -> Diese gehören sowieso nach AccessControlDefaults
	@Deprecated
	public static final String ACC_EXIT = "exitAction"; //$NON-NLS-1$
	@Deprecated
	public static final String ACC_LOGIN = "loginAction"; //$NON-NLS-1$
	@Deprecated
	public static final String ACC_PREFS = "prefsAction"; //$NON-NLS-1$
	@Deprecated
	public static final String ACC_SHOWVIEW = "showViewAction"; //$NON-NLS-1$
	
	// Briefe
	public static final String DOC_CATEGORY = "dokumente/kategorien"; //$NON-NLS-1$
	
	// Sidebar/Perspektivenauswahl
	public final static String SIDEBAR = "sidebar/pages"; //$NON-NLS-1$
	public static final String SHOWSIDEBAR = "sidebar/show"; //$NON-NLS-1$
	public static final String SHOWPERSPECTIVESELECTOR = "sidebar/perspective"; //$NON-NLS-1$
	public static final String SHOWTOOLBARITEMS = "sidebar/toolbaritems"; //$NON-NLS-1$
	
	// Persönliche Präferenzen
	public static final String USR_DEFCASELABEL = "fall/std_label"; //$NON-NLS-1$
	public static final String USR_DEFCASELABEL_DEFAULT = Messages.PreferenceConstants_general;
	public static final String USR_DEFCASEREASON = "fall/std_grund"; //$NON-NLS-1$
	public static final String USR_DEFCASEREASON_DEFAULT = Messages.PreferenceConstants_illness;
	public static final String USR_DEFLAW = "fall/std_gesetz"; //$NON-NLS-1$
	public static final String USR_DEFDIAGNOSE = "fall/std_diagnose"; //$NON-NLS-1$
	public static final String USR_DEFLOADCONSALL = "fall/load_consall"; //$NON-NLS-1$
	public static final String USR_TOPITEMSSORTING = "fall/topitemssorting"; //$NON-NLS-1$
	public static final String USR_REMINDERCOLORS = "reminder/colors"; //$NON-NLS-1$
	public static final String USR_REMINDERSOPEN = "reminder/onlyopen"; //$NON-NLS-1$
	public static final String USR_REMINDEROWN = "reminder/originator"; //$NON-NLS-1$
	public static final String USR_REMINDEROTHERS = "reminder/others"; //$NON-NLS-1$
	public static final String USR_REMINDERS_SHOW_ONLY_FROM_CURRENT_PATIENT = "reminder/overrideShowOnlyFromCurrentPatientReminderAction"; //$NON-NLS-1$
	public static final String USR_SHOWPATCHGREMINDER = "reminder/showPatientChangeReminder"; //$NON-NLS-1$
	public static final String USR_REMINDER_PAT_LABEL_CHOOSEN = "reminder/patientlabel/choosen";
	public static final String USR_REMINDER_PAT_LABEL_AVAILABLE = "reminder/patientlabel/available";
	public static final String USR_REMINDER_AUTO_SELECT_PATIENT = "reminder/autoSelectPatient"; //$NON-NLS-1$
	public static final String USR_MFU_LIST_SIZE = "mfulist/size"; //$NON-NLS-1$
	public static final String USR_PLAF = "anwender/plaf"; //$NON-NLS-1$
	public static final String USR_DEFAULTFONT = "anwender/stdfont"; //$NON-NLS-1$
	public static final String USR_SMALLFONT = "anwender/smallfont"; //$NON-NLS-1$
	public static final String USR_PATLIST_SHOWPATNR = "anwender/patlist/zeigenr"; //$NON-NLS-1$
	public static final String USR_PATLIST_SHOWNAME = "anwender/patlist/zeigename"; //$NON-NLS-1$
	public static final String USR_PATLIST_SHOWFIRSTNAME = "anwender/patlist/zeigevorname"; //$NON-NLS-1$
	public static final String USR_PATLIST_SHOWDOB = "anwender/patlist/zeigegebdat"; //$NON-NLS-1$
	public static final String USR_PATLIST_FOCUSFIELD = "anwender/patlist/focusfield"; //$NON-NLS-1$
	public static final String USR_MANDATOR_COLORS_PREFIX = "mandanten/farben/";
	public static final String USR_MESSAGES_SOUND_ON = "messages/playsound";
	public static final String USR_MESSAGES_SOUND_PATH = "messages/soundpath";
	public static final String USR_MESSAGES_ANSWER_AUTOCLEAR = "messages/answer/autoclear";
	public static final String USR_SERVICES_DIAGNOSES = "servicediagnose/";
	public static final String USR_SERVICES_DIAGNOSES_SRV = "servicediagnose/srv";
	public static final String USR_SERVICES_DIAGNOSES_DIAGNOSE = "servicediagnose/diagnose";
	public static final String USR_SERVICES_DIAGNOSES_CODES = "servicediagnose/codes";
	
	// Menu item "lock perspectives" (GlobalActions.fixLayoutAction)
	public static final String USR_FIX_LAYOUT = "perspectives/fix_layout"; //$NON-NLS-1$
	public static final boolean USR_FIX_LAYOUT_DEFAULT = false;
	
	// Rechnungen
	public static final String RNN_DEFAULTEXPORTMODE = "rechnung/default_target"; //$NON-NLS-1$
	public static final String RNN_DAYSUNTIL1ST = "rechnung/days_until_1st"; //$NON-NLS-1$
	public static final String RNN_DAYSUNTIL2ND = "rechnung/days_until_2nd"; //$NON-NLS-1$
	public static final String RNN_DAYSUNTIL3RD = "rechnung/days_until_3rd"; //$NON-NLS-1$
	public static final String RNN_AMOUNT1ST = "rechnung/amount_1st"; //$NON-NLS-1$
	public static final String RNN_AMOUNT2ND = "rechnung/amount_2nd"; //$NON-NLS-1$
	public static final String RNN_AMOUNT3RD = "rechnung/amount_3rd"; //$NON-NLS-1$
	public static final String RNN_REMOVE_OPEN_REMINDER = "rechnung/reminder/removeopen";
	// Lager
	public static final String INVENTORY_CHECK_ILLEGAL_VALUES = "inventory/check_values"; //$NON-NLS-1$
	public static final boolean INVENTORY_CHECK_ILLEGAL_VALUES_DEFAULT = true;
	public static final String INVENTORY_DEFAULT_ARTICLE_PROVIDER = "inventory/defaultArticleProvider"; //$NON-NLS-1$
	public static final String INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES = "inventory/machineOutlayPartialPackages"; //$NON-NLS-1$
	public static final boolean INVENTORY_MACHINE_OUTLAY_PARTIAL_PACKAGES_DEFAULT = false;
	public static final String INVENTORY_ORDER_TRIGGER = "inventory/order_trigger";
	public static final int INVENTORY_ORDER_TRIGGER_EQUAL = 1;
	public static final int INVENTORY_ORDER_TRIGGER_BELOW = 0;
	public static final int INVENTORY_ORDER_TRIGGER_DEFAULT =
		INVENTORY_ORDER_TRIGGER_BELOW;
	
	// Labor
	public static final String DAYS_TO_KEEP_UNSEEN_LAB_RESULTS = "3"; //$NON-NLS-1$
	
	// Labsettings
	public static final String LABSETTINGS_CFG_KEEP_UNSEEN_LAB_RESULTS = "lab/keepUnseen"; //$NON-NLS-1$
	public static final String LABSETTINGS_CFG_LABNEW_HEARTRATE = "lab/heartrate_unseen"; //$NON-NLS-1$
	public static final String LABSETTINGS_CFG_LOCAL_REFVALUES = "lab/localRefValues"; //$NON-NLS-1$
	public static final String LABSETTINGS_CFG_SHOW_MANDANT_ORDERS_ONLY = "lab/showMandantOnly"; //$NON-NLS-1$
	public static final String LABSETTINGS_PREFIX_CFG_EVAL = "lab/eval/"; //$NON-NLS-1$
	public static final String LABSETTINGS_CFG_EVAL_PREFIX_TYPE_ABSOLUT =
		LABSETTINGS_PREFIX_CFG_EVAL + "tAbsolut/"; //$NON-NLS-1$
	public static final String LABSETTINGS_CFG_EVAL_PREFIX_TYPE_TEXT =
		LABSETTINGS_PREFIX_CFG_EVAL + "tText/"; //$NON-NLS-1$
	public static final String LABSETTINGS_CFG_EVAL_REFVAL_NON_EQUAL_RESVAL_MEANS_PATHOLOGIC =
		"refValNonEqualResValMeansPath"; //$NON-NLS-1$
	
	// Scanner
	public static final String SCANNER_PREFIX_CODE = "scanner/prefixcode"; //$NON-NLS-1$
	public static final String SCANNER_POSTFIX_CODE = "scanner/postfixcode"; //$NON-NLS-1$
	public static final String BARCODE_LENGTH = "scanner/barcodelength"; //$NON-NLS-1$
	
	// Leistungscodes
	public final static String LEISTUNGSCODES_OBLIGATION = "billing/obligation"; //$NON-NLS-1$
	public final static String LEISTUNGSCODES_CFG_KEY = "billing/systems"; //$NON-NLS-1$
	public final static String LEISTUNGSCODES_BILLING_STRICT = "billing/strict"; //$NON-NLS-1$
	public final static String LEISTUNGSCODES_BILLING_ZERO_CHECK = "billing/zero_check"; //$NON-NLS-1$
	public final static String LEISTUNGSCODES_OPTIFY = "billing/optify"; //$NON-NLS-1$
	public final static String LEISTUNGSCODES_OPTIFY_XRAY = "billing/optify/XRAY"; //$NON-NLS-1$
	public final static String LEISTUNGSCODES_COLOR = "billing/color/"; //$NON-NLS-1$
	public final static String LEISTUNGSCODES_EIGENLEISTUNG_USEMULTI_SYSTEMS =
		"billing/eigenleistung/usemultiplier/systems"; //$NON-NLS-1$
	
}
