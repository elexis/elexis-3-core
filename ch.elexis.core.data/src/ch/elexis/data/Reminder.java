/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.data;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;

/**
 * Ein Reminder ist eine Erinnerung an etwas. Ein Reminder ist an einen Kontakt gebunden. Ein
 * Reminder hat ein Fälligkeitsdatum und einen Status Es gibt mehrere Typen von Remindern:
 * <ul>
 * <li>Nachricht: Es erscheint am und nach dem Fälligkeitsdatum eine Nachricht auf dem Bildschirm,
 * die den Reminder anzeigt. Dies solange, bis der status auf "erledigt" oder "unerledigt" gesetzt
 * wird.</li>
 * <li>Brief: Es wird am Fälligkeitsdatum ein Brief mit gegebener Vorlage zum angegebenen Kontakt
 * erstellt.</li>
 * </ul>
 * 
 * @author Gerry
 * 
 */
public class Reminder extends PersistentObject implements Comparable<Reminder> {
	
	public static final String MESSAGE = "Message";
	public static final String RESPONSIBLE = "Responsible";
	public static final String TYPE = "Typ";
	public static final String STATUS = "Status";
	public static final String DUE = "Due";
	public static final String CREATOR = "Creator";
	public static final String KONTAKT_ID = "IdentID";
	static final String TABLENAME = "REMINDERS";
	
	public static final String STATE_PLANNED = "geplant";
	public static final String STATE_DUE = "fällig";
	public static final String STATE_OVERDUE = "überfällig";
	public static final String DONE = "erledigt";
	public static final String UNDONE = "unerledigt";
	
	public enum LabelFields {
		PAT_ID("PatientNr"), FIRSTNAME("Vorname"), LASTNAME("Name");
		
		private final String text;
		
		private LabelFields(final String text){
			this.text = text;
		}
		
		@Override
		public String toString(){
			return text;
		}
		
		public String getKontaktEquivalent(){
			if (text.equals(FIRSTNAME.toString())) {
				return Kontakt.FLD_NAME2;
			} else if (text.equals(LASTNAME.toString())) {
				return Kontakt.FLD_NAME1;
			} else {
				return "";
			}
		}
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	static {
		addMapping(TABLENAME, KONTAKT_ID, "Creator=OriginID", "Due=S:D:DateDue", STATUS, TYPE,
			"Params", MESSAGE, RESPONSIBLE,
			"Responsibles=JOINT:ResponsibleID:ReminderID:REMINDERS_RESPONSIBLE_LINK");
	}
	
	public enum Typ {
		anzeigeTodoPat, anzeigeTodoAll, anzeigeOeffnen, anzeigeProgstart, brief
	}
	
	public static final String[] TypText = {
		"Anzeige nur beim Patienten", "Immer in Pendenzen anzeigen",
		"Popup beim Auswählen des Patienten", "Popup beim Einloggen", "Brief erstellen"
	};
	
	public enum Status {
		STATE_PLANNED, STATE_DUE, STATE_OVERDUE, STATE_DONE, STATE_UNDONE
	}
	
	Reminder(){/* leer */}
	
	private Reminder(final String id){
		super(id);
	}
	
	/**
	 * Create a new Reminder. Note: by default, the new reminder will have no responsible. @see
	 * addResponsible
	 * 
	 * @param ident
	 *            The contact (i.e. Patient) the reminder belongs to. If ident is null, the reminder
	 *            will be attributed to the corrent user
	 * @param due
	 *            A date string
	 * @param typ
	 *            type of the reminder (@see enum Typ)
	 * @param params
	 *            parameters depending on the type of the reminder
	 * @param msg
	 *            Text for the reminder
	 * 
	 */
	public Reminder(Kontakt ident, final String due, final Typ typ, final String params,
		final String msg){
		create(null);
		if (ident == null) {
			ident = CoreHub.actUser;
		}
		set(new String[] {
			KONTAKT_ID, CREATOR, DUE, STATUS, TYPE, "Params", MESSAGE
		},
			new String[] {
				ident.getId(), CoreHub.actUser.getId(), due,
				Byte.toString((byte) Status.STATE_PLANNED.ordinal()),
				Byte.toString((byte) typ.ordinal()), params, msg
			});
	}
	
