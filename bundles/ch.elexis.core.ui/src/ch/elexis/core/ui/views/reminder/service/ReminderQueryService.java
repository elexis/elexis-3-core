package ch.elexis.core.ui.views.reminder.service;

import java.time.LocalDate;
import java.util.List;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IReminderResponsibleLink;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.ISubQuery;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

/**
 * Central service for executing configurable reminder queries.
 * <p>
 * This class encapsulates all logic required to dynamically build and execute
 * {@link IReminder} queries with flexible filtering options, such as due date,
 * creator, responsible user, visibility, and patient/group association.
 * </p>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * ReminderQueryService service = new ReminderQueryService();
 * ReminderQueryService.Config cfg = new ReminderQueryService.Config().showAll(false).showSelfCreated(false)
 * 		.assignedToMe(true).showOnlyDue(true).filterDue(true);
 *
 * List<IReminder> reminders = service.load(cfg);
 * }</pre>
 *
 * @author Dalibor Aksic
 */
public class ReminderQueryService {

	private static final String GLOBALFILTERS = "global"; //$NON-NLS-1$
	private static int filterDueDateDays = ConfigServiceHolder
			.getUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS + "/" + GLOBALFILTERS, -1);

	/**
	 * Executes a reminder query based on the provided {@link Config}.
	 * <p>
	 * The method constructs the {@link IQuery} dynamically, applying filters such
	 * as:
	 * <ul>
	 * <li>Due date (past, upcoming, or unspecified)</li>
	 * <li>Popup visibility (on login / patient selection)</li>
	 * <li>Creator and responsible assignment</li>
	 * <li>Patient or group association</li>
	 * <li>Exclusion of closed reminders</li>
	 * </ul>
	 * </p>
	 *
	 * @param cfg the query configuration defining all filter options
	 * @return a list of {@link IReminder} objects matching the criteria
	 */
	public List<IReminder> load(Config cfg) {
		IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);

		// --- Due date filters ---
		if (cfg.showOnlyDue) {
			query.startGroup();
			query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.LESS_OR_EQUAL, LocalDate.now());
			query.or(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.EQUALS, null);
			query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);
			query.andJoinGroups();
		}

		if (cfg.showNotYetDueReminders) {
			query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.GREATER_OR_EQUAL, LocalDate.now());
		}

		// --- Popup visibility filters ---
		if (cfg.popupOnLogin || cfg.popupOnPatientSelection) {
			query.startGroup();
			if (cfg.popupOnLogin) {
				query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS, Visibility.POPUP_ON_LOGIN);
			}
			if (cfg.popupOnPatientSelection) {
				query.or(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
						Visibility.POPUP_ON_PATIENT_SELECTION);
			}
			query.andJoinGroups();
		}

		// --- Creator and responsible user filters ---
		ContextServiceHolder.get().getActiveUserContact().ifPresent(m -> {
			if (cfg.showSelfCreated) {
				query.and(ModelPackage.Literals.IREMINDER__CREATOR, COMPARATOR.EQUALS, m);
			}

			if (cfg.assignedToMe) {
				ISubQuery<IReminderResponsibleLink> subQuery = query.createSubQuery(IReminderResponsibleLink.class,
						CoreModelServiceHolder.get());
				subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
				subQuery.and("responsible", COMPARATOR.EQUALS, m);
				query.exists(subQuery);
			}

		});

		// --- Due date range (based on preferences) ---
		if (cfg.filterDue) {
			LocalDate now = LocalDate.now();
			int days = ConfigServiceHolder.getUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS, 30);
			LocalDate limit = now.plusDays(days);

			query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);
			query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.LESS_OR_EQUAL, limit);
		}

		// --- Patient filter ---
		if (cfg.patient != null) {
			query.and(ModelPackage.Literals.IREMINDER__CONTACT, COMPARATOR.EQUALS, cfg.patient);
			query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);
		}

		// --- Group filter ---
		if (cfg.group != null) {
			query.and(ModelPackage.Literals.IREMINDER__GROUP, COMPARATOR.EQUALS, cfg.group);
		}

		// --- Reminders without a patient ---
		if (cfg.noPatient) {
			query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);
			query.andFeatureCompare(ModelPackage.Literals.IREMINDER__CREATOR, COMPARATOR.EQUALS,
					ModelPackage.Literals.IREMINDER__CONTACT);
		}

		// --- Include reminders with no due date if no due filter is active ---
		if (!cfg.showOnlyDue && !cfg.showNotYetDueReminders) {
			query.or(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.EQUALS, null);
		}

		return query.execute();
	}

	/**
	 * Applies a due-date filter to the given query, limiting results to reminders
	 * due within the configured number of days.
	 *
	 * @param query        the query to which the filter will be applied
	 * @param includeNoDue if {@code true}, reminders without a due date are also
	 *                     included
	 */
	public static void applyDueDateFilter(IQuery<IReminder> query, boolean includeNoDue) {
		LocalDate now = LocalDate.now();
		LocalDate dueDateDays = now.plusDays(filterDueDateDays);
		query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);

		if (!includeNoDue) {
			query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.NOT_EQUALS, null);
		}

		query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.LESS_OR_EQUAL, dueDateDays);
	}

	// ========================================================================
	// CONFIGURATION CLASS
	// ========================================================================

	/**
	 * Configuration object that defines all filter options for
	 * {@link ReminderQueryService#load(Config)}.
	 * <p>
	 * The configuration follows a fluent builder pattern to allow chaining of
	 * options.
	 * </p>
	 *
	 * <pre>{@code
	 * Config cfg = new Config().showAll(true).assignedToMe(true).showOnlyDue(false);
	 * }</pre>
	 */
	public static class Config {
		public boolean showAll;
		public boolean showSelfCreated;
		public boolean assignedToMe;
		public boolean showOnlyDue;
		public boolean showNotYetDueReminders;
		public boolean popupOnLogin;
		public boolean popupOnPatientSelection;
		public boolean filterDue;
		public Object group;
		public Object patient;
		public boolean noPatient;

		/** Show all reminders (ignoring ownership or responsibility). */
		public Config showAll(boolean v) {
			this.showAll = v;
			return this;
		}

		/** Show only reminders created by the current mandator. */
		public Config showSelfCreated(boolean v) {
			this.showSelfCreated = v;
			return this;
		}

		/** Show reminders assigned to the current mandator. */
		public Config assignedToMe(boolean v) {
			this.assignedToMe = v;
			return this;
		}

		/** Show reminders that are due today or earlier. */
		public Config showOnlyDue(boolean v) {
			this.showOnlyDue = v;
			return this;
		}

		/** Show reminders that are not yet due (future due dates). */
		public Config showNotYetDueReminders(boolean v) {
			this.showNotYetDueReminders = v;
			return this;
		}

		/** Show reminders that should pop up on login. */
		public Config popupOnLogin(boolean v) {
			this.popupOnLogin = v;
			return this;
		}

		/** Show reminders that should pop up on patient selection. */
		public Config popupOnPatientSelection(boolean v) {
			this.popupOnPatientSelection = v;
			return this;
		}

		/** Apply a due-date range filter (based on global preferences). */
		public Config filterDue(boolean v) {
			this.filterDue = v;
			return this;
		}

		/** Restrict the query to reminders of a specific group. */
		public Config group(Object g) {
			this.group = g;
			return this;
		}

		/** Restrict the query to reminders for a specific patient. */
		public Config patient(Object p) {
			this.patient = p;
			return this;
		}

		/** Show reminders that are not linked to any patient. */
		public Config noPatient(boolean v) {
			this.noPatient = v;
			return this;
		}
	}
}
