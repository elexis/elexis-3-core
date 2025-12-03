package ch.elexis.core.ui.views;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.Heartbeat.HeartListener;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IReminderResponsibleLink;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.ISubQuery;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.dialogs.ReminderDetailDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.locks.LockRequestingAction;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.reminder.service.ReminderQueryService;
import ch.elexis.core.ui.views.reminder.viewers.ReminderColumnFactory;
import ch.elexis.core.ui.views.reminder.viewers.ReminderColumnFactory.ReminderComparator;
import ch.elexis.core.ui.views.reminder.viewers.ReminderColumnType;
import ch.elexis.data.Reminder;

/**
 * 
 */
public class ReminderListsView extends ViewPart implements HeartListener, IRefreshable, ISelectionProvider {
	public static final String ID = "ch.elexis.core.ui.views.reminderlistsview"; //$NON-NLS-1$

	private final static String CURRENTPATIENT = "currentpatient"; //$NON-NLS-1$
	private final static String ALLPATIENTS = "allpatients"; //$NON-NLS-1$
	private final static String GENERALREMINDERS = "generalreminders"; //$NON-NLS-1$
	private final static String MYREMINDERS = "myreminders"; //$NON-NLS-1$
	private final static String GLOBALFILTERS = "global"; //$NON-NLS-1$

