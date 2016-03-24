/*******************************************************************************
 * Copyright (c) 2005-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - #2112
 *******************************************************************************/
package ch.elexis.data;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.elexis.core.data.lock.LocalLockService;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.model.RoleConstants;
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
	public static final String FLD_JOINT_REMINDERS = "Reminders";
	public static final String FLD_EXTINFO_MANDATORS = "Mandant";
	
	static {
		addMapping(Kontakt.TABLENAME, FLD_EXTINFO, Kontakt.FLD_IS_USER,
			FLD_LABEL + "=Bezeichnung3", FLD_JOINT_REMINDERS
				+ "=JOINT:ReminderID:ResponsibleID:REMINDERS_RESPONSIBLE_LINK");
	}
	
	public Anwender(String Username, String Password){
		this(Username, Password, false);	
	}
	
	/**
	 * 
	 * @param username
	 * @param password
	 * @param isExecutiveDoctor
	 *            additionally assign the {@link Role#SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR}
	 * @since 3.1
	 */
	public Anwender(String username, String password, boolean isExecutiveDoctor){
		create(null);
		super.setConstraint();
		
		User user = new User(this, username, password);
		if (isExecutiveDoctor)
			user.setAssignedRole(Role.load(RoleConstants.SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR), true);
	}
	
	public Anwender(final String Name, final String Vorname, final String Geburtsdatum,
		final String s){
		super(Name, Vorname, Geburtsdatum, s);
	}
	
	public static Anwender load(final String id){
		Anwender ret = new Anwender(id);
		if (ret.state() > PersistentObject.INVALID_ID) {
			return ret;
		}
		return null;
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
	
	/**
	 * Get Reminders for this user, related to a specific Kontakt
	 * 
	 * @param k
	 *            related kontakt or null: all Reminders
	 * @return a List sorted by date
	 */
	public SortedSet<Reminder> getReminders(final Kontakt k){
		TreeSet<Reminder> ret = new TreeSet<Reminder>();
		List<String[]> rem = getList(FLD_JOINT_REMINDERS, (String[]) null);
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
	
	/**
	 * 
	 * @return
	 * @since 3.1
	 */
	public @NonNull List<Mandant> getExecutiveDoctorsWorkingFor(){
		List<Mandant> mandantenList = CoreHub.getMandantenList();
		String mandators = (String) getExtInfoStoredObjectByKey(FLD_EXTINFO_MANDATORS);
		if (mandators == null)
			return Collections.emptyList();
		
		List<String> man = Arrays.asList(mandators.split(","));
		return mandantenList.stream().filter(p -> man.contains(p.getLabel()))
			.collect(Collectors.toList());
	}
	
	/**
	 * 
	 * @param m
	 * @param checked
	 *            <code>true</code> add or <code>false</code> remove
	 * @since 3.1
	 */
	public void addOrRemoveExecutiveDoctorWorkingFor(Mandant m, boolean checked){
		HashSet<Mandant> hashSet = new HashSet<Mandant>(getExecutiveDoctorsWorkingFor());
		if (checked) {
			hashSet.add(m);
		} else {
			hashSet.remove(m);
		}
		List<String> edList = hashSet.stream().map(p -> p.getLabel()).collect(Collectors.toList());
		setExtInfoStoredObjectByKey(FLD_EXTINFO_MANDATORS, ts(edList));
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
	 * Initializes the first user to the system, the administrator
	 */
	protected static void initializeAdministratorUser(){
		new User();
		
		Anwender admin = new Anwender();
		admin.create(null);
		admin.set(new String[] {
			Person.NAME, FLD_LABEL, Kontakt.FLD_IS_USER
		}, ADMINISTRATOR, ADMINISTRATOR, StringConstants.ONE);
		User.load(ADMINISTRATOR).setAssignedContact(admin);
		
		CoreHub.actUser = admin;
		ElexisEventDispatcher.getInstance().fire(
			new ElexisEvent(admin, Anwender.class, ElexisEvent.EVENT_USER_CHANGED));
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
	 * @since 3.1 queries {@link User}
	 */
	public static boolean login(final String username, final String password){
		((LocalLockService) CoreHub.getLocalLockService()).reconfigure();
		CoreHub.logoffAnwender();
		
		// check if user exists
		User user = User.load(username);
		if (user == null)
			return false;
		
		// is the user currently active, or locked?
		if (!user.isActive())
			return false;
		
		// check if password is valid
		boolean result = user.verifyPassword(password);
		if (!result)
			return false;
		
		// set user in system
		ElexisEventDispatcher.getInstance().fire(
			new ElexisEvent(user, User.class, ElexisEvent.EVENT_SELECTED));
		CoreHub.actUser = Anwender.load(user.getAssignedContactId());
		ElexisEventDispatcher.getInstance().fire(
			new ElexisEvent(CoreHub.actUser, Anwender.class, ElexisEvent.EVENT_USER_CHANGED));
		
		cod.adaptForUser();
		
		CoreHub.actUser.setInitialMandator();
		
		CoreHub.userCfg =
			new SqlSettings(getConnection(), "USERCONFIG", "Param", "Value", "UserID="
				+ CoreHub.actUser.getWrappedId());
		
		CoreHub.heart.resume(true);
		
		return true;
	}
	
	/**
	 * @since 3.1
	 */
	public static void logoff() {
		CoreHub.logoffAnwender();
	}
	
	private void setInitialMandator(){
		String mandantLabel = (String) getExtInfoStoredObjectByKey(FLD_EXTINFO_MANDATORS);
		String MandantID = null;
		if (mandantLabel!=null && mandantLabel.length()>0) {
			mandantLabel = mandantLabel.split(",")[0];
			for (Mandant m : CoreHub.getMandantenList()) {
				if (m.getLabel().equals(mandantLabel)) {
					MandantID = m.getId();
					break;
				}
			}
		}
		if (MandantID != null) {
			CoreHub.setMandant(Mandant.load(MandantID));
		} else {
			Mandant m = Mandant.load(CoreHub.actUser.getId());
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
	}

	/**
	 * 
	 * @return <code>true</code> if {@link Anwender} is also {@link Mandant}
	 * @since 3.1
	 */
	public boolean isExecutiveDoctor(){
		Mandant m = Mandant.load(getId());
		return (m.exists() && m.isValid());
	}
	
	/**
	 * 
	 * @param value <code>true</code> to define as {@link Mandant}, else <code>false</code>
	 * @since 3.1
	 */
	public void setExecutiveDoctor(boolean value) {
		set(FLD_IS_MANDATOR, ts(value));
	}
}
