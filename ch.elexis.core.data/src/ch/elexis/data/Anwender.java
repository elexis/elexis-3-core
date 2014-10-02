/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/
package ch.elexis.data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.elexis.admin.ACE;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.exceptions.PersistenceException;
import ch.rgw.io.SqlSettings;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.StringTool;

/**
 * Ein Anwender ist eine Person (und damit auch ein Kontakt), die zusätzlich das Recht hat, diese
 * Software zu benützen. Ein Anwender hat Username und Passwort, sowie ein AgendaLabel. Jeder
 * Anwender gehört zu mindestens einer Gruppe.
 * 
 * Diese Klasse enthält ausserdem die statische Methode "login", mit der ein Anwender sich anmelden
 * kann.
 * 
 * @author Gerry
 * 
 */
public class Anwender extends Person {
	
	public static final String ADMINISTRATOR = "Administrator";
	public static final String FLD_LABEL = "Label"; // contains username
	
	public static final String FLD_EXTINFO_PASSWORD = "UsrPwd";
	public static final String FLD_EXTINFO_GROUPS = "Groups";
	
	static {
		addMapping(Kontakt.TABLENAME, FLD_EXTINFO, Kontakt.FLD_IS_USER,
			FLD_LABEL + "=Bezeichnung3",
			"Reminders=JOINT:ReminderID:ResponsibleID:REMINDERS_RESPONSIBLE_LINK");
	}
	
	public Anwender(final String Username, final String Password){
		create(null);
		set(new String[] {
			Person.NAME
		}, Username);
		setLabel(Username);
		setPwd(Password);
		setInfoElement(FLD_EXTINFO_GROUPS, "Anwender");
		super.setConstraint();
	}
	
	public Anwender(final String Name, final String Vorname, final String Geburtsdatum,
		final String s){
		super(Name, Vorname, Geburtsdatum, s);
	}
	
	/**
	 * Check if this Anwender is valid.
	 * <p>
	 * We check wheter the object exists in the database and whether the login name ("Label") is
	 * available.
	 * </p>
	 */
	@Override
	public boolean isValid(){
		String label = get(FLD_LABEL);
		if (StringTool.isNothing(label)) {
			return false;
		}
		if (label.equals(ADMINISTRATOR)) {
			return true; // Admin is always valid
		}
		return super.isValid();
	}
	
	/**
	 * Return a short or long label for this Anwender
	 * 
	 * This implementation returns the "Label" field for both label types
	 * 
	 * @return a label describing this Person
	 */
	@Override
	public String getLabel(final boolean shortLabel){
		String l = get(FLD_LABEL);
		if (StringTool.isNothing(l)) {
			l = checkNull(get(Person.NAME)) + StringTool.space + checkNull(get(Person.FIRSTNAME));
			if (StringTool.isNothing(l)) {
				l = "unbekannt";
			}
		}
		return l;
	}
	
	/**
	 * Kurzname setzen. Zuerst prüfen, ob es wirklich ein neuer Name ist, um unnötigen
	 * Netzwerkverkehr zu vermeiden
	 */
	public void setLabel(final String label){
		String oldlabel = getLabel();
		if (!label.equals(oldlabel)) {
			set(FLD_LABEL, label);
		}
	}
	
	/** Passwort setzen */
	public void setPwd(final String pwd){
		setInfoElement(FLD_EXTINFO_PASSWORD, pwd);
	}
	
	/**
	 * @since 3.0.0
	 */
	public String getPwd(){
		return (String) getInfoElement(FLD_EXTINFO_PASSWORD);
	}
	
	/**
	 * Get Reminders for this user, related to a specific Kontakt
	 * 
	 * @param k
	 *            related kontakt or null: all Reminders
	 * @return a List sorted by date
	 */
	public SortedSet<Reminder> getReminders(final Kontakt k){
		TreeSet<Reminder> ret = new TreeSet<Reminder>();
		List<String[]> rem = getList("Reminders", (String[]) null);
		if (rem != null) {
			String kid = k == null ? null : k.getId();
			for (String[] l : rem) {
				Reminder r = Reminder.load(l[0]);
				if (kid != null) {
					if (!r.get("IdentID").equals(kid)) {
						continue;
					}
				}
				ret.add(r);
			}
		}
		return ret;
	}
	
	public static Anwender load(final String id){
		Anwender ret = new Anwender(id);
		if (ret.state() > PersistentObject.INVALID_ID) {
			return ret;
		}
		return null;
	}
	
	@Override
	protected String getConstraint(){
		return Kontakt.FLD_IS_USER + StringTool.equals + JdbcLink.wrap(StringConstants.ONE);
	}
	
	@Override
	protected void setConstraint(){
		set(Kontakt.FLD_IS_USER, StringConstants.ONE);
	}
	
	protected Anwender(){/* leer */
	}
	
	protected Anwender(final String id){
		super(id);
	}
	
