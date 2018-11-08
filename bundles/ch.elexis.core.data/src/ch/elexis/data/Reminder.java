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
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
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
	
	public static final String FLD_MESSAGE = "Message";
	public static final String FLD_VISIBILITY = "Typ";
	public static final String FLD_STATUS = "Status";
	public static final String FLD_DUE = "Due";
	public static final String FLD_CREATOR = "Creator";
	public static final String FLD_KONTAKT_ID = "IdentID";
	public static final String FLD_RESPONSIBLE = "Responsible";
	public static final String FLD_PRIORITY = "priority";
	public static final String FLD_ACTION_TYPE = "actionType";
	public static final String FLD_SUBJECT = "subject";
	public static final String FLD_PARAMS = "Params";
	public static final String FLD_JOINT_RESPONSIBLES = "Responsibles";
	
	public static final Cache<String, Boolean> cachedAttributeKeys = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).build();

	/**
	 * To be stored in {@link #FLD_RESPONSIBLE}, making this reminder a responsibility for every
	 * user.
	 * 
	 * @since 3.4
	 */
	public static final String ALL_RESPONSIBLE = "ALL";
	
	public enum LabelFields {
			PAT_ID(Patient.FLD_PATID), FIRSTNAME(Person.FIRSTNAME), LASTNAME(Person.NAME),
			BIRTHDAY(Patient.FLD_DOB);
		
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
		addMapping(TABLENAME, FLD_KONTAKT_ID, FLD_CREATOR + "=OriginID", FLD_DUE + "=S:D:DateDue",
			FLD_STATUS, FLD_VISIBILITY, FLD_PARAMS, FLD_MESSAGE, FLD_RESPONSIBLE,
			FLD_JOINT_RESPONSIBLES + "=JOINT:ResponsibleID:ReminderID:REMINDERS_RESPONSIBLE_LINK",
			FLD_PRIORITY, FLD_ACTION_TYPE, FLD_SUBJECT);
	}
	
	Reminder(){/* empty */}
	
	private Reminder(final String id){
		super(id);
	}
	
	@Override
	public void clearCachedAttributes(){
		Iterator<Entry<String, Boolean>> iterator =
			cachedAttributeKeys.asMap().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, Boolean> key = iterator.next();
			if (key.getKey().contains(getId())) {
				getDefaultConnection().getCache().remove(key.getKey());
			}
			iterator.remove();
		}
	}
	
	@Override
	public String getKey(String field){
		String key = super.getKey(field);
		cachedAttributeKeys.put(key, true);
		return key;
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
			FLD_KONTAKT_ID, FLD_CREATOR, FLD_DUE, FLD_STATUS, FLD_VISIBILITY, FLD_PARAMS, FLD_MESSAGE
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
		List<Anwender> responsibles = getResponsibles();
		if (responsibles == null) {
			responsibles = new ArrayList<Anwender>();
		}
		responsibles.add(a);
		setResponsible(responsibles);
	}
	
	/** Einen Reminder anhand seiner ID aus der Datenbank einlesen */
	public static Reminder load(final String id){
		return new Reminder(id);
	}
	
	@Override
	public String getLabel(){
		String[] vals = get(true, FLD_KONTAKT_ID, FLD_DUE, FLD_MESSAGE, FLD_SUBJECT, FLD_CREATOR);
		Kontakt k = Kontakt.load(vals[0]);
		boolean isPatientRelatedReminder = isPatientRelatedReminder(vals[4], vals[0]);
		
		StringBuilder sb = new StringBuilder();
		if (vals[1].length() > 0) {
			sb.append(vals[1] + " ");
		}
		sb.append("(" + getConfiguredKontaktLabel(k, isPatientRelatedReminder) + "): ");
		sb.append((vals[3].length() > 1) ? vals[3] : vals[2]);
		return sb.toString();
	}
	
	private String getConfiguredKontaktLabel(Kontakt k, boolean isPatientRelatedReminder){
		if (isPatientRelatedReminder) {
			StringBuilder sb = new StringBuilder();
			String[] configLabel = CoreHub.userCfg
				.get(Preferences.USR_REMINDER_PAT_LABEL_CHOOSEN, LabelFields.LASTNAME.toString())
				.split(",");
			
			String[] values = k.get(true, configLabel);
			for (int i = 0; i < values.length; i++) {
				sb.append(values[i]);
				
				if (i != values.length - 1) {
					sb.append(", ");
				}
			}
			return sb.toString();
		}
		return k.get(Kontakt.FLD_NAME3);
	}
	
	public static ProcessStatus determineCurrentStatus(ProcessStatus givenStatus, TimeTool dueDate){
		if (ProcessStatus.CLOSED == givenStatus || ProcessStatus.ON_HOLD == givenStatus) {
			return givenStatus;
		}
		
		if (dueDate == null) {
			return givenStatus;
		}
		
		int compare = TimeTool.compare(new TimeTool(), dueDate);
		if (compare == 0) {
			return ProcessStatus.DUE;
		}
		if (compare == -1) {
			return ProcessStatus.OVERDUE;
		}
		
		return givenStatus;
	}
	
	/**
	 * Get the current reminder processing state
	 * 
	 * @return the current processing state
	 * @since 3.4
	 */
	public ProcessStatus getProcessStatus(){
		return ProcessStatus.byNumericSafe(get(FLD_STATUS));
	}
	
	/**
	 * Set the current reminder processing state
	 * 
	 * @param processStatus
	 * @since 3.4
	 */
	public void setProcessStatus(ProcessStatus processStatus){
		set(FLD_STATUS, Byte.toString((byte) processStatus.ordinal()));
	}
	
	/**
	 * 
	 * @return
	 * @deprecated
	 */
	public ProcessStatus getStatus(){
		ProcessStatus ps = ProcessStatus.byNumericSafe(get(FLD_STATUS));
		return Reminder.determineCurrentStatus(ps, getDateDue());
	}
	
	public String getMessage(){
		return checkNull(get(FLD_MESSAGE));
	}
	
	/**
	 * 
	 * @param s
	 * @deprecated use {@link #setProcessStatus(ProcessStatus)}
	 */
	public void setStatus(ProcessStatus s){
		setProcessStatus(s);
	}
	
	/**
	 * Returns the date this reminder is due
	 * 
	 * @return <code>null</code> or the respective date
	 */
	public @Nullable TimeTool getDateDue(){
		String string = get(FLD_DUE);
		if (string == null || StringConstants.EMPTY == string) {
			return null;
		}
		TimeTool ret = new TimeTool(get(FLD_DUE));
		ret.chop(3);
		return ret;
	}
	
	/**
	 * 
	 * @return 0 if not yet due (due in the future), 1 if due (due today), 2 if overdue (due in the
	 *         past)
	 * @since 3.4
	 */
	public int getDueState(){
		return Reminder.determineDueState(getDateDue());
	}
	
	/**
	 * Determine the reminder respective due state for a given {@link TimeTool}
	 * 
	 * @param dueDate
	 * @return
	 * @since 3.4 0 if not yet due (due in the future), 1 if due (due today), 2 if overdue (due in
	 *        the past)
	 */
	public static int determineDueState(TimeTool dueDate){
		if (dueDate != null) {
			TimeTool now = new TimeTool();
			if (dueDate.isBefore(now)) {
				return 2;
			}
			if (dueDate.isEqual(now)) {
				return 1;
			}
		}
		return 0;
	}
	
	/**
	 * set who is responsible for this reminder
	 * 
	 * @param responsibles
	 *            if <code>null</code> ALL are responsible, if empty list NO ONE is responsible,
	 *            else the respective {@link Anwender}
	 * @since 3.4
	 */
	public void setResponsible(List<Anwender> responsibles){
		removeFromList(FLD_JOINT_RESPONSIBLES);
		if (responsibles == null) {
			set(FLD_RESPONSIBLE, ALL_RESPONSIBLE);
		} else {
			set(FLD_RESPONSIBLE, null);
			addAllToList(FLD_JOINT_RESPONSIBLES,
				responsibles.stream().map(a -> a.getId()).collect(Collectors.toList()),
				(String[]) null);
		}
	}
	
	/**
	 * get who is responsible for this reminder
	 * 
	 * @return
	 * @since 3.4 returning <code>null</code> defines all as being responsible
	 */
	public List<Anwender> getResponsibles(){
		if (ALL_RESPONSIBLE.equals(get(FLD_RESPONSIBLE))) {
			return null;
		}
		List<String[]> lResp = getList(FLD_JOINT_RESPONSIBLES, new String[0]);
		ArrayList<Anwender> ret = new ArrayList<Anwender>(lResp.size());
		for (String[] r : lResp) {
			ret.add(Anwender.load(r[0]));
		}
		return ret;
	}
	
	public Anwender getCreator(){
		return Anwender.load(checkNull(get(FLD_CREATOR)));
	}
	
	/**
	 * 
	 * @return if this is a patient related reminder
	 * @since 3.4
	 */
	public boolean isPatientRelated(){
		String vals[] = get(true, Reminder.FLD_KONTAKT_ID, Reminder.FLD_CREATOR);
		return isPatientRelatedReminder(vals[0],vals[1]);
	}
	
	private  boolean isPatientRelatedReminder(String creatorId, String contactId){
		return !Objects.equals(creatorId, contactId);
	}
	
	private static String PS_REMINDERS_RESPONSIBLE = "SELECT r.ID FROM " + TABLENAME
		+ " r LEFT JOIN REMINDERS_RESPONSIBLE_LINK rrl ON (r.id = rrl.ReminderId) WHERE (rrl.ResponsibleID = ? OR r.Responsible = '"
		+ ALL_RESPONSIBLE + "') AND r.deleted = '0'";
	
	public static List<Reminder> findAllUserIsResponsibleFor(Anwender anwender,
		boolean showOnlyDueReminders){
		Set<Reminder> ret = new HashSet<Reminder>();
		// we have to apply a set, as there may exist
		// multiple equivalent entries in reminders_responsible_link
		// which resolve to multiple occurences of the same element, due to the left join
		DBConnection dbConnection = getDefaultConnection();
		PreparedStatement ps;
		if (showOnlyDueReminders) {
			ps = dbConnection.getPreparedStatement(
				PS_REMINDERS_RESPONSIBLE + " AND r.DateDue != '' AND r.DateDue <= ?");
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
		qbe.add(FLD_DUE, Query.LESS_OR_EQUAL, new TimeTool().toString(TimeTool.DATE_COMPACT));
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
		qbe.add(FLD_KONTAKT_ID, Query.EQUALS, p.getId());
		qbe.add(FLD_STATUS, Query.NOT_EQUAL, Integer.toString(ProcessStatus.CLOSED.numericValue()));
		qbe.add(FLD_DUE, Query.LESS_OR_EQUAL, new TimeTool().toString(TimeTool.DATE_COMPACT));
		if (responsible != null) {
			qbe.startGroup();
			qbe.add(FLD_RESPONSIBLE, Query.EQUALS, responsible.getId());
			qbe.or();
			qbe.add(FLD_RESPONSIBLE, StringTool.leer, null);
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
		qbe.add(FLD_DUE, Query.LESS_OR_EQUAL, new TimeTool().toString(TimeTool.DATE_COMPACT));
		qbe.add(FLD_STATUS, Query.NOT_EQUAL, Integer.toString(ProcessStatus.CLOSED.numericValue()));
		qbe.add(FLD_VISIBILITY, Query.EQUALS,
			Integer.toString(Visibility.POPUP_ON_LOGIN.numericValue()));
		return qbe.execute();
	}
	
	private static String PS_REMINDERS_BASE = "SELECT r.ID FROM " + TABLENAME
		+ " r LEFT JOIN REMINDERS_RESPONSIBLE_LINK rrl ON (r.id = rrl.ReminderId) WHERE (rrl.ResponsibleID = ? OR r.Responsible = '"
		+ ALL_RESPONSIBLE + "') AND r.deleted = '0' AND r.Status != '3'";
	
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
			query.append(" AND r.DateDue != '' AND r.DateDue <= "
				+ JdbcLink.wrap(new TimeTool().toString(TimeTool.DATE_COMPACT)));
		}
		if (patient != null) {
			query.append(" AND r.IdentID = " + patient.getWrappedId());
		}
		
		PreparedStatement ps = dbConnection.getPreparedStatement(query.toString());
		try {
			if (anwender != null) {
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
			}
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
		Patient ret = Patient.load(get(FLD_KONTAKT_ID));
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
		int i = TimeTool.compare(getDateDue(), r.getDateDue());
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
