/*******************************************************************************
 * Copyright (c) 2005-2016, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    MEDEVIT <office@medevit.at>
 *******************************************************************************/

package ch.elexis.data;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import ch.rgw.tools.ExHandler;
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
 * @since 3.2 major refactorings
 */
public class Reminder extends PersistentObject implements Comparable<Reminder> {
	
	public static final String TABLENAME = "REMINDERS";
	
	public static final String MESSAGE = "Message";
	public static final String RESPONSIBLE = "Responsible";
	public static final String FLD_VISIBILITY = "Typ";
	public static final String FLD_STATUS = "Status";
	public static final String DUE = "Due";
	public static final String CREATOR = "Creator";
	public static final String KONTAKT_ID = "IdentID";
	public static final String FLD_PRIORITY = "priority";
	public static final String FLD_ACTION_TYPE = "actionType";
	public static final String FLD_SUBJECT = "subject";
	public static final String FLD_PARAMS = "Params";
	public static final String FLD_JOINT_RESPONSIBLES = "Responsibles";
	
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
		addMapping(TABLENAME, KONTAKT_ID, CREATOR + "=OriginID", DUE + "=S:D:DateDue", FLD_STATUS,
			FLD_VISIBILITY, FLD_PARAMS, MESSAGE, RESPONSIBLE,
			FLD_JOINT_RESPONSIBLES + "=JOINT:ResponsibleID:ReminderID:REMINDERS_RESPONSIBLE_LINK",
			FLD_PRIORITY, FLD_ACTION_TYPE, FLD_SUBJECT);
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
	 *            The contact (i.e. Patient) the reminder belongs to. If <code>null</code>, the
	 *            reminder will be attributed to the current user, making it a non-patient-related
	 *            reminder.
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
	public Reminder(Kontakt ident, final String due, final Visibility visibility,
		final String params, final String msg){
		create(null);
		if (ident == null) {
			ident = CoreHub.actUser;
		}
		set(new String[] {
			KONTAKT_ID, CREATOR, DUE, FLD_STATUS, FLD_VISIBILITY, FLD_PARAMS, MESSAGE
		}, new String[] {
			ident.getId(), CoreHub.actUser.getId(), due,
			Byte.toString((byte) Visibility.ALWAYS.numericValue()),
			Byte.toString((byte) visibility.numericValue()), params, msg
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
		addToList(FLD_JOINT_RESPONSIBLES, a.getId(), (String[]) null);
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
				removeFromList(FLD_JOINT_RESPONSIBLES, a.getId());
		}
	}
	
	/** Einen Reminder anhand seiner ID aus der Datenbank einlesen */
	public static Reminder load(final String id){
		return new Reminder(id);
	}
	
	@Override
	public String getLabel(){
		String[] vals = get(true, KONTAKT_ID, DUE, MESSAGE, FLD_SUBJECT);
		Kontakt k = Kontakt.load(vals[0]);
		if (vals[3] != null && vals[3].length() > 1) {
			return vals[1] + " (" + getConfiguredKontaktLabel(k) + "): " + vals[3];
		} else {
			return vals[1] + " (" + getConfiguredKontaktLabel(k) + "): " + vals[2];
		}
	}
	
	private String getConfiguredKontaktLabel(Kontakt k){
		String[] configLabel = CoreHub.userCfg
			.get(Preferences.USR_REMINDER_PAT_LABEL_CHOOSEN, LabelFields.LASTNAME.toString())
			.split(",");
		
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < configLabel.length; i++) {
			sb.append(k.get(configLabel[i]));
			
			if (i != configLabel.length - 1) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	public static ProcessStatus determineCurrentStatus(ProcessStatus givenStatus, TimeTool dueDate){
		if (ProcessStatus.CLOSED == givenStatus || ProcessStatus.ON_HOLD == givenStatus) {
			return givenStatus;
		}
		
		TimeTool now = new TimeTool();
		now.chop(3);
		if (now.isEqual(dueDate)) {
			return ProcessStatus.DUE;
		}
		if (now.isAfter(dueDate)) {
			return ProcessStatus.OVERDUE;
		}
		
		return givenStatus;
	}
	
	public ProcessStatus getStatus(){
		ProcessStatus ps = ProcessStatus.byNumericSafe(get(FLD_STATUS));
		return Reminder.determineCurrentStatus(ps, getDateDue());
	}
	
	public String getMessage(){
		return checkNull(get(MESSAGE));
	}
	
	public void setStatus(ProcessStatus s){
		set(FLD_STATUS, Byte.toString((byte) s.ordinal()));
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
		List<String[]> lResp = getList(FLD_JOINT_RESPONSIBLES, new String[0]);
		ArrayList<Anwender> ret = new ArrayList<Anwender>(lResp.size());
		for (String[] r : lResp) {
			ret.add(Anwender.load(r[0]));
		}
		return ret;
	}
	
	public Anwender getCreator(){
		return Anwender.load(checkNull(get(CREATOR)));
	}
	
	private static String PS_REMINDERS_RESPONSIBLE =
		"SELECT r.ID FROM reminders r LEFT JOIN reminders_responsible_link rrl ON (r.id = rrl.ReminderId) WHERE rrl.ResponsibleID = ? AND r.deleted = '0'";
	
	public static List<Reminder> findAllUserIsResponsibleFor(Anwender anwender,
		boolean showOnlyDueReminders){
		Set<Reminder> ret = new HashSet<Reminder>();
		// we have to apply a set, as there may exist
		// multiple equivalent entries in reminders_responsible_link
		// which resolve to multiple occurences of the same element, due to the left join
		DBConnection dbConnection = getDefaultConnection();
		PreparedStatement ps;
		if (showOnlyDueReminders) {
			ps = dbConnection.getPreparedStatement(PS_REMINDERS_RESPONSIBLE + " AND r.DateDue < ?");
		} else {
			ps = dbConnection.getPreparedStatement(PS_REMINDERS_RESPONSIBLE);
		}
		try {
			ps.setString(1, anwender.getId());
			if (showOnlyDueReminders) {
				ps.setString(2, new TimeTool().toString(TimeTool.DATE_COMPACT));
			}
			ResultSet res = ps.executeQuery();
			while (res.next()) {
				Reminder reminder = Reminder.load(res.getString(1));
				reminder.setDBConnection(dbConnection);
				ret.add(reminder);
			}
			res.close();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return new ArrayList<Reminder>(ret);
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				// ignore
			}
			dbConnection.releasePreparedStatement(ps);
		}
		return new ArrayList<Reminder>(ret);
	}
	
	/**
	 * Alle heute (oder vor heute) fälligen Reminder holen
	 * 
	 * @return eine Liste aller fälligen Reminder
	 */
	public static List<Reminder> findForToday(){
		Query<Reminder> qbe = new Query<Reminder>(Reminder.class);
		qbe.add(DUE, Query.LESS_OR_EQUAL, new TimeTool().toString(TimeTool.DATE_COMPACT));
		qbe.add(FLD_STATUS, Query.NOT_EQUAL, Integer.toString(ProcessStatus.CLOSED.numericValue()));
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
		qbe.add(FLD_STATUS, Query.NOT_EQUAL, Integer.toString(ProcessStatus.CLOSED.numericValue()));
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
		qbe.add(FLD_STATUS, Query.NOT_EQUAL, Integer.toString(ProcessStatus.CLOSED.numericValue()));
		qbe.add(FLD_VISIBILITY, Query.EQUALS,
			Integer.toString(Visibility.POPUP_ON_LOGIN.numericValue()));
		return qbe.execute();
	}
	
	private static String PS_REMINDERS_BASE =
		"SELECT r.ID FROM reminders r LEFT JOIN reminders_responsible_link rrl ON (r.id = rrl.ReminderId) WHERE rrl.ResponsibleID = ? AND r.deleted = '0' AND r.Status != '3'";
	
	/**
	 * Retrieve all reminders the given {@link Anwender} is responsible for. The select can be
	 * limited by providing additional criteria
	 * 
	 * @param anwender
	 *            if <code>null</code> allocates the current user
	 * @param onlyDue
	 *            limit the selection to reminders that are already due
	 * @param patient
	 *            limit the selection to reminders concerning a specific {@link Patient}
	 * @param onlyPopup
	 *            do return only reminders with {@link Visibility#POPUP_ON_PATIENT_SELECTION}
	 * @return
	 * @since 3.1
	 */
	public static List<Reminder> findOpenRemindersResponsibleFor(@NonNull Anwender anwender,
		final boolean onlyDue, final Patient patient, final boolean onlyPopup){
		if (anwender == null) {
			anwender = CoreHub.actUser;
		}
		Set<Reminder> ret = new HashSet<Reminder>();
		// we have to apply a set, as there may exist
		// multiple equivalent entries in reminders_responsible_link
		// which resolve to multiple occurences of the same element, due to the left join
		DBConnection dbConnection = getDefaultConnection();
		StringBuilder query = new StringBuilder(PS_REMINDERS_BASE);
		if (onlyDue) {
			query.append(" AND r.DateDue < " + new TimeTool().toString(TimeTool.DATE_COMPACT));
		}
		if (patient != null) {
			query.append(" AND r.IdentID = " + patient.getWrappedId());
		}
		
		PreparedStatement ps = dbConnection.getPreparedStatement(query.toString());
		try {
			ps.setString(1, anwender.getId());
			ResultSet res = ps.executeQuery();
			while (res.next()) {
				Reminder reminder = Reminder.load(res.getString(1));
				reminder.setDBConnection(dbConnection);
				if (onlyPopup
					&& (reminder.getVisibility() != Visibility.POPUP_ON_PATIENT_SELECTION)) {
					continue;
				}
				ret.add(reminder);
			}
			res.close();
		} catch (Exception ex) {
			ExHandler.handle(ex);
			return new ArrayList<Reminder>(ret);
		} finally {
			try {
				ps.close();
			} catch (SQLException e) {
				// ignore
			}
			dbConnection.releasePreparedStatement(ps);
		}
		return new ArrayList<Reminder>(ret);
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
		return findOpenRemindersResponsibleFor(a, true, p, bOnlyPopup);
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
		getConnection()
			.exec("DELETE FROM REMINDERS_RESPONSIBLE_LINK WHERE ReminderID=" + getWrappedId());
		return super.delete();
	}
	
	public Visibility getVisibility(){
		return Visibility.byNumericSafe(get(FLD_VISIBILITY));
	}
	
	public void setVisibility(Visibility visibility){
		set(FLD_VISIBILITY, Integer.toString(visibility.numericValue()));
	}
	
	public Priority getPriority(){
		String priority = get(FLD_PRIORITY);
		return Priority.byNumericSafe(priority);
	}
	
	public void setPriority(Priority priority){
		set(FLD_PRIORITY, Integer.toString(priority.numericValue()));
	}
	
	public Type getActionType(){
		String actionType = get(FLD_ACTION_TYPE);
		return Type.byNumericSafe(actionType);
	}
	
	public void setActionType(Type at){
		set(FLD_ACTION_TYPE, Integer.toString(at.numericValue()));
	}
	
	public String getSubject(){
		return get(FLD_SUBJECT);
	}
	
	public void setSubject(String subject){
		set(FLD_SUBJECT, subject);
	}
}