	private boolean useGlobalFilters = ConfigServiceHolder.getUser(Preferences.USR_REMINDER_USE_GLOBAL_FILTERS, false);
	private int filterDueDateDays = ConfigServiceHolder
			.getUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS + "/" + GLOBALFILTERS, -1);
	private boolean autoSelectPatient = ConfigServiceHolder
			.getUser(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT + "/" + GLOBALFILTERS,
			false);
	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	private Composite viewParent;

	private Text txtSearch;
	private ReminderFilter filter = new ReminderFilter();

	private ViewerSelectionComposite viewerSelectionComposite;

	private Composite viewersParent;
	private ScrolledComposite viewersScrolledComposite;

	private HeaderComposite currentPatientHeader;
	private TableViewer currentPatientViewer;
	private HeaderComposite generalPatientHeader;
	private TableViewer generalPatientViewer;
	private HeaderComposite generalRemindersHeader;
	private TableViewer generalRemindersViewer;
	private HeaderComposite myHeader;
	private TableViewer myViewer;
	private ReminderColumnFactory columnFactory;
	HashMap<TableViewer, String> allViewers = new HashMap<>();

	record GroupComponent(String id, HeaderComposite header, TableViewer viewer) {
	}

	private Font boldFont;

	private List<IReminder> currentSelection = new ArrayList<>();
	private ListenerList<ISelectionChangedListener> selectionChangedListeners = new ListenerList<>();

	private IPatient actPatient;
	private long cvHighestLastUpdate;

	private record FilterActions(Action deleteReminderAction, Action showAssignedToMeAction,
			Action popupOnPatientSelectionReminderToggleAction, Action popupOnLoginReminderToggleAction,
			Action showNotYetDueReminderToggleAction, Action showOnlyOwnDueReminderToggleAction,
			Action showSelfCreatedReminderAction, RestrictedAction showOthersRemindersAction) {
		public void reload() {
			for (Object action : new Object[] { showAssignedToMeAction, popupOnPatientSelectionReminderToggleAction,
					popupOnLoginReminderToggleAction, showNotYetDueReminderToggleAction,
					showOnlyOwnDueReminderToggleAction, showSelfCreatedReminderAction, showOthersRemindersAction }) {
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
					showOnlyOwnDueReminderToggleAction, showSelfCreatedReminderAction }) {
				try {
					Method refreshMethod = action.getClass().getMethod("reset"); //$NON-NLS-1$
					refreshMethod.invoke(action);
				} catch (Exception e) {
					LoggerFactory.getLogger(getClass()).error("Error resetting filters", e);
				}
			}
		}
	}

	HashMap<String, FilterActions> filtersMap = new HashMap<>();

	private Action reloadAction = new Action(Messages.Core_Reload) {
		{
			setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
			setToolTipText(Messages.Core_Reread_List);
		}

		@Override
		public void run() {
			refresh();
		}
	};
	
	private Action toggleGlobalFiltersAction = new Action(Messages.ReminderView_useGlobalFiltersAction,
			Action.AS_CHECK_BOX) {
		{
			setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
			setToolTipText(Messages.ReminderView_useGlobalFiltersToolTip);
			setChecked(useGlobalFilters);
		}
		
		@Override
		public void run() {
			ConfigServiceHolder.setUser(Preferences.USR_REMINDER_USE_GLOBAL_FILTERS, // $NON-NLS-1$
					this.isChecked());
			useGlobalFilters = toggleGlobalFiltersAction.isChecked();
			refreshUserConfiguration();
			refresh();
		}
	};

	private Action toggleAutoSelectPatientAction = new Action(Messages.ReminderView_activatePatientAction,
			Action.AS_CHECK_BOX) {
		{
			setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
			setToolTipText(Messages.ReminderView_toggleSelectPatientActionTooltip);
			setChecked(autoSelectPatient);
		}

		@Override
		public void run() {
			ConfigServiceHolder.setUser(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, // $NON-NLS-1$
					this.isChecked());
		}
	};
	
	private Action newReminderAction = new Action(Messages.Core_New_ellipsis) {
		{
			setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
			setToolTipText(Messages.ReminderView_newReminderToolTip);
		}

		@Override
		public void run() {
			ReminderDetailDialog erd = null;
			erd = new ReminderDetailDialog(getViewSite().getShell());
			int retVal = erd.open();
			if (retVal == Dialog.OK) {
				IReminder reminder = CoreModelServiceHolder.get().load(erd.getReminder().getId(), IReminder.class)
						.get();
				LocalLockServiceHolder.get().acquireLock(reminder);
				LocalLockServiceHolder.get().releaseLock(reminder);
			}
			refresh();
		}
	};

	/**
	 * <p>
	 * Create the filter actions for the contextmenu on the composites.
	 * </p>
	 * 
	 * @param config identification. used to save the config to the database with
	 *               the String. example: 'currentpatient', 'allpatients'
	 * @return FilterActions
	 */
	private FilterActions createFilterActions(String config) {

		Action deleteReminderAction = new Action(Messages.Core_Delete) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.ReminderView_deleteToolTip);
			}

			private TableViewer viewer = getViewerForId(config);

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
					refreshKeepLabels();
				}
			}

			@Override
			public boolean isEnabled() {
				StructuredSelection sel = (StructuredSelection) viewer.getStructuredSelection();
				return (sel != null && sel.size() == 1 && sel.getFirstElement() instanceof IReminder);
			}
		};

		RestrictedAction showOthersRemindersAction = new RestrictedAction(EvACE.of(IReminder.class, Right.VIEW),
				Messages.Core_All, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.ReminderView_foreignTooltip);
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDEROTHERS + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.USR_REMINDEROTHERS + "/" + config, false)); //$NON-NLS-1$
				}
			}

			@Override
			public void doRun() {
				if (this.isChecked()) {
					boolean continueOperation = SWTHelper.askYesNo(Messages.Core_Warning,
							Messages.ReminderView_WarningAllFilter);
					if (!continueOperation) {
						this.setChecked(false);
						return;
					}
				}
				if (useGlobalFilters) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROTHERS + "/" + GLOBALFILTERS, //$NON-NLS-1$
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROTHERS + "/" + config, //$NON-NLS-1$
							this.isChecked());
				}
				resetOtherFilters(config);
				refresh();
			}
		};

		Action showSelfCreatedReminderAction = new Action(Messages.ReminderView_myRemindersAction,
				Action.AS_CHECK_BOX) { // $NON-NLS-1$
			{
				setToolTipText(Messages.ReminderView_myRemindersToolTip); // $NON-NLS-1$
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDEROWN + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.USR_REMINDEROWN + "/" + config, false)); //$NON-NLS-1$
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				if (useGlobalFilters) {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROWN + "/" + GLOBALFILTERS, false); //$NON-NLS-1$
				} else {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROWN + "/" + config, false); //$NON-NLS-1$
				}
			}

			@Override
			public void run() {
				if (useGlobalFilters) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROWN + "/" + GLOBALFILTERS, //$NON-NLS-1$
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDEROWN + "/" + config, //$NON-NLS-1$
							this.isChecked());
				}
				refresh();
			}
		};

		Action showOnlyOwnDueReminderToggleAction = new Action(Messages.ReminderView_onlyDueAction,
				Action.AS_CHECK_BOX) { // $NON-NLS-1$
			{
				setToolTipText(Messages.ReminderView_onlyDueToolTip); // $NON-NLS-1$
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDERSOPEN + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.USR_REMINDERSOPEN + "/" + config, false)); //$NON-NLS-1$
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				if (useGlobalFilters) {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERSOPEN + "/" + GLOBALFILTERS, false); //$NON-NLS-1$
				} else {
					this.setChecked(ConfigServiceHolder.setUser(Preferences.USR_REMINDERSOPEN + "/" + config, false)); //$NON-NLS-1$
				}
			}

			@Override
			public void run() {
				if (useGlobalFilters) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERSOPEN + "/" + GLOBALFILTERS, //$NON-NLS-1$
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERSOPEN + "/" + config, //$NON-NLS-1$
							this.isChecked());
				}

				refresh();
			}
		};

		Action showNotYetDueReminderToggleAction = new Action(Messages.ShowNotYetDueReminders, Action.AS_CHECK_BOX) { // $NON-NLS-1$
			{
				setToolTipText(Messages.ShowNotYetDueReminders_Tooltip); // $NON-NLS-1$
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + config, false)); //$NON-NLS-1$
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				if (useGlobalFilters) {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + GLOBALFILTERS, false); //$NON-NLS-1$
				} else {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + config, false); //$NON-NLS-1$
				}
			}

			@Override
			public void run() {
				if (useGlobalFilters) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + GLOBALFILTERS, //$NON-NLS-1$
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + config, //$NON-NLS-1$
							this.isChecked());
				}

				refresh();
			}
		};

		Action popupOnLoginReminderToggleAction = new Action(Messages.Reminders_PopupOnLogin, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.Reminders_PopupOnLogin_ToolTip); // $NON-NLS-1$
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.POPUP_ON_LOGIN + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.POPUP_ON_LOGIN + "/" + config, false)); //$NON-NLS-1$
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				if (useGlobalFilters) {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_LOGIN + "/" + GLOBALFILTERS, false); //$NON-NLS-1$
				} else {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_LOGIN + "/" + config, false); //$NON-NLS-1$
				}
			}

			@Override
			public void run() {
				if (useGlobalFilters) {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_LOGIN + "/" + GLOBALFILTERS, //$NON-NLS-1$
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_LOGIN + "/" + config, //$NON-NLS-1$
							this.isChecked());
				}
				refresh();
			}
		};

		Action popupOnPatientSelectionReminderToggleAction = new Action(Messages.Reminders_PopupOnPatientSelection,
				Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.Reminders_PopupOnPatientSelection_ToolTip); // $NON-NLS-1$
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + config, false)); //$NON-NLS-1$
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				if (useGlobalFilters) {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + GLOBALFILTERS, false); //$NON-NLS-1$
				} else {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + config, false); //$NON-NLS-1$
				}
			}

			@Override
			public void run() {
				if (useGlobalFilters) {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + GLOBALFILTERS, //$NON-NLS-1$
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + config, //$NON-NLS-1$
							this.isChecked());
				}

				refresh();
			}
		};

		Action showAssignedToMeAction = new Action(Messages.Reminders_AssignedToMe, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.Reminders_AssignedToMe_ToolTip);
			}

			@SuppressWarnings("unused")
			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + config, false)); //$NON-NLS-1$
				}
			}

			@SuppressWarnings("unused")
			public void reset() {
				if (useGlobalFilters) {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + GLOBALFILTERS, false); //$NON-NLS-1$
				} else {
					this.setChecked(false);
					ConfigServiceHolder.setUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + config, false); //$NON-NLS-1$
				}
			}

			@Override
			public void run() {
				boolean checked = this.isChecked();

				String key = Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/"
						+ (useGlobalFilters ? GLOBALFILTERS : config);
				ConfigServiceHolder.setUser(key, checked);

				Display.getDefault().asyncExec(() -> {
					TableViewer viewer = getViewerForId(config);
					if (viewer != null && !viewer.getTable().isDisposed()) {
						Table table = viewer.getTable();
						for (TableColumn column : table.getColumns()) {
							if (Messages.EditReminderDialog_assigTo.equalsIgnoreCase(column.getText())) {
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

				refresh();
			}
		};
		
		return new FilterActions(deleteReminderAction, showAssignedToMeAction,
				popupOnPatientSelectionReminderToggleAction, popupOnLoginReminderToggleAction,
				showNotYetDueReminderToggleAction, showOnlyOwnDueReminderToggleAction, showSelfCreatedReminderAction,
				showOthersRemindersAction);
	}

	/**
	 * resets the filters of a tableviewer to false. parameter config for example
	 * could be 'currentpatient', 'myreminders' or 'global'. <br>
	 * 'global' resets the filter actions of all tableviewers
	 * 
	 * @param config key for defining which filters to reset
	 */
	private void resetOtherFilters(String config) {
		if (useGlobalFilters) {
			for (Entry<String, FilterActions> set : filtersMap.entrySet()) {
				set.getValue().reset();
			}
		} else {
			filtersMap.get(config).reset();
		}

		toggleAutoSelectPatientAction.setChecked(false);
	}

	private RestrictedAction selectPatientAction = new RestrictedAction(EvACE.of(IPatient.class, Right.VIEW),
			Messages.ReminderView_activatePatientAction, Action.AS_UNSPECIFIED) {
		{
			setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
			setToolTipText(Messages.ReminderView_activatePatientTooltip);
		}

		public void doRun() {
			StructuredSelection sel = (StructuredSelection) getSelection();
			if (sel != null && sel.size() != 1) {
				SWTHelper.showInfo(Messages.ReminderView_onePatOnly, Messages.ReminderView_onlyOnePatientForActivation);
			} else if (sel != null && sel.size() > 0) {
				IReminder reminder = (IReminder) sel.getFirstElement();
				IContact patient = reminder.getContact();
				IContact creator = reminder.getCreator();
				if (patient != null && patient.isPatient()) {
					if (!patient.getId().equals(creator.getId())) {
						ContextServiceHolder.get().setActivePatient(patient.asIPatient());
					}
				}
			}
		}

		@Override
		public boolean isEnabled() {
			StructuredSelection sel = (StructuredSelection) getSelection();
			if (sel != null && sel.size() == 1 && sel.getFirstElement() instanceof IReminder) {
				IReminder reminder = (IReminder) sel.getFirstElement();
				return (reminder.getContact() != null && reminder.getCreator() != null)
						? !reminder.getContact().getId().equals(reminder.getCreator().getId())
						: false;
			}
			return false;
		}
	};

	@Optional
	@Inject
	void activePatient(IPatient patient) {
		CoreUiUtil.runAsyncIfActive(() -> {

			if (patient.equals(actPatient)) {
				return;
			}
			actPatient = patient;
			clearSelection();
			patientRefresh();

			/**
			 * ch.elexis.core.data.events.PatientEventListener will be called on opposite
			 * Preferences.USR_SHOWPATCHGREMINDER condition.
			 */
			if (!ConfigServiceHolder.getUser(Preferences.USR_SHOWPATCHGREMINDER, true)) {
				UiDesk.asyncExec(new Runnable() {

					public void run() {
						// base reminders for active patient query execution
						IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
						query.and(ModelPackage.Literals.IREMINDER__CONTACT, COMPARATOR.EQUALS, patient);
						query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);
						query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
								Visibility.POPUP_ON_PATIENT_SELECTION);
						query.startGroup();

						// active mandator responsible
						ContextServiceHolder.get().getActiveMandator().ifPresent(m -> {
							ISubQuery<IReminderResponsibleLink> subQuery = query
									.createSubQuery(IReminderResponsibleLink.class, CoreModelServiceHolder.get());
							subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid"); //$NON-NLS-1$ //$NON-NLS-2$
							subQuery.and("responsible", COMPARATOR.EQUALS, m); //$NON-NLS-1$
							query.exists(subQuery);
						});

						// or responsible all
						query.or("responsibleValue", COMPARATOR.EQUALS, "ALL"); //$NON-NLS-1$ //$NON-NLS-2$
						query.andJoinGroups();

						List<IReminder> list = query.execute();
						if (!list.isEmpty()) {
							StringBuilder sb = new StringBuilder();
							for (IReminder r : list) {
								sb.append(r.getSubject() + StringUtils.LF);
								sb.append(r.getMessage() + "\n\n"); //$NON-NLS-1$
							}
							SWTHelper.alert(Messages.ReminderView_importantRemindersCaption, sb.toString());
						}
					}
				});
			}
			refresh();
		}, viewersParent);
	}

	@Inject
	void activeUser(@Optional IUser user) {
		Display.getDefault().asyncExec(() -> {
			if (viewersScrolledComposite != null && !viewersScrolledComposite.isDisposed()) {
				adaptForUser(user);
			}
		});
	}

	@Optional
	@Inject
	void onReminderReload(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Class<?> clazz) {
		if (IReminder.class.equals(clazz)) {
			Display.getDefault().asyncExec(() -> {
				refresh();
			});
		}
	}

	private void adaptForUser(IUser user) {
		refreshUserConfiguration();
		refresh();
	}

	@Optional
	@Inject
	void crudFinding(@UIEventTopic(ElexisEventTopics.BASE_MODEL + "*") IReminder reminder) {
		CoreUiUtil.runAsyncIfActive(() -> {
			refresh();
		}, viewerSelectionComposite);
	}

	@Override
	public void createPartControl(Composite parent) {
		viewParent = new Composite(parent, SWT.NONE);
		viewParent.setLayout(new GridLayout());

		txtSearch = new Text(viewParent, SWT.SEARCH);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtSearch.setMessage(Messages.ReminderView_txtSearch_message);
		txtSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				clearSelection();
				filter.setFilterText(txtSearch.getText());
				refreshKeepLabels();
			}
		});
		boldFont = SWTResourceManager.getBoldFont(txtSearch.getFont());

		viewerSelectionComposite = new ViewerSelectionComposite(viewParent, SWT.NONE);
		viewerSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		viewersScrolledComposite = new ScrolledComposite(viewParent, SWT.V_SCROLL);
		viewersScrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewersScrolledComposite.setExpandVertical(true);
		viewersScrolledComposite.setExpandHorizontal(true);
		viewersScrolledComposite.addListener(SWT.Resize, event -> {
			int width = viewersScrolledComposite.getClientArea().width;
			viewersScrolledComposite.setMinSize(viewersParent.computeSize(width, SWT.DEFAULT));
		});

		viewersParent = new Composite(viewersScrolledComposite, SWT.NONE);
		viewersParent.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
		GridLayout layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		viewersParent.setLayout(layout);
		viewersScrolledComposite.setContent(viewersParent);

		currentPatientHeader = new HeaderComposite(viewersParent, SWT.NONE);
		currentPatientHeader.setTextFont(boldFont);
		currentPatientHeader.setText(Messages.ReminderView_currentPatient);
		currentPatientViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(currentPatientViewer, false);
		addDragSupport(currentPatientViewer);
		addDropSupport(currentPatientViewer, TableType.CURRENT_PATIENT);

		generalPatientHeader = new HeaderComposite(viewersParent, SWT.NONE);
		generalPatientHeader.setTextFont(boldFont);
		generalPatientHeader.setText(Messages.ReminderView_allPatients);
		generalPatientViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(generalPatientViewer, true);
		addDragSupport(generalPatientViewer);
		addDropSupport(generalPatientViewer, TableType.GENERAL_PATIENT);
		((GridData) generalPatientViewer.getTable().getLayoutData()).heightHint = 300;

		generalRemindersHeader = new HeaderComposite(viewersParent, SWT.NONE);
		generalRemindersHeader.setTextFont(boldFont);
		generalRemindersHeader.setText(Messages.ReminderView_generalReminders);
		generalRemindersViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(generalRemindersViewer, false);
		addDragSupport(generalRemindersViewer);
		addDropSupport(generalRemindersViewer, TableType.GENERALREMINDERS);
		((GridData) generalPatientViewer.getTable().getLayoutData()).heightHint = 300;

		myHeader = new HeaderComposite(viewersParent, SWT.NONE);
		myHeader.setTextFont(boldFont);
		myHeader.setText(Messages.ReminderView_myReminders);
		myViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(myViewer, true);
		addDragSupport(myViewer);
		addDropSupport(myViewer, TableType.MYREMINDERS);

		viewerSelectionComposite.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				updateViewerSelection(selection);
				refresh();
			}
		});
		actPatient = ContextServiceHolder.get().getActivePatient().orElse(null);

		viewerSelectionComposite.loadSelection();
		updateViewerSelection((StructuredSelection) viewerSelectionComposite.getSelection());

		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(reloadAction, newReminderAction, toggleGlobalFiltersAction, toggleAutoSelectPatientAction);

		allViewers.put(currentPatientViewer, CURRENTPATIENT);
		allViewers.put(generalPatientViewer, ALLPATIENTS);
		allViewers.put(generalRemindersViewer, GENERALREMINDERS);
		allViewers.put(myViewer, MYREMINDERS);

		for (Map.Entry<TableViewer, String> entry : allViewers.entrySet()) {
			String id = entry.getValue();
			Table table = entry.getKey().getTable();

			FilterActions actions = createFilterActions(id);
			filtersMap.put(id, actions);

			MenuManager timeFilterSubMenu = new MenuManager(Messages.ReminderView_timeFilterMenu);
			CustomTimeAction custom = new CustomTimeAction(Messages.ReminderView_customTimeAction, id);
			FilterTimeAction action30 = new FilterTimeAction(30);
			FilterTimeAction action60 = new FilterTimeAction(60);
			FilterTimeAction action90 = new FilterTimeAction(90);
			action30.setOthers(Arrays.asList(action60, action90, custom));
			action60.setOthers(Arrays.asList(action30, action90, custom));
			action90.setOthers(Arrays.asList(action30, action60, custom));
			custom.setOthers(Arrays.asList(action30, action60, action90));
			timeFilterSubMenu.add(action30);
			timeFilterSubMenu.add(action60);
			timeFilterSubMenu.add(action90);
			timeFilterSubMenu.add(custom);

			MenuManager menuManager = new MenuManager();
			menuManager.add(new ReminderStatusSubMenu(id));
			menuManager.add(actions.deleteReminderAction());
			menuManager.add(timeFilterSubMenu);
			menuManager.add(actions.showOnlyOwnDueReminderToggleAction());
			menuManager.add(actions.showNotYetDueReminderToggleAction());
			menuManager.add(actions.showSelfCreatedReminderAction());
			menuManager.add(actions.showAssignedToMeAction());
			menuManager.add(actions.popupOnLoginReminderToggleAction());
			menuManager.add(actions.popupOnPatientSelectionReminderToggleAction());
			menuManager.add(actions.showOthersRemindersAction());
			menuManager.add(new Action(Messages.ReminderView_resetColumnOrder) {
				{
					setToolTipText(Messages.ReminderView_resetColumnOrderTooltip);
				}

				@Override
				public void run() {
					TableViewer viewer = entry.getKey();
					String cfg = id;
					Table table = viewer.getTable();
					ConfigServiceHolder.setUser("reminder.column.order." + cfg, null);
					int[] defaultOrder = IntStream.range(0, table.getColumnCount()).toArray();
					table.setColumnOrder(defaultOrder);
					String label = getColumnGroupLabel(cfg);
					SWTHelper.showInfo(Messages.ReminderView_resetColumnOrderDoneTitle,
							MessageFormat.format(Messages.ReminderView_resetColumnOrderDoneMessage, label));
				}
			});

			menuManager.addMenuListener(new IMenuListener() {
				@Override
				public void menuAboutToShow(IMenuManager manager) {
					actions.deleteReminderAction().setEnabled(actions.deleteReminderAction().isEnabled());
				}
			});

			table.setMenu(menuManager.createContextMenu(table));
		}

		refreshUserConfiguration();
		getSite().getPage().addPartListener(udpateOnVisible);
		applyColumnVisibilityToAllViewers();

	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
	}

	/**
	 * <p>
	 * Adds a scroll listener which only scrolls through the table if the mouse is
	 * hovered over a {@link TableItem}.
	 * </p>
	 * <p>
	 * If the mouse is not hovered over the {@link TableItem}, then it will not
	 * scroll through the table but instead the entire parent
	 * {@link ScrolledComposite}.
	 * </p>
	 * 
	 * @param tableViewer viewer to add the listener to.
	 */
	private void addModifiedScrollListener(Table table) {
		table.addListener(SWT.MouseWheel, e -> {
			Point mouseLocation = table.toControl(Display.getCurrent().getCursorLocation());
			if (table.getItem(mouseLocation) == null) {
				e.doit = false;
				ScrollBar verticalBar = viewersScrolledComposite.getVerticalBar();
				if (verticalBar != null && verticalBar.isEnabled()) {
					int newSelection = verticalBar.getSelection() - e.count * verticalBar.getIncrement();
					verticalBar.setSelection(newSelection);
					viewersScrolledComposite.setOrigin(viewersScrolledComposite.getOrigin().x, newSelection);
				}
			}
		});
	}

	/**
	 * <p>
	 * Setup the given {@link TableViewer}
	 * </p>
	 * <p>
	 * This method sets up the table header, layout, content provider, filters, and
	 * listeners for the {@link TableViewer}. It also enables resizing functionality
	 * and adds a scroll listener and focus listener to the underlying table.
	 * Additionally, this method creates and configures the required table columns.
	 * </p>
	 * 
	 * @param tableViewer
	 * @param columnIndex
	 */
	private void setupViewer(TableViewer tableViewer, boolean withPatientColumn) {
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 100;
		tableViewer.getTable().setLayoutData(gd);
		tableViewer.setComparator(new ReminderComparator());
		tableViewer.addFilter(filter);
		tableViewer.addSelectionChangedListener(getSelectionListener());
		tableViewer.addDoubleClickListener(getDoubleClickListener());
		columnFactory = new ReminderColumnFactory(boldFont);
		if (tableViewer == myViewer) {
			if (withPatientColumn) {
				columnFactory.createColumns(tableViewer, ReminderColumnType.TYPE, ReminderColumnType.DATE,
						ReminderColumnType.STATUS, ReminderColumnType.PATIENT, ReminderColumnType.DESCRIPTION);
			} else {
				columnFactory.createColumns(tableViewer, ReminderColumnType.TYPE, ReminderColumnType.DATE,
						ReminderColumnType.STATUS, ReminderColumnType.DESCRIPTION);
			}
		} else {
			if (withPatientColumn) {
				columnFactory.createColumns(tableViewer, ReminderColumnType.TYPE, ReminderColumnType.DATE,
						ReminderColumnType.RESPONSIBLE, ReminderColumnType.STATUS, ReminderColumnType.PATIENT,
						ReminderColumnType.DESCRIPTION);
			} else {
				columnFactory.createColumns(tableViewer, ReminderColumnType.TYPE, ReminderColumnType.DATE,
						ReminderColumnType.RESPONSIBLE, ReminderColumnType.STATUS, ReminderColumnType.DESCRIPTION);
			}
		}
		TableViewerResizer.enableResizing(tableViewer, viewersScrolledComposite);
		addModifiedScrollListener(tableViewer.getTable());
		tableViewer.getTable().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				tableViewer.getTable().deselectAll();
			}
		});
		Table table = tableViewer.getTable();
		for (TableColumn col : table.getColumns()) {
			col.setMoveable(true);
			col.addListener(SWT.Move, e -> {
				Display.getDefault().timerExec(300, () -> {
					if (!table.isDisposed()) {
						int[] order = table.getColumnOrder();
						String orderString = Arrays.stream(order).mapToObj(String::valueOf)
								.collect(Collectors.joining(","));
						ConfigServiceHolder.setUser("reminder.column.order." + allViewers.get(tableViewer),
								orderString);
					}
				});
			});
		}
		restoreColumnOrderAsync();
		applyColumnVisibilityToAllViewers();
	}

	private void restoreColumnOrderAsync() {
		Display.getDefault().asyncExec(() -> {
			for (Map.Entry<TableViewer, String> entry : allViewers.entrySet()) {
				TableViewer viewer = entry.getKey();
				String cfg = entry.getValue();
				Table tbl = viewer.getTable();
				if (tbl == null || tbl.isDisposed())
					continue;

				String orderString = ConfigServiceHolder.getUser("reminder.column.order." + cfg, null);
				if (orderString != null) {
					int[] order = Arrays.stream(orderString.split(",")).mapToInt(Integer::parseInt).toArray();
					try {
						tbl.setColumnOrder(order);
					} catch (IllegalArgumentException ex) {
						LoggerFactory.getLogger(getClass()).warn("Invalid column order for " + cfg, ex);
					}
				}
			}
		});
	}
	
	private void updateViewerSelection(StructuredSelection selection) {
		viewersParent.setRedraw(false);
		hideControl(currentPatientHeader);
		hideControl(currentPatientViewer.getTable());
		hideControl(generalPatientHeader);
		hideControl(generalPatientViewer.getTable());
		hideControl(generalRemindersHeader);
		hideControl(generalRemindersViewer.getTable());
		hideControl(myHeader);
		hideControl(myViewer.getTable());
		if (selection != null && !selection.isEmpty()) {
			for (Object selected : selection.toList()) {
				if (selected instanceof String) {
					if (SELECTIONCOMP_CURRENTPATIENT_ID.equals(selected)) {
						showControl(currentPatientHeader);
						showControl(currentPatientViewer.getTable());
					} else if (SELECTIONCOMP_GENERALPATIENT_ID.equals(selected)) {
						showControl(generalPatientHeader);
						showControl(generalPatientViewer.getTable());
					} else if (SELECTIONCOMP_GENERALREMINDERS_ID.equals(selected)) {
						showControl(generalRemindersHeader);
						showControl(generalRemindersViewer.getTable());
					} else if (SELECTIONCOMP_MYREMINDERS_ID.equals(selected)) {
						showControl(myHeader);
						showControl(myViewer.getTable());
					}
				}
			}
		}
		viewersParent.setRedraw(true);
		int width = viewersScrolledComposite.getClientArea().width;
		viewersScrolledComposite.setMinSize(viewersParent.computeSize(width, SWT.DEFAULT));
		viewParent.layout(true, true);
	}

	private void addDragSupport(TableViewer viewer) {
		DragSource dragSource = new DragSource(viewer.getTable(), DND.DROP_MOVE);
		dragSource.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });

		dragSource.addDragListener(new DragSourceAdapter() {
			@Override
			public void dragSetData(DragSourceEvent event) {
				LocalSelectionTransfer.getTransfer().setSelection(viewer.getStructuredSelection());
			}
		});
	}

	private void addDropSupport(TableViewer viewer, TableType tableType) {
		DropTarget dropTarget = new DropTarget(viewer.getTable(), DND.DROP_MOVE);
		dropTarget.setTransfer(new Transfer[] { LocalSelectionTransfer.getTransfer() });

		dropTarget.addDropListener(new DropTargetAdapter() {

			@Override
			public void drop(DropTargetEvent event) {
				ISelection selection = LocalSelectionTransfer.getTransfer().getSelection();
				if (selection instanceof IStructuredSelection) {
					for (Object element : ((IStructuredSelection) selection).toList()) {
						if (element instanceof IReminder) {
							IReminder reminder = (IReminder) element;
							updateReminderForTarget(reminder, tableType, viewer, actPatient);
							CoreModelServiceHolder.get().save(reminder);
							refresh();
						}
					}
				}
			}
		});
	}

	private void updateReminderForTarget(IReminder reminder, TableType type, TableViewer viewer, IPatient patient) {
		switch (type) {
		case CURRENT_PATIENT:
			// do nothing, it should not be possible to change the patient of a reminder.
			break;
		case GENERAL_PATIENT:
			if (reminder.isResponsibleAll()) {
				break;
			}
			String responsibleText = reminder.getResponsible().stream()
					.map(r -> r.getDescription1() + StringUtils.SPACE + r.getDescription2())
					.collect(Collectors.joining(", ")); //$NON-NLS-1$

			StringBuilder sb = new StringBuilder("Pendenz "); //$NON-NLS-1$
			if (!responsibleText.isEmpty()) {
				sb.append("von: ").append(responsibleText).append(" "); //$NON-NLS-1$ //$NON-NLS-2$
			}
			sb.append("an Alle zuweisen?"); //$NON-NLS-1$

			if (MessageDialog.openConfirm(viewParent.getShell(), "Pendenz: " + reminder.getSubject(), sb.toString())) { //$NON-NLS-1$
				if (!reminder.getResponsible().isEmpty()) {
					for (IContact contact : reminder.getResponsible()) {
						reminder.removeResponsible(contact);
					}
				}
				reminder.setResponsibleAll(true);
			}
			break;
		case GENERALREMINDERS:
			// do nothing
			break;
		case MYREMINDERS:
			if (reminder.isResponsibleAll()) {
				reminder.setResponsibleAll(false);
			}
			if (!reminder.getResponsible().isEmpty()) {
				for (IContact contact : reminder.getResponsible()) {
					reminder.removeResponsible(contact);
				}
			}
			reminder.addResponsible(ContextServiceHolder.getActiveMandatorOrNull());
			break;
		default:
			break;
		}
	}

	private TableViewer getViewerForId(String id) {
		return allViewers.entrySet().stream().filter(c -> c.getValue().equalsIgnoreCase(id)).findFirst()
				.orElse(null)
				.getKey();
	}

	private void showControl(Control control) {
		if (control != null && control.getLayoutData() != null) {
			control.setVisible(true);
			((GridData) control.getLayoutData()).exclude = false;
		}
	}

	private void hideControl(Control control) {
		if (control != null && control.getLayoutData() != null) {
			control.setVisible(false);
			((GridData) control.getLayoutData()).exclude = true;
		}
	}

	@Override
	public void setFocus() {
		viewersParent.setFocus();
	}

	private void refreshKeepLabels() {
		for (Entry<TableViewer, String> set : allViewers.entrySet()) {
			TableViewer viewer = set.getKey();
			if (viewer.getTable().isVisible()) {
				viewer.refresh(false);
			}
		}
	}

	@Override
	public void refresh() {
		Display.getDefault().asyncExec(() -> {
			if (!filtersMap.isEmpty()) {
				filtersMap.values().forEach(fa -> fa.reload());
			}
			patientRefresh();
			generalRefresh();
			generalRemindersRefresh();
			myRemindersRefresh();
			int width = viewersScrolledComposite.getClientArea().width;
			viewersScrolledComposite.setMinSize(viewersParent.computeSize(width, SWT.DEFAULT));
			viewParent.layout(true, true);
		});
	}

	private void patientRefresh() {
		if (actPatient != null) {
			if (currentPatientViewer.getTable().isVisible()) {
				refreshCurrentPatientInput(CURRENTPATIENT);
			}
		} else {
			currentPatientViewer.setInput(Collections.emptyList());
		}
	}

	private void generalRefresh() {
		if (generalPatientViewer.getTable().isVisible()) {
			refreshGeneralPatientInput(ALLPATIENTS);
		}
	}

	private void generalRemindersRefresh() {
		if (generalRemindersViewer.getTable().isVisible()) {
			refreshGeneralInput(GENERALREMINDERS);
		}
	}

	private void myRemindersRefresh() {
		if (myViewer.getTable().isVisible()) {
			refreshMyRemindersInput(MYREMINDERS);
		}
	}

	private void refreshCurrentPatientInput(String config) {
		refreshViewerInput(config, currentPatientViewer, SELECTIONCOMP_CURRENTPATIENT_ID, actPatient, false);
	}

	private void refreshGeneralPatientInput(String config) {
		refreshViewerInput(config, generalPatientViewer, SELECTIONCOMP_GENERALPATIENT_ID, null, false);
	}

	private void refreshGeneralInput(String config) {
		refreshViewerInput(config, generalRemindersViewer, SELECTIONCOMP_GENERALREMINDERS_ID, null, false);
	}

	private void refreshMyRemindersInput(String config) {
		refreshViewerInput(config, myViewer, SELECTIONCOMP_MYREMINDERS_ID, null, false);
	}
	
	/**
	 * Führt eine Reminder-Abfrage asynchron aus und aktualisiert den angegebenen
	 * Viewer.
	 */
	private void refreshViewerInput(String configKey, TableViewer viewer, String counterId, IPatient patient,
			boolean visibleOnly) {
		if (filtersMap == null || filtersMap.isEmpty()) {
			return;
		}

		if (visibleOnly && !viewer.getTable().isVisible()) {
	        return;
	    }

		var filter = filtersMap.get(configKey);


		ReminderQueryService.Config cfg = new ReminderQueryService.Config().showAll(false)
				.filterDue(filterDueDateDays > 0).dueInDays(filterDueDateDays).showOnlyDue(false)
				.showNotYetDueReminders(false).showSelfCreated(false).assignedToMe(false).popupOnLogin(false)
				.popupOnPatientSelection(false);


		if (filter.showOnlyOwnDueReminderToggleAction.isChecked()) {
			cfg.showOnlyDue(true);
		}
		if (filter.showNotYetDueReminderToggleAction.isChecked()) {
			cfg.showNotYetDueReminders(true);
		}
		if (filter.showSelfCreatedReminderAction.isChecked()) {
			cfg.showSelfCreated(true);
		}
		if (filter.showAssignedToMeAction.isChecked()) {
			cfg.assignedToMe(true);
		}
		if (filter.popupOnLoginReminderToggleAction.isChecked()) {
			cfg.popupOnLogin(true);
		}
		if (filter.popupOnPatientSelectionReminderToggleAction.isChecked()) {
			cfg.popupOnPatientSelection(true);
		}
		if (filter.showOthersRemindersAction().isChecked()) {
			cfg.showAll(true);
			cfg.showSelfCreated(false);
			cfg.assignedToMe(false);
		} else {
			cfg.showAll(false);
		}

		switch (configKey) {
		case CURRENTPATIENT:
			cfg.patient(patient);
			break;

		case ALLPATIENTS:
			cfg.patient(null);
			break;

		case GENERALREMINDERS:
			cfg.patient(null);
			cfg.noPatient(true);
			break;

		case MYREMINDERS:
			cfg.patient(null);
			cfg.assignedToMe(true);
			cfg.showSelfCreated(false);
			break;

		default:
			break;
		}

		CompletableFuture.supplyAsync(() -> new ReminderQueryService().load(cfg))
				.thenAcceptAsync(reminders -> Display.getDefault().asyncExec(() -> {
					if (viewer != null && !viewer.getTable().isDisposed()) {
						viewer.setInput(reminders);
						viewerSelectionComposite.setCount(counterId, viewer.getTable().getItemCount());
					}
				}));
	}
	
	private void refreshUserConfiguration() {
		// reload all filters
		filtersMap.values().forEach(fa -> fa.reload());
		applyColumnVisibilityToAllViewers();
	}

	private ISelectionChangedListener getSelectionListener() {
		return new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				ReminderListsView.this.selectionChanged(selection.toList());
				selectPatientAction.setEnabled(selection.size() <= 1);
				selectPatientAction.reflectRight();
				if (toggleAutoSelectPatientAction.isChecked() && selectPatientAction.isEnabled()) {
					selectPatientAction.doRun();
				}
			}
		};
	}

	private IDoubleClickListener getDoubleClickListener() {
		return new IDoubleClickListener() {

			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection != null && !selection.isEmpty()) {
					IReminder reminder = (IReminder) selection.getFirstElement();
					AcquireLockBlockingUi.aquireAndRun(reminder, new ILockHandler() {
						@Override
						public void lockAcquired() {
							ReminderDetailDialog rdd = new ReminderDetailDialog(UiDesk.getTopShell(),
									(Reminder) NoPoUtil.loadAsPersistentObject(reminder));
							int retVal = rdd.open();
							if (retVal == Dialog.OK) {
								ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, reminder);
							}
						}

						@Override
						public void lockFailed() {
							refreshKeepLabels();
						}
					});
				}
			}
		};
	}

	private static String SELECTIONCOMP_CURRENTPATIENT_ID = "reminderlistsview.selection.currentpatient"; //$NON-NLS-1$
	private static String SELECTIONCOMP_GENERALPATIENT_ID = "reminderlistsview.selection.generalpatient"; //$NON-NLS-1$
	private static String SELECTIONCOMP_GENERALREMINDERS_ID = "reminderlistsview.selection.generalreminders"; //$NON-NLS-1$
	private static String SELECTIONCOMP_MYREMINDERS_ID = "reminderlistsview.selection.myreminders"; //$NON-NLS-1$

	private class ViewerSelectionComposite extends Composite implements ISelectionProvider {

		private List<Action> currentSelection;

		private ListenerList<ISelectionChangedListener> selectionChangedListeners;

		private ToolBarManager manager;

		public ViewerSelectionComposite(Composite parent, int style) {
			super(parent, style);
			currentSelection = new ArrayList<>();
			selectionChangedListeners = new ListenerList<>();

			createContent();
		}

		public void setCount(String id, int itemCount) {
			for (IContributionItem item : manager.getItems()) {
				if (item.getId().equals(id)) {
					IAction action = ((ActionContributionItem) item).getAction();
					String text = action.getText();
					if (text.indexOf(" (") != -1) { //$NON-NLS-1$
						text = text.substring(0, text.indexOf(" (")); //$NON-NLS-1$
					}
					action.setText(text + " (" + itemCount + ")"); //$NON-NLS-1$ //$NON-NLS-2$
					item.update();
				}
			}
			manager.update(true);
			layout();
		}

		private void createContent() {
			setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			setLayout(new FillLayout());

			manager = new ToolBarManager(SWT.WRAP);
			manager.add(new Action("aktueller Patient", Action.AS_CHECK_BOX) {
				@Override
				public String getId() {
					return SELECTIONCOMP_CURRENTPATIENT_ID;
				}

				@Override
				public void run() {
					if (currentSelection.contains(this)) {
						currentSelection.remove(this);
					} else {
						currentSelection.add(this);
					}
					fireSelectionChanged();
					manager.update(true);
					saveSelection();
				}
			});
			manager.add(new Action("alle Patienten", Action.AS_CHECK_BOX) {
				@Override
				public String getId() {
					return SELECTIONCOMP_GENERALPATIENT_ID;
				}

				@Override
				public void run() {
					if (currentSelection.contains(this)) {
						currentSelection.remove(this);
					} else {
						currentSelection.add(this);
					}
					fireSelectionChanged();
					manager.update(true);
					saveSelection();
				}
			});
			manager.add(new Action("Pendenzen ohne Patientenbezug", Action.AS_CHECK_BOX) {
				@Override
				public String getId() {
					return SELECTIONCOMP_GENERALREMINDERS_ID;
				}

				@Override
				public void run() {
					if (currentSelection.contains(this)) {
						currentSelection.remove(this);
					} else {
						currentSelection.add(this);
					}
					fireSelectionChanged();
					manager.update(true);
					saveSelection();
				}
			});
			manager.add(new Action("meine Pendenzen", Action.AS_CHECK_BOX) {

				@Override
				public String getId() {
					return SELECTIONCOMP_MYREMINDERS_ID;
				}

				@Override
				public void run() {
					if (currentSelection.contains(this)) {
						currentSelection.remove(this);
					} else {
						currentSelection.add(this);
					}
					fireSelectionChanged();
					manager.update(true);
					saveSelection();
				}
			});
			manager.createControl(this);
		}

		private void saveSelection() {
			List<String> selectedIds = currentSelection.stream().map(action -> action.getId())
					.collect(Collectors.toList());
			StringJoiner sj = new StringJoiner(","); //$NON-NLS-1$
			selectedIds.forEach(id -> sj.add(id));
			ConfigServiceHolder.setUser(Preferences.USR_REMINDER_VIEWER_SELECTION, sj.toString());
		}

		public void loadSelection() {
			currentSelection.clear();
			String[] loadedIds = ConfigServiceHolder
					.getUser(Preferences.USR_REMINDER_VIEWER_SELECTION, StringUtils.EMPTY).split(","); //$NON-NLS-1$
			for (String id : loadedIds) {
				for (IContributionItem item : manager.getItems()) {
					if (item.getId().equals(id)) {
						IAction action = ((ActionContributionItem) item).getAction();
						action.setChecked(true);
						currentSelection.add((Action) action);
					}
				}
				fireSelectionChanged();
				manager.update(true);
				layout();
			}
		}

		@Override
		public void addSelectionChangedListener(ISelectionChangedListener listener) {
			selectionChangedListeners.add(listener);
		}

		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener) {
			selectionChangedListeners.remove(listener);
		}

		@Override
		public ISelection getSelection() {
			return new StructuredSelection(
					currentSelection.stream().map(action -> action.getId()).collect(Collectors.toList()));
		}

		@Override
		public void setSelection(ISelection selection) {
			// ignore until needed
		}

		private void fireSelectionChanged() {
			ISelection selection = getSelection();
			for (ISelectionChangedListener listener : selectionChangedListeners) {
				SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
				listener.selectionChanged(event);
			}
		}
	}

	private class FilterTimeAction extends Action {

		private List<Action> others;
		private int days;

		public FilterTimeAction(int days) {
			super(MessageFormat.format(Messages.ReminderView_nextDays, days), Action.AS_CHECK_BOX);
			this.days = days;
			if (filterDueDateDays == days) {
				setChecked(true);
			}
		}

		public void setOthers(List<Action> list) {
			this.others = list;
		}

		@Override
		public void run() {
			if (isChecked()) {
				ConfigServiceHolder.setUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS + "/" + GLOBALFILTERS, days);
				filterDueDateDays = days;
				clearSelection();
				refresh();
			} else {
				ConfigServiceHolder.setUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS + "/" + GLOBALFILTERS, -1);
				filterDueDateDays = -1;
				clearSelection();
				refresh();
			}

			if (others != null) {
				for (Action other : others) {
					other.setChecked(false);
				}
			}
		}
	}

	private class CustomTimeAction extends Action {

		private List<Action> others;
		private String id;

		public CustomTimeAction(String text, String id) {
			super(text, Action.AS_CHECK_BOX);
			this.id = id;
			List<Integer> list = List.of(-1, 30, 60, 90);
			setChecked(!list.contains(filterDueDateDays));
		}

		public void setOthers(List<Action> list) {
			this.others = list;
		}

		@Override
		public void run() {
			if (isChecked()) {
				CustomTimePopupDialog dialog = new CustomTimePopupDialog();
				if (dialog.open() == Dialog.OK) {
					int days = dialog.getSelectedDays();
					filterDueDateDays = days;
					if (useGlobalFilters) {
						ConfigServiceHolder.setUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS + "/" + GLOBALFILTERS, //$NON-NLS-1$
								days);
					} else {
						ConfigServiceHolder.setUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS + "/" + id, days); //$NON-NLS-1$
					}
				}
			} else {
				filterDueDateDays = -1;
				if (useGlobalFilters) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS + "/" + GLOBALFILTERS, -1); //$NON-NLS-1$
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS + "/" + id, -1); //$NON-NLS-1$
				}
			}
			clearSelection();
			refresh();

			if (others != null) {
				for (Action other : others) {
					other.setChecked(false);
				}
			}
		}
	}

	private class CustomTimePopupDialog extends Dialog {

		LocalDate today = LocalDate.now();
		private int selectedDays = 0;
		private String title = "Nächste %s Tage";

		public CustomTimePopupDialog() {
			super(getViewSite().getShell());
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			updateTitle(parent);
			Composite area = (Composite) super.createDialogArea(parent);
			area.setLayout(new GridLayout(1, false));

			DateTime calendar = new DateTime(area, SWT.CALENDAR | SWT.BORDER);
			calendar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			calendar.addListener(SWT.Selection, e -> {
				LocalDate selectedDate = LocalDate.of(calendar.getYear(), calendar.getMonth() + 1, calendar.getDay());
				if (selectedDate.isBefore(today)) {
					calendar.setDate(today.getYear(), today.getMonthValue() - 1, today.getDayOfMonth());
				} else {
					selectedDays = (int) ChronoUnit.DAYS.between(today, selectedDate);
					updateTitle(parent);
				}
			});

			return area;
		}

		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
			createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		}

		public int getSelectedDays() {
			return selectedDays;
		}

		private void updateTitle(Composite parent) {
			parent.getShell().setText(String.format(title, selectedDays));
		}
	}

	private class ReminderStatusSubMenu extends MenuManager {

		private TableViewer viewer;

		public ReminderStatusSubMenu(String id) {
			super(Messages.ReminderView_statusMenu);
			viewer = getViewerForId(id);
			setRemoveAllWhenShown(true);
			addMenuListener(new ReminderStatusSubMenuListener());
		}

		private class ReminderStatusSubMenuListener implements IMenuListener {

			@Override
			public void menuAboutToShow(IMenuManager manager) {
				StructuredSelection selection = (StructuredSelection) viewer.getStructuredSelection();
				if (selection != null && selection.size() == 1) {
					if (selection.getFirstElement() instanceof IReminder) {
						IReminder reminder = (IReminder) selection.getFirstElement();
						manager.add(new StatusAction(ProcessStatus.OPEN, reminder));
						manager.add(new StatusAction(ProcessStatus.IN_PROGRESS, reminder));
						manager.add(new StatusAction(ProcessStatus.CLOSED, reminder));
						manager.add(new StatusAction(ProcessStatus.ON_HOLD, reminder));
					}
				} else {
					manager.add(new Action("Multiple selection") {
						@Override
						public boolean isEnabled() {
							return false;
						}
					});
				}
			}

			private class StatusAction extends LockRequestingAction<IReminder> {

				private final ProcessStatus representedStatus;
				private final IReminder reminder;

				public StatusAction(ProcessStatus representedStatus, IReminder reminder) {
					super(representedStatus.getLocaleText(), SWT.RADIO);
					this.representedStatus = representedStatus;
					this.reminder = reminder;

					setChecked(isRepresentedStatusCurrent());
				}

				@Override
				public boolean isChecked() {
					return isRepresentedStatusCurrent();
				}

				private boolean isRepresentedStatusCurrent() {
					ProcessStatus status = reminder.getStatus();
					if (status == ProcessStatus.OPEN && isDueOrOverdue(reminder)) {
						return representedStatus == ProcessStatus.OPEN;
					}

					return representedStatus == status;
				}

				@Override
				public String getText() {
					String text = super.getText();
					if (representedStatus == ProcessStatus.OPEN && isDueOrOverdue(reminder)) {
						LocalDate due = reminder.getDue();
						if (due != null) {
							LocalDate now = LocalDate.now();
							if (due.isBefore(now)) {
								return text + " (" + Messages.ProcessStatus_OVERDUE + ")";
							} else if (due.isEqual(now)) {
								return text + " (" + Messages.ProcessStatus_DUE + StringUtils.SPACE
										+ Messages.Core_today + ")";
							}
						}
					}
					return text;
				}

				private boolean isDueOrOverdue(IReminder reminder) {
					LocalDate due = reminder.getDue();
					if (due == null)
						return false;
					LocalDate now = LocalDate.now();
					return due.isBefore(now) || due.isEqual(now);
				}

				@Override
				public IReminder getTargetedObject() {
					return reminder;
				}

				@Override
				public void doRun(IReminder element) {
					element.setStatus(representedStatus);
					CoreModelServiceHolder.get().save(element);
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, element);
				}
			}
		}
	}

	private class HeaderComposite extends Composite {

		private Label header;
		private ToolBarManager toolbarManager;

		public HeaderComposite(Composite parent, int style) {
			super(parent, style);
			setBackground(parent.getBackground());
			GridLayout layout = new GridLayout();
			layout.horizontalSpacing = 0;
			layout.verticalSpacing = 0;
			layout.marginHeight = 0;
			layout.marginWidth = 0;
			setLayout(layout);

			header = new Label(this, SWT.NONE);
			header.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
			header.setBackground(getBackground());

			toolbarManager = new ToolBarManager();
			ToolBar toolbar = toolbarManager.createControl(this);
			toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			toolbar.setBackground(getBackground());
		}

		public void setTextFont(Font font) {
			header.setFont(font);
		}

		public void setText(String text) {
			header.setText(text);
			ReminderListsView.this.viewParent.layout(true, true);
		}
	}

	private class TableViewerResizer {
		private static int lastY = 0;
		private static int newY = 0;
		private static int tolerance = 15;
		private static int minHeight = 25;

		public static void enableResizing(TableViewer tableViewer, ScrolledComposite scrolledComposite) {
			Table table = tableViewer.getTable();
			Composite parent = table.getParent();

			table.addMouseMoveListener(new MouseMoveListener() {
				@Override
				public void mouseMove(MouseEvent e) {
					if (isNearBottomEdge(table, e.y)) {
						table.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
					} else if (e.stateMask != SWT.BUTTON1 && table.getCursor() != null) {
						table.setCursor(null);
					}
					if (e.stateMask == SWT.BUTTON1 && lastY != 0) {
						newY = e.y;
					}
				}
			});
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDown(MouseEvent e) {
					if (isNearBottomEdge(table, e.y)) {
						lastY = e.y;
						table.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
					}
				}
				@Override
				public void mouseUp(MouseEvent e) {
					if (lastY != 0 && lastY != newY) {
						int deltaY = newY - lastY;
						GridData gd = (GridData) table.getLayoutData();
						if (deltaY != 0) {
							int newHeight = gd.heightHint + deltaY;
							if (newHeight > minHeight) {
								gd.heightHint = newHeight;
								table.setLayoutData(gd);
								lastY += deltaY;
								table.getParent().layout(true, true);
									Point newSize = table.getParent().computeSize(SWT.DEFAULT, SWT.DEFAULT);
									scrolledComposite.setMinSize(newSize.x,
											Math.max(newSize.y, scrolledComposite.getClientArea().height));
									scrolledComposite.layout(true, true);
							}
						}
					}
					lastY = 0;
				}
			});
		}

		private static boolean isNearBottomEdge(Table table, int y) {
			int tableHeight = table.getBounds().height;
			if (table.getHorizontalBar().isVisible()) {
				tableHeight -= table.getHorizontalBar().getSize().y;
			}
			return y >= tableHeight - tolerance && y <= tableHeight + tolerance;
		}
	}

	private enum TableType {
		CURRENT_PATIENT, GENERAL_PATIENT, GENERALREMINDERS, MYREMINDERS, GROUP
	}

	@Override
	public void heartbeat() {
		long highestLastUpdate = CoreModelServiceHolder.get().getHighestLastUpdate(IReminder.class);
		if (highestLastUpdate > cvHighestLastUpdate) {
			refresh();
			cvHighestLastUpdate = highestLastUpdate;
		}
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionChangedListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		return new StructuredSelection(currentSelection);
	}

	@Override
	public void setSelection(ISelection arg0) {
		// currently not supported
	}

	private List<IUserGroup> getUserGroups() {
		List<IUserGroup> userGroups = CoreModelServiceHolder.get().getQuery(IUserGroup.class).execute();
		userGroups.sort((u1, u2) -> u1.getLabel().compareTo(u2.getLabel()));
		List<IUserGroup> filtered = new ArrayList<>();
		for (IUserGroup group : userGroups) {
			if (group.getUsers().contains(ContextServiceHolder.get().getActiveUser().get())) {
				filtered.add(group);
			}
		}
		return filtered;
	}

	private void clearSelection() {
		StructuredSelection clear = new StructuredSelection();
		for (Entry<TableViewer, String> set : allViewers.entrySet()) {
			TableViewer viewer = set.getKey();
			viewer.setSelection(clear);
		}
	}

	private void selectionChanged(List<IReminder> list) {
		currentSelection.clear();
		currentSelection.addAll(list);
		fireSelectionChanged();
	}

	private void fireSelectionChanged() {
		ISelection selection = getSelection();
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
			listener.selectionChanged(event);
		}
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	private void applyColumnVisibilityToAllViewers() {
		allViewers.keySet().forEach(v -> {
			if (v != null && !v.getTable().isDisposed()) {
				v.refresh();
			}
		});
	}

	private String getColumnGroupLabel(String cfg) {
		return switch (cfg) {
		case CURRENTPATIENT -> Messages.ReminderView_currentPatient;
		case ALLPATIENTS -> Messages.ReminderView_allPatients;
		case GENERALREMINDERS -> Messages.ReminderView_generalReminders;
		case MYREMINDERS -> Messages.ReminderView_myReminders;
		default -> cfg;
		};
	}
}
