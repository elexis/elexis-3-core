/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    A. Kaufmann - better support for IDataAccess
 *    H. Marlovits - introduced SQL Fields
 * 
 *******************************************************************************/

package ch.elexis.core.ui.text;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.data.util.ScriptUtil;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.text.XRefExtensionConstants;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.DocumentSelectDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.preferences.TextTemplatePreferences;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Brief;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.data.Script;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

public class TextContainer {
	private static final String WARNING_SIGN = "??"; //$NON-NLS-1$
	public static final String MATCH_SQUARE_BRACKET = "[\\[\\]]"; //$NON-NLS-1$
	private static final String TEMPLATE_NOT_FOUND_HEADER =
		Messages.TextContainer_TemplateNotFoundHeader;
	private static final String TEMPLATE_NOT_FOUND_BODY =
		Messages.TextContainer_TemplateNotFoundBody;
	
	private ITextPlugin plugin = null;
	private static Logger log = LoggerFactory.getLogger(TextContainer.class); //$NON-NLS-1$
	private Shell shell;
	private static final String DONT_SHOW_REPLACEMENT_ERRORS = "*";
	public static final String MATCH_TEMPLATE = "\\[[" + DONT_SHOW_REPLACEMENT_ERRORS //$NON-NLS-1$
		+ "]?[-a-zA-ZäöüÄÖÜéàè_ ]+\\.[-a-zA-Z0-9äöüÄÖÜéàè_ ]+\\]";
	public static final String MATCH_INDIRECT_TEMPLATE = "\\[[" + DONT_SHOW_REPLACEMENT_ERRORS //$NON-NLS-1$
		+ "]?[-a-zA-ZäöüÄÖÜéàè_ ]+(\\.[-a-zA-Z0-9äöüÄÖÜéàè_ ]+)+\\]";
	public static final String MATCH_GENDERIZE =
		"\\[[" + DONT_SHOW_REPLACEMENT_ERRORS + "]?[a-zA-Z]+:mwn?:[^\\[]+\\]"; //$NON-NLS-1$
	//public static final String MATCH_IDATACCESS = "\\[[-_a-zA-Z0-9]+:[-a-zA-Z0-9]+:[-a-zA-Z0-9\\.]+:[-a-zA-Z0-9\\.]:?.*\\]"; //$NON-NLS-1$
	public static final String MATCH_IDATACCESS = "\\[[" + DONT_SHOW_REPLACEMENT_ERRORS //$NON-NLS-1$
		+ "]?[-_a-zA-Z0-9]+:[-a-zA-Z0-9]+:[-a-zA-Z0-9\\.]+:[-a-zA-Z0-9\\.]:?[^\\]]*\\]";
	public static final String MATCH_SQLCLAUSE =
		"\\[[" + DONT_SHOW_REPLACEMENT_ERRORS + "]?SQL[^:]*:[^\\[]+\\]"; //$NON-NLS-1$
	public static final String DISALLOWED_SQLEXPRESSIONS = "DROP,UPDATE,CREATE,INSERT"; //$NON-NLS-1$
	//public static final String MATCH_SCRIPT = "\\["+Script.SCRIPT_MARKER+".+\\]"; //$NON-NLS-1$
	public static final String MATCH_SCRIPT = "\\[" + Script.SCRIPT_MARKER + "[^\\[]+\\]"; //$NON-NLS-1$
	
	public static Connection queryConn = null;
	
	/**
	 * Der Konstruktor sucht nach dem in den Settings definierten Textplugin Wenn er kein Textplugin
	 * findet, wählt er ein rudimentäres Standardplugin aus (das in der aktuellen Version nur eine
	 * Fehlermeldung ausgibt)
	 */
	public TextContainer(){
		if (plugin == null) {
			String ExtensionToUse = CoreHub.localCfg.get(Preferences.P_TEXTMODUL, null);
			IExtensionRegistry exr = Platform.getExtensionRegistry();
			IExtensionPoint exp =
				exr.getExtensionPoint(ExtensionPointConstantsUi.TEXTPROCESSINGPLUGIN);
			if (exp != null) {
				IExtension[] extensions = exp.getExtensions();
				for (IExtension ex : extensions) {
					IConfigurationElement[] elems = ex.getConfigurationElements();
					for (IConfigurationElement el : elems) {
						if ((ExtensionToUse == null) || el.getAttribute("name").equals( //$NON-NLS-1$
							ExtensionToUse)) {
							try {
								plugin = (ITextPlugin) el.createExecutableExtension("Klasse"); //$NON-NLS-1$
							} catch (/* Core */Exception e) {
								ExHandler.handle(e);
							}
						}
						
					}
				}
			}
		}
		if (plugin == null) {
			plugin = new DefaultTextPlugin();
		}
	}
	
	public TextContainer(final IViewSite s){
		this();
		shell = s.getShell();
	}
	
	public TextContainer(final Shell s){
		this();
		shell = s;
	}
	
	public void setFocus(){
		plugin.setFocus();
	}
	
	public ITextPlugin getPlugin(){
		return plugin;
	}
	
	public void dispose(){
		plugin.dispose();
	}
	
