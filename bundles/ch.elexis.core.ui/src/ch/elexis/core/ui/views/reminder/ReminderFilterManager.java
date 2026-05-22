package ch.elexis.core.ui.views.reminder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.ReminderListsView;

public class ReminderFilterManager {

	public record FilterActions(Action deleteReminderAction, Action showAssignedToMeAction,
			Action popupOnPatientSelectionReminderToggleAction, Action popupOnLoginReminderToggleAction,
			Action showNotYetDueReminderToggleAction, Action showPastDueReminderToggleAction,
			Action showOnlyOwnDueReminderToggleAction,
			Action showSelfCreatedReminderAction, RestrictedAction showOthersRemindersAction) {
// WICHTIG: showPastDueReminderToggleAction wurde hier im Record und in der reload()/reset() Liste hinzugefügt!

		public void reload() {
			for (Object action : new Object[] { showAssignedToMeAction, popupOnPatientSelectionReminderToggleAction,
					popupOnLoginReminderToggleAction, showNotYetDueReminderToggleAction,
					showPastDueReminderToggleAction, showOnlyOwnDueReminderToggleAction, showSelfCreatedReminderAction,
					showOthersRemindersAction }) {
				try {
					Method refreshMethod = action.getClass().getMethod("reload"); //$NON-NLS-1$
					refreshMethod.invoke(action);
				} catch (Exception e) {
					LoggerFactory.getLogger(getClass()).error("Error reloading filters", e);
				}
			}
		}

		public void reset() {
			for (Object action : new Object[] { showAssignedToMeAction, popupOnPatientSelectionReminderToggleAction,
					popupOnLoginReminderToggleAction, showNotYetDueReminderToggleAction,
					showOnlyOwnDueReminderToggleAction, showSelfCreatedReminderAction, showOthersRemindersAction }) {
				try {
					Method refreshMethod = action.getClass().getMethod("reset"); //$NON-NLS-1$
					refreshMethod.invoke(action);
				} catch (Exception e) {
					LoggerFactory.getLogger(getClass()).error("Error resetting filters", e);
				}
			}
		}
	}

	private final ReminderListsView view;
	private final HashMap<String, FilterActions> filtersMap = new HashMap<>();

	public ReminderFilterManager(ReminderListsView view) {
		this.view = view;
	}

	public FilterActions getFiltersFor(String config) {
		return filtersMap.get(config);
	}

	public void reloadAllFilters() {
		filtersMap.values().forEach(FilterActions::reload);
	}

	public void resetOtherFilters(String config) {
		if (view.isUseGlobalFilters()) {
			for (Entry<String, FilterActions> set : filtersMap.entrySet()) {
				set.getValue().reset();
			}
		} else {
			filtersMap.get(config).reset();
		}
		view.setAutoSelectPatient(false);
	}

	public FilterActions createFilterActions(String config, TableViewer viewer) {
		Action deleteReminderAction = new Action(ch.elexis.core.ui.views.Messages.Core_Delete) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(ch.elexis.core.ui.views.Messages.ReminderView_deleteToolTip);
			}

			@Override
			public void run() {
				StructuredSelection sel = (StructuredSelection) viewer.getStructuredSelection();
				if (sel != null && sel.size() == 1 && sel.getFirstElement() instanceof IReminder) {
					IReminder r = (IReminder) sel.getFirstElement();
					LockResponse lockResponse = LocalLockServiceHolder.get().acquireLock(r);
					if (lockResponse.isOk()) {
						CoreModelServiceHolder.get().delete(r);
						LocalLockServiceHolder.get().releaseLock(r);
					} else {
						LockResponseHelper.showInfo(lockResponse, r, null);
					}
					view.refreshKeepLabels();
				}
			}