	/**
	 * Den ersten Benutzer anlegen und initiale Zugriffsrechte setzen Wird von PersistentObject()
	 * aufgerufen, wenn die Datenbank neu angelegt wurde.
	 */
	@SuppressWarnings("unchecked")
	protected static void init(){
		// Administrator muss "zu fuss" erstellt werden, da noch keine
		// Rechteverwaltung vorhanden ist
		Anwender admin = new Anwender();
		admin.create(null);
		admin.set(new String[] {
			Person.NAME, FLD_LABEL, Kontakt.FLD_IS_USER
		}, ADMINISTRATOR, ADMINISTRATOR, StringConstants.ONE);
		CoreHub.actUser = admin;
		CoreHub.acl.grant(admin, new ACE(ACE.ACE_IMPLICIT, "WriteInfoStore"), new ACE(
			ACE.ACE_IMPLICIT, "LoadInfoStore"), new ACE(ACE.ACE_IMPLICIT, "WriteGroups"), new ACE(
			ACE.ACE_IMPLICIT, "ReadGroups"));
		
		admin.setExtInfoStoredObjectByKey(FLD_EXTINFO_PASSWORD, "admin");
		admin.setExtInfoStoredObjectByKey(FLD_EXTINFO_GROUPS, "Admin,Anwender");

		CoreHub.acl.grant("Admin", new ACE(ACE.ACE_IMPLICIT, "ReadUsrPwd"), new ACE(
			ACE.ACE_IMPLICIT, "WriteUsrPwd"), new ACE(ACE.ACE_IMPLICIT, "CreateAndDelete"),
			new ACE(ACE.ACE_IMPLICIT, "WriteGroups"));
		CoreHub.acl.grant("System", new ACE(ACE.ACE_IMPLICIT, "ReadUsrPwd"));
		
	}
	
	/**
	 * Login: Anwender anmelden, passenden Mandanten anmelden. (Jeder Anwender ist entweder selber
	 * ein Mandant oder ist einem Mandanten zugeordnet)
	 * 
	 * @param username
	 *            Kurzname
	 * @param password
	 *            Passwort
	 * @return <code>true</code> erfolgreich angemeldet, CoreHub.actUser gesetzt, else
	 *         <code>false</code>
	 */
	@SuppressWarnings("unchecked")
	public static boolean login(final String username, final String password){
		logoff();
		CoreHub.actUser = null;
		cod.adaptForUser();
		Query<Anwender> qbe = new Query<Anwender>(Anwender.class);
		qbe.add(FLD_LABEL, StringTool.equals, username);
		List<Anwender> list = qbe.execute();
		if ((list == null) || (list.size() < 1)) {
			return false;
		}
		Anwender a = list.get(0);
		Map<Object, Object> km = a.getMap(FLD_EXTINFO);
		if (km == null) {
			MessageEvent.fireLoggedError("Interner Fehler",
				"Die Datenstruktur ExtInfo von " + a.getLabel() + " ist beschädigt.");
			try {
				a.setMap("ExtInfo", new HashMap<Object, Object>());
			} catch (PersistenceException e) {
				MessageEvent.fireLoggedError("Fatal error", "Can't store user map", e);
			}
		}
		String pwd = (String) km.get(FLD_EXTINFO_PASSWORD);
		if (pwd == null) {
			return false;
		}
		if (pwd.equals(password)) {
			CoreHub.actUser = a;
			String MandantLabel = (String) km.get("Mandant");
			String MandantID = null;
			if (!StringTool.isNothing(MandantLabel)) {
				MandantLabel = MandantLabel.split(",")[0];
				for (Mandant m : CoreHub.getMandantenList()) {
					if (m.getLabel().equals(MandantLabel)) {
						MandantID = m.getId();
						break;
					}
				}
			}
			if (MandantID != null) {
				CoreHub.setMandant(Mandant.load(MandantID));
			} else {
				Mandant m = Mandant.load(a.getId());
				if ((m != null) && m.isValid()) {
					CoreHub.setMandant(m);
				} else {
					List<Mandant> ml = new Query<Mandant>(Mandant.class).execute();
					if ((ml != null) && (ml.size() > 0)) {
						m = ml.get(0);
						CoreHub.setMandant(m);
						
					} else {
						MessageEvent
							.fireError("Kein Mandant definiert",
								"Sie können Elexis erst normal benutzen, wenn Sie mindestens einen Mandanten definiert haben");
					}
				}
			}
			
			CoreHub.userCfg =
				new SqlSettings(getConnection(), "USERCONFIG", "Param", "Value", "UserID="
					+ a.getWrappedId());
			
			cod.adaptForUser();
			CoreHub.heart.resume(true);
			return true;
		}
		return false;
	}
	
	public static void logoff(){
		if (CoreHub.userCfg != null) {
			CoreHub.userCfg.flush();
		}
		CoreHub.setMandant(null);
		CoreHub.heart.suspend();
		CoreHub.actUser = null;
		ElexisEventDispatcher.getInstance().fire(
			new ElexisEvent(CoreHub.actUser, Anwender.class, ElexisEvent.EVENT_USER_CHANGED));
		CoreHub.userCfg = CoreHub.localCfg;
	}
}