	private Brief loadTemplate(String name){
		Query<Brief> qbe = new Query<Brief>(Brief.class);
		qbe.add(Brief.FLD_TYPE, Query.EQUALS, Brief.TEMPLATE);
		qbe.and();
		qbe.add(Brief.FLD_SUBJECT, Query.EQUALS, name);
		qbe.startGroup();
		qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, CoreHub.actMandant.getId());
		qbe.or();
		qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, StringTool.leer);
		qbe.endGroup();
		List<Brief> list = qbe.execute();
		if ((list == null) || (list.size() == 0)) {
			return null;
		}
		Brief template = list.get(0);
		return template;
	}
	
	/**
	 * Ein Dokument aus einer namentlich genannten Vorlage erstellen. Die Vorlage muss entweder dem
	 * aktuellen Mandanten oder allen Mandanten zugeordet sein.
	 * 
	 * @param templatename
	 *            Name der Vorlage. Wenn in der lokalen Konfiguration
	 *            (Datei-Einstellungen-Textvorlagen) eine Alternative zu diesem Vorlagennamen
	 *            hinterlegt ist, wird diese genommen
	 * @param typ
	 *            Typ des zu erstellenden Dokuments
	 * @param adressat
	 *            Adressat
	 * @param subject
	 *            TODO
	 * @return Ein Brief-Objekt oder null bei Fehler
	 */
	
	public Brief createFromTemplateName(final Konsultation kons, final String templatenameRaw,
		final String typ, final Kontakt adressat, final String subject){
		String suffix = CoreHub.localCfg.get(TextTemplatePreferences.SUFFIX_STATION, "");
		Brief template = loadTemplate(templatenameRaw + suffix);
		if (template == null && suffix.length() > 0) {
			template = loadTemplate(templatenameRaw);
		}
		if (template == null) {
			SWTHelper.showError(TEMPLATE_NOT_FOUND_HEADER,
				TEMPLATE_NOT_FOUND_BODY + templatenameRaw);
			return null;
		}
		
		return createFromTemplate(kons, template, typ, adressat, subject);
	}
	
	/**
	 * Ein Dokument aus einer Vorlage erstellen. Dabei werden Datensatz-Variablen durch die
	 * entsprechenden Inhalte ersetzt und geschlechtsspezifische Formulierungen entsprechend
	 * gewählt.
	 * 
	 * @param template
	 *            die Vorlage
	 * @param typ
	 *            Typ des zu erstellenden Dokuments
	 * @param subject
	 *            TODO
	 * @param Adressat
	 *            der Adressat
	 * @return true bei Erfolg
	 */
	public Brief createFromTemplate(final Konsultation kons, final Brief template, final String typ,
		Kontakt adressat, final String subject){
		if (adressat == null) {
			KontaktSelektor ksel = new KontaktSelektor(shell, Kontakt.class,
				Messages.TextContainer_SelectDestinationHeader,
				Messages.TextContainer_SelectDestinationBody, Kontakt.DEFAULT_SORT);
			if (ksel.open() != Dialog.OK) {
				return null;
			}
			adressat = (Kontakt) ksel.getSelection();
		}
		// Konsultation kons=getBehandlung();
		if (template == null) {
			if (plugin.createEmptyDocument()) {
				Brief brief =
					new Brief(subject == null ? Messages.TextContainer_EmptyDocument : subject,
						null, CoreHub.actUser, adressat, kons, typ);
				addBriefToKons(brief, kons);
				return brief;
			}
		} else {
			if (plugin.loadFromByteArray(template.loadBinary(), true) == true) {
				final Brief ret = new Brief(subject == null ? template.getBetreff() : subject, null,
					CoreHub.actUser, adressat, kons, typ);
				plugin.initTemplatePrintSettings(template.getBetreff());
				
				plugin.findOrReplace(MATCH_TEMPLATE, new ReplaceCallback() {
					public Object replace(final String in){
						return replaceFields(ret,
							in.replaceAll(MATCH_SQUARE_BRACKET, StringTool.leer));
					}
				});
				plugin.findOrReplace(MATCH_INDIRECT_TEMPLATE, new ReplaceCallback() {
					public Object replace(final String in){
						return replaceIndirectFields(ret,
							in.replaceAll(MATCH_SQUARE_BRACKET, StringTool.leer));
					}
				});
				plugin.findOrReplace(MATCH_GENDERIZE, new ReplaceCallback() {
					public String replace(final String in){
						return genderize(ret, in.replaceAll(MATCH_SQUARE_BRACKET, StringTool.leer));
					}
				});
				plugin.findOrReplace(MATCH_IDATACCESS, new ReplaceCallback() {
					public Object replace(final String in){
						return ScriptUtil.loadDataFromPlugin(
							in.replaceAll(MATCH_SQUARE_BRACKET, StringTool.leer));
					}
				});
				plugin.findOrReplace(MATCH_SQLCLAUSE, new ReplaceCallback() {
					public Object replace(final String in){
						return replaceSQLClause(ret,
							in.replaceAll(MATCH_SQUARE_BRACKET, StringTool.leer));
					}
				});
				plugin.findOrReplace(MATCH_SCRIPT, new ReplaceCallback() {
					
					@Override
					public Object replace(String in){
						return executeScript(ret,
							in.replaceAll(MATCH_SQUARE_BRACKET, StringTool.leer));
					}
				});
				saveBrief(ret, typ);
				addBriefToKons(ret, kons);
				return ret;
			}
		}
		return null;
	}
	
	private Object replaceFields(final Brief brief, final String b){
		String bl = b;
		boolean showErrors = true;
		if (bl.substring(0, 1).equalsIgnoreCase(DONT_SHOW_REPLACEMENT_ERRORS)) {
			bl = bl.substring(1);
			showErrors = false;
		}
		String[] q = bl.split("\\."); //$NON-NLS-1$
		if (q.length != 2) {
			log.warn(Messages.TextContainer_BadVariableFormat + bl); // Kann
			// eigentlich
			// nie
			// vorkommen
			// ?!?
			return null;
		}
		if (q[0].equals("Datum")) { //$NON-NLS-1$
			return new TimeTool().toString(TimeTool.DATE_GER);
		}
		if (q[0].indexOf(":") != -1) { //$NON-NLS-1$
			return ScriptUtil.loadDataFromPlugin(bl);
		}
		PersistentObject o = (PersistentObject) resolveObject(brief, q[0]);
		if (o == null) {
			if (showErrors) {
				return WARNING_SIGN + bl + WARNING_SIGN;
			} else {
				return "";
			}
		}
		
		String ret = o.get(q[1]);
		if ((ret == null) || (ret.startsWith("**"))) { //$NON-NLS-1$
			
			if (!(o.map(PersistentObject.FLD_EXTINFO).startsWith("**"))) { //$NON-NLS-1$
				@SuppressWarnings("rawtypes")
				Map ext = o.getMap(PersistentObject.FLD_EXTINFO);
				String an = (String) ext.get(q[1]);
				if (an != null) {
					return an;
				}
			}
			log.warn("Nicht erkanntes Feld in " + bl); //$NON-NLS-1$
			if (showErrors) {
				return "???" + bl + "???";
			} else {
				return "";
			}
		}
		
		if (ret.startsWith("<?xml")) { //$NON-NLS-1$
			Samdas samdas = new Samdas(ret);
			ret = samdas.getRecordText();
		}
		return ret;
	}
	
	/**
	 * Resolve an indirect field, e. g. Fall.Kostentrager.Bezeichnung1
	 * 
	 * @param brief
	 *            the curren Brief
	 * @param field
	 *            the filed to resolv
	 * @return the resolved value
	 */
	private Object replaceIndirectFields(final Brief brief, final String field){
		String fieldl = field;
		boolean showErrors = true;
		if (fieldl.substring(0, 1).equalsIgnoreCase(DONT_SHOW_REPLACEMENT_ERRORS)) {
			fieldl = fieldl.substring(1);
			showErrors = false;
		}
		String[] tokens = fieldl.split("\\."); //$NON-NLS-1$
		if (tokens.length <= 2) {
			if (showErrors) {
				return WARNING_SIGN + fieldl + WARNING_SIGN;
			} else {
				return "";
			}
		}
		
		String firstToken = tokens[0];
		String valueToken = tokens[tokens.length - 1];
		
		// resolve the first field
		IPersistentObject first = resolveObject(brief, firstToken);
		if (first == null) {
			if (showErrors) {
				return WARNING_SIGN + fieldl + WARNING_SIGN;
			} else {
				return "";
			}
		}
		
		// resolve intermediate objects
		IPersistentObject current = first;
		for (int i = 1; i < tokens.length - 1; i++) {
			IPersistentObject next = resolveIndirectObject(current, tokens[i]);
			if (next == null) {
				if (showErrors) {
					return WARNING_SIGN + fieldl + WARNING_SIGN;
				} else {
					return "";
				}
			}
			current = next;
		}
		
		// resolve value
		
		IPersistentObject o = current;
		
		String value = o.get(valueToken);
		if ((value == null) || (value.startsWith("**"))) { //$NON-NLS-1$
			log.warn("Nicht erkanntes Feld in " + fieldl); //$NON-NLS-1$
			if (showErrors) {
				return WARNING_SIGN + fieldl + WARNING_SIGN;
			} else {
				return "";
			}
		}
		
		if (value.startsWith("<?xml")) { //$NON-NLS-1$
			Samdas samdas = new Samdas(value);
			value = samdas.getRecordText();
		}
		return value;
	}
	
	private IPersistentObject resolveIndirectObject(IPersistentObject parent, String field){
		if (parent instanceof Fall) {
			String fieldl = field;
			if (fieldl.substring(0, 1).equalsIgnoreCase(DONT_SHOW_REPLACEMENT_ERRORS)) {
				fieldl = fieldl.substring(1);
			}
			Fall fall = (Fall) parent;
			
			return fall.getReferencedObject(fieldl);
		} else {
			// not yet supported
			return null;
		}
	}
	
	/**
	 * Execute a Script to convert input value to output value. format: SCRIPT:scriptname scriptname
	 * can be of the form name(param,param)
	 * 
	 * @param ret
	 *            the Brief to fill in
	 * @param in
	 *            the input String to replace
	 * @return the converted String
	 */
	private String executeScript(final Brief ret, final String in){
		String[] q = in.split(":");
		if (q.length != 2) {
			log.error("Falsches SCRIPT format: " + in);
			return "???SYNTAX???";
			
		}
		try {
			Object result = Script.executeScript(q[1], ret);
			return result == null ? q[1] : result.toString();
		} catch (ElexisException e) {
			SWTHelper.showError("Fehler beim Ausführen des Scripts", e.getMessage());
			return "??SCRIPT ERROR??";
		}
	}
	
	/**
	 * Format für Genderize: [Feld:mw:formulierung Mann/formulierung Frau] oder
	 * [Feld:mwn:mann/frau/neutral]
	 */
	private String genderize(final Brief brief, final String in){
		String inl = in;
		boolean showErrors = true;
		if (inl.substring(0, 1).equalsIgnoreCase(DONT_SHOW_REPLACEMENT_ERRORS)) {
			inl = inl.substring(1);
			showErrors = false;
		}
		String[] q = inl.split(":"); //$NON-NLS-1$
		IPersistentObject o = resolveObject(brief, q[0]);
		if (o == null) {
			if (showErrors) {
				return "???";
			} else {
				return "";
			}
		}
		if (q.length != 3) {
			log.error("falsches genderize Format " + inl); //$NON-NLS-1$
			return null;
		}
		if (!(o instanceof Kontakt)) {
			if (showErrors) {
				return Messages.TextContainer_FieldTypeForContactsOnly;
			} else {
				return "";
			}
		}
		Kontakt k = (Kontakt) o;
		String[] g = q[2].split("/"); //$NON-NLS-1$
		if (g.length < 2) {
			if (showErrors) {
				return Messages.TextContainer_BadFieldDefinition;
			} else {
				return "";
			}
		}
		if (k.istPerson()) {
			Person p = Person.load(k.getId());
			
			if (p.get(Person.SEX).equals(Person.MALE)) {
				if (q[1].startsWith("m")) { //$NON-NLS-1$
					return g[0];
				}
				return g[1];
			} else {
				if (q[1].startsWith("w")) { //$NON-NLS-1$
					return g[0];
				}
				return g[1];
			}
		} else {
			if (g.length < 3) {
				if (showErrors) {
					return Messages.TextContainer_FieldTypeForPersonsOnly;
				} else {
					return "";
				}
			}
			return g[2];
		}
	}
	
	private IPersistentObject resolveObject(final Brief actBrief, final String k){
		String kl = k;
		if (kl.substring(0, 1).equalsIgnoreCase(DONT_SHOW_REPLACEMENT_ERRORS)) {
			kl = kl.substring(1);
		}
		IPersistentObject ret = null;
		if (kl.equalsIgnoreCase("Mandant")) { //$NON-NLS-1$
			ret = CoreHub.actMandant;
		} else if (kl.equalsIgnoreCase("Anwender")) { //$NON-NLS-1$
			ret = CoreHub.actUser;
		} else if (kl.equalsIgnoreCase("Adressat")) { //$NON-NLS-1$
			ret = actBrief.getAdressat();
		} else {
			try {
				String fqname = "ch.elexis.data." + kl; //$NON-NLS-1$
				ret = ElexisEventDispatcher.getSelected(Class.forName(fqname));
			} catch (Throwable ex) {
				log.warn(Messages.TextContainer_UnrecognizedFieldType + kl);
				ret = null;
			}
		}
		if (ret == null) {
			log.warn(Messages.TextContainer_UnrecognizedFieldType + kl);
		}
		return ret;
	}
	
	/*
	 * Für eine eingebettete SQL-Abfrage in Office-Dokumenten Format des Platzhalters, drei
	 * Varianten: [SQL:<Hier kommt die SQL-Abfrage hin>] [SQL|<FeldTrenner>:<Hier kommt die
	 * SQL-Abfrage hin>] [SQL|<FeldTrenner>|<DatensatzTrenner>:<Hier kommt die SQL-Abfrage hin>]
	 * 
	 * Die Feldtrenner können beliebige Strings sein. Wird kein FeldTrenner respektive kein
	 * DatensatzTrenner angegeben, so werden Defaults verwendet: für FeldTrenner: Tab \t für
	 * DatensatzTrenner: newLine \n In den Trennern können die escape characters \n, \r, \t, \f, \b
	 * verwendet werden. Octal-Escapes werden zur Zeit nicht unterstützt.
	 * 
	 * In der SQL-Abfrage können alle üblichen direkten und indirekten Platzhalter verwendet werden,
	 * zBsp [Patient.ID] oder [Fall.ID], [Mandant.Vorname], [Konsultation.Datum] etc. Diese werden
	 * zuerst ersetzt. Danach wird die eigentliche Abfrage durchgeführt.
	 * 
	 * Um die im Datenbankfeld "ExtInfo" gespeichterten Daten abzurufen, ist die folgende
	 * Hilfssyntax als Feldabfrage vorgesehen:
	 * extinfo:<TabellenName>.<FeldNameInnerhalbDerHashtableAusDerExtinfo> Bsp:
	 * extinfo:KONTAKT:Beruf Das Feld Beruf wurde in den Einstellungen
	 * "Zusatzfelder in Patient-Detail-Blatt" definiert
	 * 
	 * 
	 * Auf diese Weise lässt sich so ziemlich alles in ziemlich jeglicher Form zur Darstellung
	 * extrahieren.
	 * 
	 * 
	 * Beispiele:
	 * 
	 * Abfrage: [SQL:select chr(9) || prozent || '%', to_char(to_date(datumvon, 'yyyymmdd'),
	 * 'dd.mm.yyyy'), '-', to_char(to_date(datumbis, 'yyyymmdd'), 'dd.mm.yyyy') from auf where
	 * fallid='[Fall.ID]'] Resultat: 100% 01.01.2010 - 07.01.2010 50% 08.01.2010 - 15.01.2010
	 * 
	 * Abfrage> [SQL|_\t_|\n%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%\n: select extinfo:KONTAKT.Beruf,
	 * extinfo:KONTAKT.Ledigname, Bezeichnung1, Bezeichnung2 from KONTAKT where id ='[Patient.ID]'
	 * or id='1029'] Resultat: Schreiner_ _Bünzli_ _Hagenmüller_ _Margrit
	 * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%% Lehrer_ _Germann_ _Marlovits_ _Annegret
	 * 
	 * 
	 * 
	 * **************************************************************************
	 * ******************************
	 */
	
	static public String replaceSQLClause(final String b){
		TextContainer tmpMe = new TextContainer();
		String result = (String) tmpMe.replaceSQLClause(null, b);
		tmpMe.dispose();
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private Object replaceSQLClause(final Brief brief, final String b){
		String bl = b;
		boolean showErrors = true;
		if (bl.substring(0, 1).equalsIgnoreCase(DONT_SHOW_REPLACEMENT_ERRORS)) {
			bl = bl.substring(1);
			showErrors = false;
		}
		// get db/statement
		JdbcLink j = PersistentObject.getConnection();
		Stm stm = j.getStatement();
		
		// get fieldDelimiter and rowDelimiter from params, else provide
		// default-values tab and newline
		String sql = bl;
		String sqlPrefix = sql.split(":")[0];
		String[] sqlPrefixParts = sqlPrefix.split("\\|");
		String fieldDelimiter = "	"; // default: tab
		String rowDelimiter = "\n"; // default: newline
		if (sqlPrefixParts.length > 1) {
			fieldDelimiter = sqlPrefixParts[1];
			// replace escape sequences
			fieldDelimiter = convertSpecialCharacters(fieldDelimiter);
		}
		if (sqlPrefixParts.length > 2) {
			rowDelimiter = sqlPrefixParts[2];
			// replace escape sequences
			rowDelimiter = convertSpecialCharacters(rowDelimiter);
		}
		
		// strip SQL-selector from start of string -> actual SQL-statement in
		// sql
		sql = sql.substring(sqlPrefix.length() + 1);
		
		// SAFETY: disallow clauses with DROP, UPDATE, INSERT and CREATE
		String[] disallowedList = DISALLOWED_SQLEXPRESSIONS.split(",");
		for (int i = 0; i < disallowedList.length; i++) {
			String disallowed = disallowedList[i];
			Pattern p = Pattern.compile("[^_^\\w]*" + disallowed + "\\s", Pattern.MULTILINE);
			Matcher m = p.matcher(sql);
			while (m.find()) {
				return "??? '" + disallowed + "' ist in SQL-Platzhaltern nicht erlaubt ???";
			}
		}
		
		// preprocess SQL-statement:
		// enclose extinfo:<tableName>.<hashTableFieldName> with apostrophs
		// -> query will just return the string itself without error
		// will be processed later
		Pattern p = Pattern.compile("extinfo:[\\w]+\\.[\\w]+[^\\w]", Pattern.MULTILINE);
		Matcher m = p.matcher(sql);
		while (m.find()) {
			// get extinfo
			String part = m.group();
			// strip the delimiter [^\\w] from the end of the string
			String stringWithoutDelim = part.substring(0, part.length() - 1);
			// get the delimiter [^\\w]
			String delim = part.substring(part.length() - 1);
			// get the part <tableName> by stripping "extinfo:" and getting then
			// the part left of "."
			String tablePart = stringWithoutDelim.substring("extinfo:".length()).split("\\.")[0];
			// replace the found string: don't change original, just append
			// fieldPart to the query
			// this way, the query returns the contents/the hashtable right
			// after the textSpec
			// will be processed later
			sql = sql.replace(m.group(),
				"'" + stringWithoutDelim + "', " + tablePart + ".extinfo" + delim);
		}
		
		// execute query
		if (queryConn == null) {
			queryConn = j.getKeepAliveConnection();
		}
		Statement statement = null;
		ResultSet rs = null;
		try {
			statement = queryConn.createStatement();
			rs = statement.executeQuery(sql);
		} catch (SQLException e1) {
			j.releaseStatement(stm);
			try {
				if (statement != null) {
					statement.close();
				}
			} catch (SQLException e) {}
			if (showErrors) {
				return "[???" + bl + " ***" + e1.getMessage() + "*** ???]";
			} else {
				return "";
			}
		}
		
		// create result by reading rows/fields and extracting hashTable fields
		// from extInfo
		String fieldContent = "";
		String result = "";
		String lRowDelimiter = "";
		try {
			// loop through all rows of resultSet
			while (rs.next()) {
				String delimiter = "";
				result = result + lRowDelimiter;
				lRowDelimiter = rowDelimiter;
				// loop through columns
				for (int i = 1; i < 1000; i++) { // hope 1000 cols is enough...
					// list all fields, delimiter = tab
					try {
						// read field contents
						fieldContent = rs.getString(i);
						// if field starts with "extinfo:" then read data from
						// db-field extinfo/hashtable
						if ((fieldContent.length() >= "extinfo:".length()) && fieldContent
							.substring(0, "extinfo:".length()).equalsIgnoreCase("extinfo:")) {
							String extInfoSpec = fieldContent.substring("extinfo:".length());
							String extInfoField = extInfoSpec.split("\\.")[1];
							// the actual blob contents can be found in the
							// following field - read blob
							i++;
							byte[] blob = rs.getBytes(i);
							if (blob == null) {
								fieldContent = "";
							} else {
								// get hashTable, read field
								Hashtable<Object, Object> ht = fold(blob);
								fieldContent = (String) ht.get(extInfoField);
							}
						}
						// append field to result
						result = result + delimiter + (fieldContent == null ? "" : fieldContent);
						delimiter = fieldDelimiter;
					} catch (Exception e) {
						// this just catches the case where i > num of
						// columns...
						break;
					}
				}
				// result = result + rowDelimiter;
			}
		} catch (SQLException e) {
			j.releaseStatement(stm);
			try {
				statement.close();
			} catch (SQLException e1) {}
			if (showErrors) {
				return "[???" + bl + " ***" + e.getMessage() + "*** ???]";
			} else {
				return "";
			}
		}
		// aufräumen
		j.releaseStatement(stm);
		try {
			statement.close();
		} catch (SQLException e) {}
		return result;
	}
	
	@SuppressWarnings("unchecked")
	private Hashtable fold(final byte[] flat){
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(flat);
			ZipInputStream zis = new ZipInputStream(bais);
			zis.getNextEntry();
			ObjectInputStream ois = new ObjectInputStream(zis);
			Hashtable<Object, Object> res = (Hashtable<Object, Object>) ois.readObject();
			ois.close();
			bais.close();
			return res;
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return null;
		}
	}
	
	private String convertSpecialCharacters(final String in){
		// \ddd how to replace octal values?
		String result = in;
		result = result.replaceAll("\\\\n", "\n");
		result = result.replaceAll("\\\\t", "\t");
		result = result.replaceAll("\\\\b", "\b");
		result = result.replaceAll("\\\\r", "\r");
		result = result.replaceAll("\\\\f", "\f");
		return result;
	}
	
	private void addBriefToKons(final Brief brief, final Konsultation kons){
		if (kons != null) {
			if (CoreHub.getLocalLockService().acquireLock(kons).isOk()) {
				String label = "\n[ " + brief.getLabel() + " ]"; //$NON-NLS-1$ //$NON-NLS-2$
				kons.addXRef(XRefExtensionConstants.providerID, brief.getId(), -1, label);
				CoreHub.getLocalLockService().releaseLock(kons);
			}
		}
	}
	
	/**
	 * Dokument speichern. Wenn noch kein Adressat vorhanden ist, wird eine Auswahl angeboten.
	 * 
	 * @param brief
	 *            das zu speichernde Dokument
	 * @param typ
	 *            Typ des Dokuments
	 */
	public void saveBrief(Brief brief, final String typ){
		log.debug(
			"ch.elexis.views/TextContainer.java saveBrief(Brief brief, final String typ): begin");
		
		if (brief == null) {
			log.debug("ch.elexis.views/TextContainer.java saveBrief(): WARNING: brief == null");
		} else {
			log.debug(
				"ch.elexis.views/TextContainer.java saveBrief(): brief == " + brief.toString());
			log.debug("ch.elexis.views/TextContainer.java saveBrief(): brief.getBetreff() == "
				+ brief.getBetreff());
			// 20130425js: Das hier lieber nicht machen: Das öffnet interaktiv das Dialogfenster zur
			// Adressauswahl; unpassend als Debug-Output.
			// log.debug("ch.elexis.views/TextContainer.java saveBrief(): brief.getAdressat() == "+brief.getAdressat());
		}
		
		if ((brief == null) || (brief.getAdressat() == null)) {
			
			// TODO: 20130425js added this: Nur Hinweis auf möglichen Bedienfehler im Log (Keine
			// Konsultation beim Erstellen eines Briefes)
			if (Konsultation.getAktuelleKons() == null) {
				log.debug(
					"ch.elexis.views/TextContainer.java saveBrief(): TODO REVIEW TODO REVIEW TODO REVIEW TODO REVIEW TODO REVIEW TODO REVIEW");
				log.debug(
					"ch.elexis.views/TextContainer.java saveBrief(): WARNING: Konsultation.getAktuelleKonsultation == null");
				log.debug(
					"ch.elexis.views/TextContainer.java saveBrief(): WARNING: Soll hier (oder etwas früher!!) vielleicht ein Abbruch der Brief-Erstellung in den Code?");
				log.debug(
					"ch.elexis.views/TextContainer.java saveBrief(): TODO REVIEW TODO REVIEW TODO REVIEW TODO REVIEW TODO REVIEW TODO REVIEW;");
			} else {
				log.debug(
					"ch.elexis.views/TextContainer.java saveBrief(): Konsultation.getAktuelleKons()=="
						+ Konsultation.getAktuelleKons() + ": "
						+ Konsultation.getAktuelleKons().getDatum());
			}
			
			KontaktSelektor ksl = new KontaktSelektor(shell, Kontakt.class,
				Messages.TextContainer_SelectAdresseeHeader,
				Messages.TextContainer_SelectAdresseeBody, Kontakt.DEFAULT_SORT);
			
			if (ksl.open() == Dialog.OK) {
				log.debug(
					"ch.elexis.views/TextContainer.java saveBrief(): about to brief = new Brief(...)");
				brief = new Brief(Messages.TextContainer_Letter, null, CoreHub.actUser,
					(Kontakt) ksl.getSelection(), Konsultation.getAktuelleKons(), typ);
			}
		}
		
		if (brief == null) {
			log.debug(
				"ch.elexis.views/TextContainer.java saveBrief(): WARNING: STILL: brief == null");
		} else {
			log.debug(
				"ch.elexis.views/TextContainer.java saveBrief(): brief == " + brief.toString());
			log.debug("ch.elexis.views/TextContainer.java saveBrief(): brief.getBetreff() == "
				+ brief.getBetreff());
			log.debug("ch.elexis.views/TextContainer.java saveBrief(): brief.getAdressat() == "
				+ brief.getAdressat());
		}
		
		if (brief != null) {
			if (StringTool.isNothing(brief.getBetreff())) {
				InputDialog dlg = new InputDialog(shell, Messages.TextContainer_SaveDocumentHeader,
					Messages.TextContainer_SaveDocumentBody, brief.getBetreff(), null);
				if (dlg.open() == Dialog.OK) {
					brief.setBetreff(dlg.getValue());
				} else {
					brief.setBetreff(brief.getTyp());
				}
			}
			
			log.debug(
				"ch.elexis.views/TextContainer.java saveBrief(): about to byte[] contents = plugin.storeToByteArray();");
			// TODO: js: why should this variable be named "contents" here, and "arr" in "open()"
			// below? Please refactor and use similar names for similar things.
			
			byte[] contents = plugin.storeToByteArray();
			if (contents == null) {
				log.debug(
					"ch.elexis.views/TextContainer.java saveBrief(): WARNING: contents == null - still proceding to brief.save(contents,...)...");
				log.error(Messages.TextContainer_NullSaveHeader);
			}
			
			log.debug(
				"ch.elexis.views/TextContainer.java saveBrief(): about to brief.save(contents,plugin.getMimeType()...");
			brief.save(contents, plugin.getMimeType());
			
			log.debug(
				"ch.elexis.views/TextContainer.java saveBrief(): about to ElexisEventDispatcher.reload(Brief.class)");
			ElexisEventDispatcher.reload(Brief.class);
		}
	}
	
	/**
	 * Den Aktuellen Inhalt des Textpuffers als Vorlage speichern. Name und zuzuordender Mandant
	 * werden per Dialog erfragt.
	 * 
	 */
	public void saveTemplate(String name){
		SaveTemplateDialog std = new SaveTemplateDialog(shell, name);
		// InputDialog dlg=new
		// InputDialog(getViewSite().getShell(),"Vorlage speichern","Geben Sie bitte einen Namen für die Vorlage ein","",null);
		if (std.open() == Dialog.OK) {
			String title = std.title;
			Brief brief =
				new Brief(title, null, CoreHub.actUser, std.selectedMand, null, Brief.TEMPLATE);
			if (std.bSysTemplate) {
				brief.set(Brief.FLD_KONSULTATION_ID, "SYS"); //$NON-NLS-1$
			}
			byte[] tmpl = plugin.storeToByteArray();
			if (tmpl == null) {
				log.error(Messages.TextContainer_NullSaveBody);
			}
			brief.save(tmpl, plugin.getMimeType());
			// text.clear();
			// set sticker for this template if not to ask for addressee
			DocumentSelectDialog.setDontAskForAddresseeForThisTemplate(brief,
				std.dontShowAddresseeSelection);
		}
	}
	
	/** Einen Brief einlesen */
	public boolean open(final Brief brief){
		log.debug("ch.elexis.views/TextContainer.java open(final Brief brief): begin - brief=="
			+ brief.toString() + ": " + brief.getBetreff());
		log.debug(
			"ch.elexisviews/TextContainer.java.open(): brief.getLabel())==" + brief.getLabel());
		log.debug(
			"ch.elexisviews/TextContainer.java.open(): about to byte[] arr = brief.loadBinary()...");
		
		byte[] arr = brief.loadBinary();
		if (arr == null) {
			log.debug(
				"ch.elexis.views/TextContainer.java open(): WARNING: arr == null -> about to return false...");
			log.warn(Messages.TextContainer_ErroneousLetter + brief.getLabel());
			return false;
		}
		
		log.debug(
			"ch.elexis.views/TextContainer.java open(): about to return plugin.loadFromByteArray(arr, false) and end...");
		return plugin.loadFromByteArray(arr, false);
	}
	
	class SaveTemplateDialog extends TitleAreaDialog {
		Text name;
		Combo cMands;
		String title;
		Button btSysTemplate;
		boolean bSysTemplate;
		List<Mandant> lMands;
		Mandant selectedMand;
		String tmplName;
		Button checkBoxDontShowAddresseeSelection;
		boolean dontShowAddresseeSelection;
		
		protected SaveTemplateDialog(final Shell parentShell, String templateName){
			super(parentShell);
			tmplName = templateName;
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.TextContainer_SaveTemplateHeader);
			setMessage(Messages.TextContainer_SaveTemplateBody);
			getShell().setText(Messages.TextContainer_Template);
		}
		
		@Override
		protected Control createDialogArea(final Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			ret.setLayout(new GridLayout());
			new Label(ret, SWT.NONE).setText(Messages.TextContainer_TemplateName);
			name = new Text(ret, SWT.BORDER);
			name.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			name.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e){
					// checkbox for sys template
					boolean isSysTemplate = isSystemTemplate(name.getText());
					btSysTemplate.setSelection(isSysTemplate);
					checkBoxDontShowAddresseeSelection.setSelection(isSysTemplate);
					checkBoxDontShowAddresseeSelection.setEnabled(!isSysTemplate);
					// checkbox for dont show addressee selection dialog
					if (!isSysTemplate)
						checkBoxDontShowAddresseeSelection.setSelection(DocumentSelectDialog
							.getDontAskForAddresseeForThisTemplateName(name.getText()));
					// show mandator: if no template matching then clear selection
					Brief brief = getBriefForTemplateName(name.getText());
					String lMandator = StringTool.leer;
					if (brief != null) {
						String mandatorID = brief.get(Brief.FLD_DESTINATION_ID);
						if (mandatorID != null) {
							Mandant lMand = Mandant.load(mandatorID);
							if (lMand.exists())
								lMandator = lMand.get(Mandant.FLD_NAME3);
						}
					}
					cMands.setText(lMandator);
				}
			});
			new Label(ret, SWT.NONE).setText(Messages.TextContainer_Mandator);
			Composite line = new Composite(ret, SWT.NONE);
			GridLayout gridLayout = new GridLayout(2, false);
			gridLayout.marginWidth = 0;
			gridLayout.marginHeight = 0;
			line.setLayout(gridLayout);
			line.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			cMands = new Combo(line, SWT.SINGLE);
			Query<Mandant> qbe = new Query<Mandant>(Mandant.class);
			lMands = qbe.execute();
			cMands.add(Messages.TextContainer_All);
			for (Mandant m : lMands) {
				cMands.add(m.getLabel());
			}
			btSysTemplate = new Button(line, SWT.CHECK);
			btSysTemplate.setText(Messages.TextContainer_SystemTemplate);
			btSysTemplate.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e){
					checkBoxDontShowAddresseeSelection.setEnabled(!btSysTemplate.getSelection());
					if (btSysTemplate.getSelection())
						checkBoxDontShowAddresseeSelection.setSelection(true);
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e){}
			});
			
			// checkbox whether an address selection should be shown when creating the doc
			checkBoxDontShowAddresseeSelection = new Button(ret, SWT.CHECK);
			checkBoxDontShowAddresseeSelection.setText(Messages.TextContainer_DontAskForAddressee);
			checkBoxDontShowAddresseeSelection
				.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			
			// at the end -> checkboxes will be set correctly by the listeners
			if (tmplName != null)
				name.setText(tmplName);
			name.setSelection(32000); // caret at end of text field instead of start
			
			return ret;
		}
		
		@Override
		protected void okPressed(){
			title = name.getText();
			if (title.length() == 0) {
				MessageDialog.openError(getShell(),
					Messages.TextContainer_TemplateTitleEmptyCaption,
					Messages.TextContainer_TemplateTitleEmptyBody);
				return;
			}
			
			bSysTemplate = btSysTemplate.getSelection();
			int i = cMands.getSelectionIndex();
			if (i != -1) {
				if (i == 0) {
					selectedMand = null;
				} else {
					selectedMand = lMands.get(i - 1);
				}
			}
			Query<Brief> qbe = new Query<Brief>(Brief.class);
			qbe.add(Brief.FLD_TYPE, Query.EQUALS, Brief.TEMPLATE);
			if (selectedMand != null) {
				qbe.startGroup();
				qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, selectedMand.getId());
				qbe.or();
				qbe.add(Brief.FLD_DESTINATION_ID, Query.EQUALS, StringTool.leer);
				qbe.endGroup();
				qbe.and();
			}
			qbe.add("geloescht", Query.NOT_EQUAL, StringConstants.ONE); //$NON-NLS-1$
			qbe.add(Brief.FLD_SUBJECT, Query.EQUALS, title);
			List<Brief> l = qbe.execute();
			if (l.size() > 0) {
				if (MessageDialog.openQuestion(getShell(),
					Messages.TextContainer_TemplateExistsCaption,
					Messages.TextContainer_TemplateExistsBody)) {
					Brief old = l.get(0);
					old.delete();
				} else {
					return;
				}
			}
			// save value into boolean for access from the outside world
			dontShowAddresseeSelection = checkBoxDontShowAddresseeSelection.getSelection();
			super.okPressed();
		}
		
		/**
		 * get the template brief for this template name
		 * 
		 * @param templateName
		 *            the template name to be tested
		 * @return the brief object or null if not found
		 * @author marlovitsh
		 */
		Brief getBriefForTemplateName(String templateName){
			Query<Brief> qry = new Query<Brief>(Brief.class);
			qry.add(Brief.FLD_SUBJECT, Query.EQUALS, templateName, true);
			qry.add(Brief.FLD_TYPE, Query.EQUALS, Brief.TEMPLATE, true);
			List<Brief> result = qry.execute();
			if (result.size() > 0)
				return result.get(0);
			return null;
		}
		
		/**
		 * check whether this is a system template - has "SYS" in "BehandlungsID"
		 * 
		 * @param templateName
		 *            the template name to be tested
		 * @return true if is a system template, false if not
		 * @author marlovitsh
		 */
		boolean isSystemTemplate(String templateName){
			Query<Brief> qry = new Query<Brief>(Brief.class);
			qry.add(Brief.FLD_SUBJECT, Query.EQUALS, templateName, true);
			qry.add(Brief.FLD_TYPE, Query.EQUALS, Brief.TEMPLATE, true);
			qry.add(Brief.FLD_KONSULTATION_ID, Query.EQUALS, "SYS", true); //$NON-NLS-1$
			List<Brief> result = qry.execute();
			if (result.size() > 0)
				return true;
			return false;
		}
	}
	
	public boolean replace(final String pattern, final ReplaceCallback cb){
		return plugin.findOrReplace(pattern, cb);
	}
	
	public boolean replace(final String pattern, final String repl){
		return plugin.findOrReplace(pattern, new ReplaceCallback() {
			public String replace(final String in){
				return repl;
			}
		});
	}
	
	static class DefaultTextPlugin implements ITextPlugin {
		private static final String expl = Messages.TextContainer_NoPlugin1
			+ Messages.TextContainer_NoPlugin2 + Messages.TextContainer_Noplugin3
			+ Messages.TextContainer_NoPlugin4 + Messages.TextContainer_NoPLugin5;
		
		public Composite createContainer(final Composite parent, final ITextPlugin.ICallback h){
			parent.setLayout(new FillLayout());
			// Composite ret=new Composite(parent,SWT.BORDER);
			Form form = UiDesk.getToolkit().createForm(parent);
			form.setText(Messages.TextContainer_NoPluginCaption);
			form.getBody().setLayout(new FillLayout());
			FormText ft = UiDesk.getToolkit().createFormText(form.getBody(), false);
			ft.setText(expl, true, false);
			return form.getBody();
		}
		
		public void dispose(){}
		
		public void showMenu(final boolean b){}
		
		public void showToolbar(final boolean b){}
		
		public boolean createEmptyDocument(){
			return false;
		}
		
		public boolean loadFromByteArray(final byte[] bs, final boolean asTemplate){
			return false;
		}
		
		public boolean findOrReplace(final String pattern, final ReplaceCallback cb){
			return false;
		}
		
		public byte[] storeToByteArray(){
			return null;
		}
		
		public boolean clear(){
			return false;
		}
		
		public void setInitializationData(final IConfigurationElement config,
			final String propertyName, final Object data) throws CoreException{}
		
		public boolean loadFromStream(final InputStream is, final boolean asTemplate){
			// TODO Automatisch erstellter Methoden-Stub
			return false;
		}
		
		public boolean print(final String printer, final String tray,
			final boolean waitUntilFinished){
			return false;
		}
		
		public boolean insertTable(final String marke, final int props, final String[][] contents,
			final int[] columnSizes){
			return false;
		}
		
		public void setFocus(){

		}
		
		public PageFormat getFormat(){
			return PageFormat.USER;
		}
		
		public void setFormat(final PageFormat f){

		}
		
		public Object insertTextAt(final int x, final int y, final int w, final int h,
			final String text, final int adjust){
			return null;
		}
		
		public boolean setFont(final String name, final int style, final float size){
			return false;
		}
		
		public boolean setStyle(final int style){
			return false;
		}
		
		public Object insertText(final String marke, final String text, final int adjust){
			// TODO Auto-generated method stub
			return null;
		}
		
		public Object insertText(final Object pos, final String text, final int adjust){
			// TODO Auto-generated method stub
			return null;
		}
		
		public String getMimeType(){
			return "text/nothing"; //$NON-NLS-1$
		}
		
		public void setSaveOnFocusLost(final boolean bSave){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean isDirectOutput(){
			return false;
		}
		
		@Override
		public void setParameter(Parameter parameter){
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void initTemplatePrintSettings(String template){
			// TODO Auto-generated method stub
		}
		
	}
	
}