	/**
	 * Add a new user to the list of responsibles for that reminder, if not already in list. The
	 * reminder will show up among the reminders, if one of its responsibles is logged in.
	 * 
	 * @param a
	 *            the user to add to the list of responsible users
	 */
	public void addResponsible(final Anwender a){
		for (Anwender anwender : getResponsibles()) {
			if (anwender.getId().equalsIgnoreCase(a.getId()))
				return;
		}
		addToList("Responsibles", a.getId(), (String[]) null);
	}
	
	/**
	 * Removes a user from the list of responsibles for that reminder, if the user is in the list.
	 * If the user is not in the list, nothing is done.
	 * 
	 * @param a
	 *            the user to remove from the list of responsible users
	 */
	public void removeResponsible(final Anwender a){
		for (Anwender anwender : getResponsibles()) {
			if (anwender.getId().equalsIgnoreCase(a.getId()))
				removeFromList("Responsibles", a.getId());
		}
	}
	
	/** Einen Reminder anhand seiner ID aus der Datenbank einlesen */
	public static Reminder load(final String id){
		return new Reminder(id);
	}
	
	@Override
	public String getLabel(){
		Kontakt k = Kontakt.load(get(KONTAKT_ID));
		
		StringBuilder sb = new StringBuilder();
		sb.append(get(DUE)).append(" (").append(getConfiguredKontaktLabel(k)).append("): ")
			.append(get(MESSAGE));
		return sb.toString();
	}
	