			@Override
			public boolean isEnabled() {
				StructuredSelection sel = (StructuredSelection) viewer.getStructuredSelection();
				return (sel != null && sel.size() == 1 && sel.getFirstElement() instanceof IReminder);
			}
		};

		RestrictedAction showOthersRemindersAction = new RestrictedAction(EvACE.of(IReminder.class, Right.VIEW),
				ch.elexis.core.ui.views.Messages.Core_All, Action.AS_CHECK_BOX) {
			{
				setToolTipText(ch.elexis.core.ui.views.Messages.ReminderView_foreignTooltip);
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (view.isUseGlobalFilters()) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.USR_REMINDEROTHERS + "/" + ReminderListsView.GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.USR_REMINDEROTHERS + "/" + config, false)); //$NON-NLS-1$
				}
			}

			@Override
			public void doRun() {
				if (this.isChecked()) {
					boolean continueOperation = SWTHelper.askYesNo(ch.elexis.core.ui.views.Messages.Core_Warning,
							ch.elexis.core.ui.views.Messages.ReminderView_WarningAllFilter);
					if (!continueOperation) {
						this.setChecked(false);
						return;
					}
				}
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROTHERS + "/" + ReminderListsView.GLOBALFILTERS,
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROTHERS + "/" + config, this.isChecked());
				}
				resetOtherFilters(config);
				view.refresh();
			}
		};

		Action showSelfCreatedReminderAction = new Action(
				ch.elexis.core.ui.views.Messages.ReminderView_myRemindersAction, Action.AS_CHECK_BOX) {
			{
				setToolTipText(ch.elexis.core.ui.views.Messages.ReminderView_myRemindersToolTip);
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (view.isUseGlobalFilters()) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.USR_REMINDEROWN + "/" + ReminderListsView.GLOBALFILTERS, false));
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.USR_REMINDEROWN + "/" + config, false));
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				this.setChecked(false);
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROWN + "/" + ReminderListsView.GLOBALFILTERS,
							false);
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROWN + "/" + config, false);
				}
			}

			@Override
			public void run() {
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROWN + "/" + ReminderListsView.GLOBALFILTERS,
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROWN + "/" + config, this.isChecked());
				}
				view.refresh();
			}
		};

		Action showOnlyOwnDueReminderToggleAction = new Action(
				ch.elexis.core.ui.views.Messages.ReminderView_onlyDueAction, Action.AS_CHECK_BOX) {
			{
				setToolTipText(ch.elexis.core.ui.views.Messages.ReminderView_onlyDueToolTip);
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (view.isUseGlobalFilters()) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.USR_REMINDERSOPEN + "/" + ReminderListsView.GLOBALFILTERS, false));
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.USR_REMINDERSOPEN + "/" + config, false));
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				this.setChecked(false);
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERSOPEN + "/" + ReminderListsView.GLOBALFILTERS,
							false);
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERSOPEN + "/" + config, false);
				}
			}

			@Override
			public void run() {
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERSOPEN + "/" + ReminderListsView.GLOBALFILTERS,
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERSOPEN + "/" + config, this.isChecked());
				}
				view.refresh();
			}
		};

		Action showNotYetDueReminderToggleAction = new Action(ch.elexis.core.ui.views.Messages.ShowNotYetDueReminders,
				Action.AS_CHECK_BOX) {
			{
				setToolTipText(ch.elexis.core.ui.views.Messages.ShowNotYetDueReminders_Tooltip);
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (view.isUseGlobalFilters()) {
					this.setChecked(ConfigServiceHolder.getUser(
							Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + ReminderListsView.GLOBALFILTERS, false));
				} else {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + config, false));
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				this.setChecked(false);
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(
							Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + ReminderListsView.GLOBALFILTERS, false);
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + config, false);
				}
			}

			@Override
			public void run() {
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(
							Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + ReminderListsView.GLOBALFILTERS,
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + config, this.isChecked());
				}
				view.refresh();
			}
		};

		Action popupOnLoginReminderToggleAction = new Action(ch.elexis.core.ui.views.Messages.Reminders_PopupOnLogin,
				Action.AS_CHECK_BOX) {
			{
				setToolTipText(ch.elexis.core.ui.views.Messages.Reminders_PopupOnLogin_ToolTip);
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (view.isUseGlobalFilters()) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.POPUP_ON_LOGIN + "/" + ReminderListsView.GLOBALFILTERS, false));
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.POPUP_ON_LOGIN + "/" + config, false));
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				this.setChecked(false);
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_LOGIN + "/" + ReminderListsView.GLOBALFILTERS,
							false);
				} else {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_LOGIN + "/" + config, false);
				}
			}

			@Override
			public void run() {
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_LOGIN + "/" + ReminderListsView.GLOBALFILTERS,
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_LOGIN + "/" + config, this.isChecked());
				}
				view.refresh();
			}
		};

		Action popupOnPatientSelectionReminderToggleAction = new Action(
				ch.elexis.core.ui.views.Messages.Reminders_PopupOnPatientSelection, Action.AS_CHECK_BOX) {
			{
				setToolTipText(ch.elexis.core.ui.views.Messages.Reminders_PopupOnPatientSelection_ToolTip);
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (view.isUseGlobalFilters()) {
					this.setChecked(ConfigServiceHolder.getUser(
							Preferences.POPUP_ON_PATIENT_SELECTION + "/" + ReminderListsView.GLOBALFILTERS, false));
				} else {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + config, false));
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				this.setChecked(false);
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(
							Preferences.POPUP_ON_PATIENT_SELECTION + "/" + ReminderListsView.GLOBALFILTERS, false);
				} else {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + config, false);
				}
			}

			@Override
			public void run() {
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(
							Preferences.POPUP_ON_PATIENT_SELECTION + "/" + ReminderListsView.GLOBALFILTERS,
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + config,
							this.isChecked());
				}
				view.refresh();
			}
		};

		Action showAssignedToMeAction = new Action(ch.elexis.core.ui.views.Messages.Reminders_AssignedToMe,
				Action.AS_CHECK_BOX) {
			{
				setToolTipText(ch.elexis.core.ui.views.Messages.Reminders_AssignedToMe_ToolTip);
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (view.isUseGlobalFilters()) {
					this.setChecked(ConfigServiceHolder.getUser(
							Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + ReminderListsView.GLOBALFILTERS, false));
				} else {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + config, false));
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				this.setChecked(false);
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser(
							Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + ReminderListsView.GLOBALFILTERS, false);
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + config, false);
				}
			}

			@Override
			public void run() {
				boolean checked = this.isChecked();
				String key = Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/"
						+ (view.isUseGlobalFilters() ? ReminderListsView.GLOBALFILTERS : config);
				ConfigServiceHolder.setUser(key, checked);

				Display.getDefault().asyncExec(() -> {
					if (viewer != null && !viewer.getTable().isDisposed()) {
						Table table = viewer.getTable();
						for (TableColumn column : table.getColumns()) {
							if (ch.elexis.core.ui.views.Messages.EditReminderDialog_assigTo
									.equalsIgnoreCase(column.getText())) {
								column.setResizable(!checked);
								column.setWidth(checked ? 0 : 80);
								column.setMoveable(!checked);
								column.setData("hidden", checked);
								break;
							}
						}
					}
				});

				ConfigServiceHolder.setUser("reminder.column.hidden.Zust." + config, checked);
				view.refresh();
			}
		};

		Action showPastDueReminderToggleAction = new Action("Erinnerungen mit vergangenem Datum anzeigen",
				Action.AS_CHECK_BOX) {
			{
				setToolTipText("Zeigt nur Pendenzen an, deren Datum in der Vergangenheit liegt");
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (view.isUseGlobalFilters()) {
					this.setChecked(ConfigServiceHolder
							.getUser("reminder.filter.past_due/" + ReminderListsView.GLOBALFILTERS, false));
				} else {
					this.setChecked(ConfigServiceHolder.getUser("reminder.filter.past_due/" + config, false));
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				this.setChecked(false);
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser("reminder.filter.past_due/" + ReminderListsView.GLOBALFILTERS, false);
				} else {
					ConfigServiceHolder.setUser("reminder.filter.past_due/" + config, false);
				}
			}

			@Override
			public void run() {
				if (view.isUseGlobalFilters()) {
					ConfigServiceHolder.setUser("reminder.filter.past_due/" + ReminderListsView.GLOBALFILTERS,
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser("reminder.filter.past_due/" + config, this.isChecked());
				}
				view.refresh();
			}
		};

		FilterActions actions = new FilterActions(deleteReminderAction, showAssignedToMeAction,
				popupOnPatientSelectionReminderToggleAction, popupOnLoginReminderToggleAction,
				showNotYetDueReminderToggleAction, showPastDueReminderToggleAction, showOnlyOwnDueReminderToggleAction,
				showSelfCreatedReminderAction,
				showOthersRemindersAction);

		filtersMap.put(config, actions);
		return actions;
	}
}