	private String getConfiguredKontaktLabel(Kontakt k){
		String[] configLabel =
			CoreHub.userCfg.get(Preferences.USR_REMINDER_PAT_LABEL_CHOOSEN,
				LabelFields.LASTNAME.toString()).split(",");
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < configLabel.length; i++) {
			sb.append(k.get(configLabel[i]));
			
			if (i != configLabel.length - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	public Typ getTyp(){
		String t = get(TYPE);
		if (StringTool.isNothing(t)) {
			t = "1";
		}
		Typ ret = Typ.values()[Byte.parseByte(t)];
		return ret;
	}
	
	public Status getStatus(){
		String t = get(STATUS);
		if (StringTool.isNothing(t)) {
			t = "0";
		}
		Status ret = Status.values()[Byte.parseByte(t)];
		if (ret == Status.STATE_PLANNED) {
			TimeTool now = new TimeTool();
			now.chop(3);
			TimeTool mine = getDateDue();
			if (now.isEqual(mine)) {
				return Status.STATE_DUE;
			}
			if (now.isAfter(mine)) {
				return Status.STATE_OVERDUE;
			}
		}
		return ret;
	}
	
	public String getMessage(){
		return checkNull(get(MESSAGE));
	}
	
	public void setStatus(final Status s){
		set(STATUS, Byte.toString((byte) s.ordinal()));
	}
	
	public TimeTool getDateDue(){
		TimeTool ret = new TimeTool(get(DUE));
		ret.chop(3);
		return ret;
	}
	
	public boolean isDue(){
		TimeTool now = new TimeTool();
		TimeTool mine = getDateDue();
		if (mine.isEqual(now)) {
			return true;
		}
		return false;
	}
	
	public boolean isOverdue(){
		TimeTool now = new TimeTool();
		TimeTool mine = getDateDue();
		if (mine.isBefore(now)) {
			return true;
		}
		return false;
	}
	
	public List<Anwender> getResponsibles(){
		List<String[]> lResp = getList("Responsibles", new String[0]);
		ArrayList<Anwender> ret = new ArrayList<Anwender>(lResp.size());
		for (String[] r : lResp) {
			ret.add(Anwender.load(r[0]));
		}
		return ret;
	}
	
	public Anwender getCreator(){
		return Anwender.load(checkNull(get(CREATOR)));
	}
	
	/**
	 * Alle heute (oder vor heute) fälligen Reminder holen
	 * 
	 * @return eine Liste aller fälligen Reminder
	 */
	public static List<Reminder> findForToday(){
		Query<Reminder> qbe = new Query<Reminder>(Reminder.class);
		qbe.add(DUE, Query.LESS_OR_EQUAL, new TimeTool().toString(TimeTool.DATE_COMPACT));
		qbe.add(STATUS, Query.NOT_EQUAL, Integer.toString(Status.STATE_DONE.ordinal()));
		List<Reminder> ret = qbe.execute();
		return ret;
	}
	
	/**
	 * Alle Reminder zu einem Patienten holen
	 * 
	 * @param p
	 *            der Patient
	 * @param responsible
	 *            der Verantwortliche oder null: Alle
	 * @return eine Liste aller offenen Reminder dieses Patienten
	 */
	public static List<Reminder> findForPatient(final Patient p, final Kontakt responsible){
		Query<Reminder> qbe = new Query<Reminder>(Reminder.class);
		qbe.add(KONTAKT_ID, Query.EQUALS, p.getId());
		qbe.add(STATUS, Query.NOT_EQUAL, Integer.toString(Status.STATE_DONE.ordinal()));
		qbe.add(DUE, Query.LESS_OR_EQUAL, new TimeTool().toString(TimeTool.DATE_COMPACT));
		if (responsible != null) {
			qbe.startGroup();
			qbe.add(RESPONSIBLE, Query.EQUALS, responsible.getId());
			qbe.or();
			qbe.add(RESPONSIBLE, StringTool.leer, null);
			qbe.endGroup();
		}
		return qbe.execute();
	}
	
	/**
	 * Alle Reminder holen, die beim Progammstart gezeigt werden sollen
	 * 
	 * @return
	 */
	public static List<Reminder> findToShowOnStartup(final Anwender a){
		Query<Reminder> qbe = new Query<Reminder>(Reminder.class);
		qbe.add(DUE, Query.LESS_OR_EQUAL, new TimeTool().toString(TimeTool.DATE_COMPACT));
		qbe.add(STATUS, Query.NOT_EQUAL, Integer.toString(Status.STATE_DONE.ordinal()));
		qbe.add(TYPE, Query.EQUALS, Integer.toString(Typ.anzeigeProgstart.ordinal()));
		return qbe.execute();
	}
	
	/**
	 * Alle Reminder holen, die bei einem bestimmten Patienten für einen bestimmten Anwender fällig
	 * sind
	 * 
	 * @param p
	 *            der Patient
	 * @param a
	 *            der Anwender
	 * @param bOnlyPopup
	 *            nur die zeigen, die den Typ "Bei Auswahl popup" haben.
	 * @return eine Liste der fälligen Reminder dieses Patienten
	 */
	public static List<Reminder> findRemindersDueFor(final Patient p, final Anwender a,
		final boolean bOnlyPopup){
		if (a == null) {
			return new ArrayList<Reminder>();
		}
		final SortedSet<Reminder> r4a = a.getReminders(p);
		List<Reminder> ret = new ArrayList<Reminder>(r4a.size());
		TimeTool today = new TimeTool();
		for (Reminder r : r4a) {
			if (r.getDateDue().isAfter(today)) {
				continue;
			}
			if (r.getStatus() == Status.STATE_DONE) {
				continue;
			}
			if ((bOnlyPopup == true) && (r.getTyp() != Typ.anzeigeOeffnen)) {
				continue;
			}
			ret.add(r);
		}
		
		return ret;
	}
	
	public Patient getKontakt(){
		Patient ret = Patient.load(get(KONTAKT_ID));
		if (ret.exists()) {
			return ret;
		}
		return null;
	}
	
	/**
	 * The comparator is used when reminders are inserted chronologically in a sorted set. To allow
	 * multiple different reminders at the same day, we use the id to differentiate reminders with
	 * identical dates.
	 */
	public int compareTo(final Reminder r){
		int i = getDateDue().compareTo(r.getDateDue());
		if (i == 0) {
			return getId().compareTo(r.getId());
		} else {
			return i;
		}
		
	}
	
	@Override
	public boolean delete(){
		getConnection().exec(
			"DELETE FROM REMINDERS_RESPONSIBLE_LINK WHERE ReminderID=" + getWrappedId());
		return super.delete();
	}
	
}
