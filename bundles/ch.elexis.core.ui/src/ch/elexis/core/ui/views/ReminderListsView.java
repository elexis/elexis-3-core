package ch.elexis.core.ui.views;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
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
import ch.elexis.core.constants.StringConstants;
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
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.model.issue.Visibility;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.ISubQuery;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
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
import ch.elexis.data.Reminder;

/**
 * 
 */
public class ReminderListsView extends ViewPart implements HeartListener, IRefreshable, ISelectionProvider {
	public static final String ID = "ch.elexis.core.ui.views.reminderlistsview"; //$NON-NLS-1$

	private final static String CURRENTPATIENT = "currentpatient"; //$NON-NLS-1$
	private final static String ALLPATIENTS = "allpatients"; //$NON-NLS-1$
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
	private HeaderComposite myHeader;
	private TableViewer myViewer;

	HashMap<TableViewer, String> allViewers = new HashMap<>();

	record GroupComponent(String id, HeaderComposite header, TableViewer viewer) {
	}
	private List<GroupComponent> usergroupComponents = new ArrayList<>();
	private List<IUserGroup> userGroups = getUserGroups();

	private Font boldFont;
	private Color colorInProgress;
	private Color colorDue;
	private Color colorOverdue;
	private Color colorOpen;

	private List<IReminder> currentSelection = new ArrayList<>();
	private ListenerList<ISelectionChangedListener> selectionChangedListeners = new ListenerList<>();

	private IPatient actPatient;
	private long cvHighestLastUpdate;

	private record FilterActions(Action deleteReminderAction, Action showAssignedToMeAction,
			Action popupOnPatientSelectionReminderToggleAction, Action popupOnLoginReminderToggleAction,
			Action showNotYetDueReminderToggleAction, Action showOnlyOwnDueReminderToggleAction,
			Action showSelfCreatedReminderAction, RestrictedAction showOthersRemindersAction) {
		public void reload() {
			for (Object action : new Object[] { showAssignedToMeAction,
					popupOnPatientSelectionReminderToggleAction, popupOnLoginReminderToggleAction,
					showNotYetDueReminderToggleAction, showOnlyOwnDueReminderToggleAction,
					showSelfCreatedReminderAction, showOthersRemindersAction }) {
				try {
					Method refreshMethod = action.getClass().getMethod("reload"); //$NON-NLS-1$
					refreshMethod.invoke(action);
				} catch (Exception e) {
					LoggerFactory.getLogger(getClass()).error("Error reloading filters", e);
				}
			}
		}

		public void reset() {
			for (Object action : new Object[] { showAssignedToMeAction,
					popupOnPatientSelectionReminderToggleAction, popupOnLoginReminderToggleAction,
					showNotYetDueReminderToggleAction, showOnlyOwnDueReminderToggleAction,
					showSelfCreatedReminderAction }) {
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
			ConfigServiceHolder.setUser(Preferences.USR_REMINDER_USE_GLOBAL_FILTERS, //$NON-NLS-1$
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
			ConfigServiceHolder.setUser(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, //$NON-NLS-1$
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
				Messages.Core_All,
				Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.ReminderView_foreignTooltip);
			}

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

		Action showSelfCreatedReminderAction = new Action(Messages.ReminderView_myRemindersAction,Action.AS_CHECK_BOX){ // $NON-NLS-1$
			{
				setToolTipText(Messages.ReminderView_myRemindersToolTip); // $NON-NLS-1$
			}

			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDEROWN + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.USR_REMINDEROWN + "/" + config, false)); //$NON-NLS-1$
				}
			}

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

		Action showOnlyOwnDueReminderToggleAction = new Action(Messages.ReminderView_onlyDueAction,Action.AS_CHECK_BOX){ // $NON-NLS-1$
			{
				setToolTipText(Messages.ReminderView_onlyDueToolTip); // $NON-NLS-1$
			}

			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDERSOPEN + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.USR_REMINDERSOPEN + "/" + config, false)); //$NON-NLS-1$
				}
			}

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

		Action showNotYetDueReminderToggleAction = new Action(Messages.ShowNotYetDueReminders,Action.AS_CHECK_BOX){ // $NON-NLS-1$
			{
				setToolTipText(Messages.ShowNotYetDueReminders_Tooltip); // $NON-NLS-1$
			}

			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDERS_NOT_YET_DUE + "/" + config, false)); //$NON-NLS-1$
				}
			}

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

		Action popupOnLoginReminderToggleAction = new Action(Messages.Reminders_PopupOnLogin,Action.AS_CHECK_BOX){
			{
				setToolTipText(Messages.Reminders_PopupOnLogin_ToolTip); // $NON-NLS-1$
			}

			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.POPUP_ON_LOGIN + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(ConfigServiceHolder.getUser(Preferences.POPUP_ON_LOGIN + "/" + config, false)); //$NON-NLS-1$
				}
			}

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

			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.POPUP_ON_PATIENT_SELECTION + "/" + config, false)); //$NON-NLS-1$
				}
			}

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

			public void reload() {
				if (useGlobalFilters) {
					this.setChecked(ConfigServiceHolder
							.getUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + GLOBALFILTERS, false)); //$NON-NLS-1$
				} else {
					this.setChecked(
							ConfigServiceHolder.getUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + config, false)); //$NON-NLS-1$
				}
			}

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
				if (useGlobalFilters) {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + GLOBALFILTERS, //$NON-NLS-1$
							this.isChecked());
				} else {
					ConfigServiceHolder.setUser(Preferences.USR_REMINDER_ASSIGNED_TO_ME + "/" + config, //$NON-NLS-1$
							this.isChecked());
				}
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
		currentPatientHeader.setText("aktueller Patient");
		currentPatientViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(currentPatientViewer, 3);
		addDragSupport(currentPatientViewer);
		addDropSupport(currentPatientViewer, TableType.CURRENT_PATIENT);

		generalPatientHeader = new HeaderComposite(viewersParent, SWT.NONE);
		generalPatientHeader.setTextFont(boldFont);
		generalPatientHeader.setText("alle Patienten");
		generalPatientViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(generalPatientViewer, 4);
		addDragSupport(generalPatientViewer);
		addDropSupport(generalPatientViewer, TableType.GENERAL_PATIENT);
		((GridData) generalPatientViewer.getTable().getLayoutData()).heightHint = 300;

		myHeader = new HeaderComposite(viewersParent, SWT.NONE);
		myHeader.setTextFont(boldFont);
		myHeader.setText("meine Pendenzen");
		myViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(myViewer, 3);
		addDragSupport(myViewer);
		addDropSupport(myViewer, TableType.MYREMINDERS);

		for (IUserGroup group : userGroups) {
			HeaderComposite groupHeader = new HeaderComposite(viewersParent, SWT.NONE);
			groupHeader.setTextFont(boldFont);
			groupHeader.setText(group.getId() + "-Pendenzen");
			TableViewer groupViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
			setupViewer(groupViewer, 3);
			addDragSupport(groupViewer);
			addDropSupport(groupViewer, TableType.GROUP);
			usergroupComponents.add(new GroupComponent(group.getId(), groupHeader, groupViewer));
		}

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
		allViewers.put(myViewer, MYREMINDERS);

		for (GroupComponent group : usergroupComponents) {
			allViewers.put(group.viewer(), group.id());
		}

		for (Map.Entry<TableViewer, String> entry : allViewers.entrySet()) {
			String id = entry.getValue();
			Table table = entry.getKey().getTable();

			FilterActions actions = createFilterActions(id);
			filtersMap.put(id, actions);

			MenuManager timeFilterSubMenu = new MenuManager("Zeitraum Anzeige (ab heute)");
			CustomTimeAction custom = new CustomTimeAction("Benutzerdefinierte Anzahl", id);
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
	private void setupViewer(TableViewer tableViewer, int columnIndex) {
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 100;
		tableViewer.getTable().setLayoutData(gd);
		tableViewer.setComparator(new ReminderComparator());
		tableViewer.addFilter(filter);
		tableViewer.addSelectionChangedListener(getSelectionListener());
		tableViewer.addDoubleClickListener(getDoubleClickListener());
		createTypeColumn(tableViewer, 20, 0);
		createDateColumn(tableViewer, 80, 1);
		createResponsibleColumn(tableViewer, 80, 2);
		createPatientColumn(tableViewer, 150, 3);
		createDescriptionColumn(tableViewer, 400, columnIndex);
		TableViewerResizer.enableResizing(tableViewer, viewersScrolledComposite);
		addModifiedScrollListener(tableViewer.getTable());
		tableViewer.getTable().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				tableViewer.getTable().deselectAll();
			}
		});
	}

	private void updateViewerSelection(StructuredSelection selection) {
		viewersParent.setRedraw(false);
		hideControl(currentPatientHeader);
		hideControl(currentPatientViewer.getTable());
		hideControl(generalPatientHeader);
		hideControl(generalPatientViewer.getTable());
		hideControl(myHeader);
		hideControl(myViewer.getTable());
		for (GroupComponent group : usergroupComponents) {
			hideControl(group.header());
			hideControl(group.viewer().getTable());
		}
		if (selection != null && !selection.isEmpty()) {
			for (Object selected : selection.toList()) {
				if (selected instanceof String) {
					if (SELECTIONCOMP_CURRENTPATIENT_ID.equals(selected)) {
						showControl(currentPatientHeader);
						showControl(currentPatientViewer.getTable());
					} else if (SELECTIONCOMP_GENERALPATIENT_ID.equals(selected)) {
						showControl(generalPatientHeader);
						showControl(generalPatientViewer.getTable());
					} else if (SELECTIONCOMP_MYREMINDERS_ID.equals(selected)) {
						showControl(myHeader);
						showControl(myViewer.getTable());
					} else if (!userGroups.isEmpty()) {
						for (GroupComponent group : usergroupComponents) {
							if ((SELECTIONCOMP_GROUPREMINDERS_PREFIX + group.id()).equals(selected)) {
								showControl(group.header());
								showControl(group.viewer().getTable());
							}
						}
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
			if (!reminder.getResponsible().isEmpty()) {
				for (IContact contact : reminder.getResponsible()) {
					reminder.removeResponsible(contact);
				}
			}
			reminder.setResponsibleAll(true);
			break;
		case GROUP:
			IUserGroup targetGroup = findGroupForViewer(viewer);
			if (targetGroup != null) {
				List<IContact> currentResponsibles = reminder.getResponsible();
				if (reminder.isResponsibleAll()) {
					reminder.setResponsibleAll(false);
				}
				if (!currentResponsibles.isEmpty()) {
					for (IContact contact : currentResponsibles) {
						reminder.removeResponsible(contact);
					}
				}
				for (IUser user : targetGroup.getUsers()) {
					reminder.addResponsible(user.getAssignedContact());
				}
			}
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

	/**
	 * Find and return the {@link IUserGroup} which has the same {@link TableViewer}
	 * as the given one.
	 * 
	 * @param viewer
	 * @return IUserGroup
	 */
	private IUserGroup findGroupForViewer(TableViewer viewer) {
		return usergroupComponents.stream().filter(component -> component.viewer().equals(viewer))
				.map(component -> getUserGroupById(component.id()))
				.findFirst().orElse(null);
	}

	/**
	 * Find and return the {@link IUserGroup} which has the same id as the given
	 * one.
	 * 
	 * @param id
	 * @return IUserGroup
	 */
	private IUserGroup getUserGroupById(String id) {
		return userGroups.stream().filter(group -> group.getId().equals(id)).findFirst().orElse(null);
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
		if (generalPatientViewer.getTable().isVisible()) {
			generalPatientViewer.refresh(false);
		}
		if (currentPatientViewer.getTable().isVisible()) {
			currentPatientViewer.refresh(false);
		}
		if (myViewer.getTable().isVisible()) {
			myViewer.refresh(false);
		}
		for (GroupComponent group : usergroupComponents) {
			if (group.viewer().getTable().isVisible()) {
				group.viewer().refresh(false);
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
			myRemindersRefresh();
			groupRemindersRefresh();
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

	private void myRemindersRefresh() {
		if (myViewer.getTable().isVisible()) {
			refreshMyRemindersInput(MYREMINDERS);
		}
	}
	
	private void groupRemindersRefresh() {
		for (GroupComponent group : usergroupComponents) {
			if (group.viewer().getTable().isVisible()) {
				refreshGroupRemindersInput(group);
			}
		}
	}

	private void refreshCurrentPatientInput(String config) {
		if (filtersMap != null || filtersMap.isEmpty()) {
			CompletableFuture<List<IReminder>> currentLoader = CompletableFuture
					.supplyAsync(new CurrentPatientSupplier(actPatient)
							.showAll(ConfigServiceHolder.getUser(Preferences.USR_REMINDEROTHERS, false)
									&& AccessControlServiceHolder.get().evaluate(EvACE.of(IReminder.class, Right.VIEW)))
							.filterDue(filterDueDateDays != -1)
							.showOnlyDue(filtersMap.get(config).showOnlyOwnDueReminderToggleAction.isChecked())
							.showNotYetDueReminders(
									filtersMap.get(config).showNotYetDueReminderToggleAction.isChecked())
							.showSelfCreated(filtersMap.get(config).showSelfCreatedReminderAction.isChecked())
							.popupOnLogin(filtersMap.get(config).popupOnLoginReminderToggleAction.isChecked())
							.popupOnPatientSelectionToggleAction(
									filtersMap.get(config).popupOnPatientSelectionReminderToggleAction.isChecked())
							.showAssignedToMeAction(filtersMap.get(config).showAssignedToMeAction.isChecked()));
			currentLoader.thenRunAsync(() -> {
				Display.getDefault().asyncExec(() -> {
					if (currentPatientViewer != null && !currentPatientViewer.getTable().isDisposed()) {
						List<IReminder> input;
						try {
							input = currentLoader.get();
							currentPatientViewer.setInput(input);
							viewerSelectionComposite.setCount(SELECTIONCOMP_CURRENTPATIENT_ID,
									currentPatientViewer.getTable().getItemCount());
						} catch (InterruptedException | ExecutionException e) {
							LoggerFactory.getLogger(getClass()).error("Error loading reminders", e);
						}

					}
				});
			});
		}
	}

	private void refreshGeneralPatientInput(String config) {
		if (filtersMap != null || filtersMap.isEmpty()) {
			CompletableFuture<List<IReminder>> currentLoader = CompletableFuture
					.supplyAsync(new GeneralPatientSupplier(actPatient)
							.showAll(ConfigServiceHolder.getUser(Preferences.USR_REMINDEROTHERS, false)
									&& AccessControlServiceHolder.get().evaluate(EvACE.of(IReminder.class, Right.VIEW)))
							.filterDue(filterDueDateDays != -1)
							.showOnlyDue(filtersMap.get(config).showOnlyOwnDueReminderToggleAction.isChecked())
							.showNotYetDueReminders(
									filtersMap.get(config).showNotYetDueReminderToggleAction.isChecked())
							.showSelfCreated(filtersMap.get(config).showSelfCreatedReminderAction.isChecked())
							.popupOnLoginReminderToggleAction(
									filtersMap.get(config).popupOnLoginReminderToggleAction.isChecked())
							.popupOnPatientSelectionToggleAction(
									filtersMap.get(config).popupOnPatientSelectionReminderToggleAction.isChecked())
							.showAssignedToMeAction(filtersMap.get(config).showAssignedToMeAction.isChecked()));
			currentLoader.thenRunAsync(() -> {
				Display.getDefault().asyncExec(() -> {
					if (generalPatientViewer != null && !generalPatientViewer.getTable().isDisposed()) {
						List<IReminder> input;
						try {
							input = currentLoader.get();
							generalPatientViewer.setInput(input);
							viewerSelectionComposite.setCount(SELECTIONCOMP_GENERALPATIENT_ID,
									generalPatientViewer.getTable().getItemCount());
						} catch (InterruptedException | ExecutionException e) {
							LoggerFactory.getLogger(getClass()).error("Error loading reminders", e);
						}

					}
				});
			});
		}
	}

	private void refreshMyRemindersInput(String config) {
		if (filtersMap != null || filtersMap.isEmpty()) {
			CompletableFuture<List<IReminder>> currentLoader = CompletableFuture.supplyAsync(new MyRemindersSupplier()
					.showAll(ConfigServiceHolder.getUser(Preferences.USR_REMINDEROTHERS, false)
							&& AccessControlServiceHolder.get().evaluate(EvACE.of(IReminder.class, Right.VIEW)))
					.filterDue(filterDueDateDays != -1)
					.showOnlyDue(filtersMap.get(config).showOnlyOwnDueReminderToggleAction.isChecked())
					.showNotYetDueReminders(filtersMap.get(config).showNotYetDueReminderToggleAction.isChecked())
					.showSelfCreated(filtersMap.get(config).showSelfCreatedReminderAction.isChecked())
					.popupOnLogin(filtersMap.get(config).popupOnLoginReminderToggleAction.isChecked())
					.popupOnPatientSelectionToggleAction(
							filtersMap.get(config).popupOnPatientSelectionReminderToggleAction.isChecked())
					.showAssignedToMeAction(filtersMap.get(config).showAssignedToMeAction.isChecked()));
			currentLoader.thenRunAsync(() -> {
				Display.getDefault().asyncExec(() -> {
					if (myViewer != null && !myViewer.getTable().isDisposed()) {
						List<IReminder> input;
						try {
							input = currentLoader.get();
							myViewer.setInput(input);
							viewerSelectionComposite.setCount(SELECTIONCOMP_MYREMINDERS_ID,
									myViewer.getTable().getItemCount());
						} catch (InterruptedException | ExecutionException e) {
							LoggerFactory.getLogger(getClass()).error("Error loading reminders", e);
						}

					}
				});
			});
		}
	}

	private void refreshGroupRemindersInput(GroupComponent group) {
		if (filtersMap != null || filtersMap.isEmpty()) {
			if (group.viewer().getTable().isVisible()) {
				CompletableFuture<List<IReminder>> currentLoader = CompletableFuture
						.supplyAsync(new GroupRemindersSupplier(group.id())
								.showAll(ConfigServiceHolder.getUser(Preferences.USR_REMINDEROTHERS, false)
										&& AccessControlServiceHolder.get()
												.evaluate(EvACE.of(IReminder.class, Right.VIEW)))
								.filterDue(filterDueDateDays != -1)
								.showOnlyDue(filtersMap.get(group.id()).showOnlyOwnDueReminderToggleAction.isChecked())
								.showNotYetDueReminders(
										filtersMap.get(group.id()).showNotYetDueReminderToggleAction.isChecked())
								.showSelfCreated(filtersMap.get(group.id()).showSelfCreatedReminderAction.isChecked())
								.popupOnLogin(filtersMap.get(group.id()).popupOnLoginReminderToggleAction.isChecked())
								.popupOnPatientSelectionToggleAction(
										filtersMap.get(group.id()).popupOnPatientSelectionReminderToggleAction
												.isChecked())
								.showAssignedToMeAction(filtersMap.get(group.id()).showAssignedToMeAction.isChecked()));
				currentLoader.thenRunAsync(() -> {
					Display.getDefault().asyncExec(() -> {
						if (group.viewer() != null && !group.viewer().getTable().isDisposed()) {
							List<IReminder> input;
							try {
								input = currentLoader.get();
								group.viewer().setInput(input);
								viewerSelectionComposite.setCount(SELECTIONCOMP_GROUPREMINDERS_PREFIX + group.id(),
										group.viewer().getTable().getItemCount());
							} catch (InterruptedException | ExecutionException e) {
								LoggerFactory.getLogger(getClass()).error("Error loading reminders", e);
							}
						}
					});
				});
			}
		}
	}
	
	private void applyDueDateFilter(IQuery<IReminder> query, boolean includeNoDue) {
	    LocalDate now = LocalDate.now();
		LocalDate dueDateDays = now.plusDays(filterDueDateDays);
	    if (!includeNoDue) {
	        query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.NOT_EQUALS, null);
	    }
		query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.GREATER_OR_EQUAL, now);
	    query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.LESS_OR_EQUAL, dueDateDays);
	}

	private void refreshUserConfiguration() {
		// reload all filters
		filtersMap.values().forEach(fa -> fa.reload());

//		// get state from user's configuration
//		showOthersRemindersAction.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDEROTHERS, false));
//
//		// update action's access rights
//		showOthersRemindersAction.reflectRight();

		colorInProgress = UiDesk.getColorFromRGB(ConfigServiceHolder
				.getUser(Preferences.USR_REMINDERCOLORS + "/" + ProcessStatus.IN_PROGRESS.name(), "FFFFFF")); //$NON-NLS-1$ ;
		colorDue = UiDesk.getColorFromRGB(
				ConfigServiceHolder.getUser(Preferences.USR_REMINDERCOLORS + "/" + ProcessStatus.DUE.name(), "FFFFFF")); //$NON-NLS-1$ ;
		colorOverdue = UiDesk.getColorFromRGB(ConfigServiceHolder
				.getUser(Preferences.USR_REMINDERCOLORS + "/" + ProcessStatus.OVERDUE.name(), "FF0000")); //$NON-NLS-1$
		colorOpen = UiDesk.getColorFromRGB(ConfigServiceHolder
				.getUser(Preferences.USR_REMINDERCOLORS + "/" + ProcessStatus.OPEN.name(), "00FF00")); //$NON-NLS-1$
	}

	private TableViewerColumn createTypeColumn(TableViewer viewer, int width, int columnIndex) {
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return null;
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof IReminder) {
					IReminder reminder = (IReminder) element;
					Type actionType = reminder.getType();
					switch (actionType) {
					case PRINT:
					case PRINT_DRUG_STICKER:
						return Images.IMG_PRINTER.getImage();
					case MAKE_APPOINTMENT:
						return Images.IMG_CALENDAR.getImage();
					case DISPENSE_MEDICATION:
						return Images.IMG_PILL.getImage();
					case PROCESS_SERVICE_RECORDING:
						return Images.IMG_MONEY.getImage();
					case CHECK_LAB_RESULT:
					case READ_DOCUMENT:
						return Images.IMG_EYE_WO_SHADOW.getImage();
					case SEND_DOCUMENT:
						return Images.IMG_MAIL_SEND.getImage();
					default:
						return null;
					}
				}
				return null;
			}
		});
		return viewerColumn;
	}

	private TableViewerColumn createDateColumn(TableViewer viewer, int width, int columnIndex) {
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.setText("Datum");
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));

		viewerColumn.setLabelProvider(new ColumnLabelProvider() {

			private DateTimeFormatter defaultDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy"); //$NON-NLS-1$

			@Override
			public String getText(Object element) {
				IReminder reminder = (IReminder) element;
				if (reminder.getDue() != null) {
					return defaultDateFormatter.format(reminder.getDue());
				}
				return "";
			}

			@Override
			public Color getBackground(Object element) {
				IReminder reminder = (IReminder) element;
				LocalDate now = LocalDate.now();
				if (reminder.getDue() != null) {
					if (reminder.getDue().equals(now)) {
						return colorDue;
					} else if (reminder.getDue().isBefore(now)) {
						return colorOverdue;
					} else {
						ProcessStatus processStatus = reminder.getStatus();
						if (ProcessStatus.OPEN == processStatus) {
							return colorOpen;
						} else if (ProcessStatus.IN_PROGRESS == processStatus) {
							return colorInProgress;
						}
					}
				}
				return null;
			}
		});
		return viewerColumn;
	}

	private TableViewerColumn createPatientColumn(TableViewer viewer, int width, int columnIndex) {
		ColumnViewerToolTipSupport.enableFor(viewer);
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.setText("Patient");
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IReminder reminder = (IReminder) element;
				IContact contact = reminder.getContact();
				return contact != null ? contact.getLabel() : StringConstants.EMPTY;
			}

			@Override
			public String getToolTipText(Object element) {
				return getText(element);
			}
		});
		return viewerColumn;
	}

	private TableViewerColumn createDescriptionColumn(TableViewer viewer, int width, int columnIndex) {
		ColumnViewerToolTipSupport.enableFor(viewer);
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.setText("Betreff");
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IReminder reminder = (IReminder) element;
				return StringUtils.isEmpty(reminder.getSubject()) ? reminder.getMessage() : reminder.getSubject();
			}

			@Override
			public String getToolTipText(Object element) {
				return getText(element);
			}

			@Override
			public Font getFont(Object element) {
				IReminder reminder = (IReminder) element;
				Priority prio = reminder.getPriority();
				if (Priority.HIGH == prio) {
					return boldFont;
				}
				return null;
			}
		});
		return viewerColumn;
	}

	private TableViewerColumn createResponsibleColumn(TableViewer viewer, int width, int columnIndex) {
		ColumnViewerToolTipSupport.enableFor(viewer);
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.setText("Zust.");
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				IReminder reminder = (IReminder) element;
				if (reminder.isResponsibleAll()) {
					return "Alle";
				}
				List<IContact> responsibles = reminder.getResponsible();
				if (responsibles != null) {
					StringJoiner sj = new StringJoiner("| "); //$NON-NLS-1$
					responsibles.forEach(r -> {
						if (r.isMandator()) {
							sj.add(r.getDescription1() + " " + r.getDescription2());
						} else {
							sj.add(r.getLabel());
						}
					});
					return sj.toString();
				}
				return null;
			}

			@Override
			public String getToolTipText(Object element) {
				return getText(element);
			}
		});
		return viewerColumn;
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

	private SelectionAdapter getSelectionAdapter(final TableViewer viewer, final TableColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ReminderComparator comparator = (ReminderComparator) viewer.getComparator();
				comparator.setColumn(index);
				int dir = comparator.getDirection();
				viewer.getTable().setSortColumn(column);
				viewer.getTable().setSortDirection(dir);
				viewer.refresh(true);
			}
		};
		return selectionAdapter;
	}

	private class ReminderComparator extends ViewerComparator implements Comparator<IReminder> {

		private int column;

		private int direction;

		public ReminderComparator() {
			column = -1;
			direction = SWT.DOWN;
		}

		@Override
		public int compare(IReminder r1, IReminder r2) {
			int result = 0;

			switch (column) {
			case 0: // reminder type
				result = compareByString(r1.getType().toString(), r2.getType().toString());
				break;
			case 1: // data
				result = compareByDate(r1, r2);
				break;
			case 2: // responsible
				result = compareByString(r1.getResponsible().toString(), r2.getResponsible().toString());
				break;
			case 3: // patient
				result = compareByString(r1.getContact().getLabel(), r2.getContact().getLabel());
				break;
			case 4: // subject
				result = compareByString(r1.getSubject(), r2.getSubject());
				break;
			default:
				result = compareByDate(r1, r2);
				break;
			}

			return (direction == SWT.UP) ? -result : result;
		}

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return compare((IReminder) e1, (IReminder) e2);
		}

		private int compareByDate(IReminder r1, IReminder r2) {
			if (r1.getDue() != null && r2.getDue() != null) {
				return r1.getDue().compareTo(r2.getDue());
			} else if (r1.getDue() == null && r2.getDue() == null) {
				return 0;
			} else {
				return (r1.getDue() == null) ? 1 : -1;
			}
		}

		private int compareByString(String s1, String s2) {
			if (s1 == null)
				s1 = "";
			if (s2 == null)
				s2 = "";
			return s1.compareToIgnoreCase(s2);
		}

		public void setColumn(int index) {
			if (column == index) {
				// Same column as last sort; toggle the direction
				direction = ((direction == SWT.DOWN) ? SWT.UP : SWT.DOWN);
			} else {
				// New column; do an ascending sort
				column = index;
				direction = SWT.DOWN;
			}
		}

		public int getDirection() {
			return direction;
		}
	}

	private static String SELECTIONCOMP_CURRENTPATIENT_ID = "reminderlistsview.selection.currentpatient"; //$NON-NLS-1$
	private static String SELECTIONCOMP_GENERALPATIENT_ID = "reminderlistsview.selection.generalpatient"; //$NON-NLS-1$
	private static String SELECTIONCOMP_MYREMINDERS_ID = "reminderlistsview.selection.myreminders"; //$NON-NLS-1$
	private static String SELECTIONCOMP_GROUPREMINDERS_PREFIX = "reminderlistsview.selection.groupreminders."; //$NON-NLS-1$

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
			for (IUserGroup group : userGroups) {
				manager.add(new Action(group.getId(), Action.AS_CHECK_BOX) {

					@Override
					public String getId() {
						return SELECTIONCOMP_GROUPREMINDERS_PREFIX + group.getId();
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
			}
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
			super(String.format("nächste %d Tage", days), Action.AS_CHECK_BOX);
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
				ConfigServiceHolder.setUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS, days);
				filterDueDateDays = days;
				clearSelection();
				refresh();
			} else {
				ConfigServiceHolder.setUser(Preferences.USR_REMINDER_FILTER_DUE_DAYS, -1);
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
			super("Status...");
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
				private IReminder reminder;

				public StatusAction(ProcessStatus representedStatus, IReminder reminder) {
					super(representedStatus.getLocaleText(), SWT.RADIO);
					this.representedStatus = representedStatus;
					this.reminder = reminder;

					ProcessStatus status = reminder.getStatus();
					if (ProcessStatus.DUE == status || ProcessStatus.OVERDUE == status) {
						setChecked(representedStatus == ProcessStatus.OPEN);
					} else {
						setChecked(representedStatus == status);
					}
				}

				@Override
				public boolean isChecked() {
					ProcessStatus status = reminder.getStatus();
					if (ProcessStatus.DUE == status || ProcessStatus.OVERDUE == status) {
						return (representedStatus == ProcessStatus.OPEN);
					} else {
						return (representedStatus == reminder.getStatus());
					}
				}

				@Override
				public String getText() {
					String text = super.getText();
					ProcessStatus status = reminder.getStatus();
					if ((ProcessStatus.DUE == status || ProcessStatus.OVERDUE == status)
							&& (ProcessStatus.OPEN == representedStatus)) {
						return text + " (" + status.getLocaleText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
					}
					return text;
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

	private class CurrentPatientSupplier implements Supplier<List<IReminder>> {

		private IPatient patient;
		private boolean showAll;
		private boolean filterDue;
		private boolean showSelfCreated;
		private boolean showOnlyDue;
		private boolean popupOnLogin;
		private boolean popupOnPatientSelection;
		private boolean assignedToMe;
		private boolean showNotYetDueReminders;

		public CurrentPatientSupplier(IPatient actPatient) {
			patient = actPatient;
		}

		@Override
		public List<IReminder> get() {
			if (patient != null) {
				IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
				query.and(ModelPackage.Literals.IREMINDER__CONTACT, COMPARATOR.EQUALS, patient);
				query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);

				if (showOnlyDue) {
					query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.LESS_OR_EQUAL, LocalDate.now());
				}
				if (showNotYetDueReminders) {
					query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.GREATER_OR_EQUAL, LocalDate.now());
				}

				if (popupOnLogin || popupOnPatientSelection) {
					query.startGroup();

					if (popupOnLogin) {
						query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
								Visibility.POPUP_ON_LOGIN);
					}

					if (popupOnPatientSelection) {
						if (popupOnLogin) {
							query.or(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
									Visibility.POPUP_ON_PATIENT_SELECTION);
						} else {
							query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
									Visibility.POPUP_ON_PATIENT_SELECTION);
						}
					}

					query.andJoinGroups();
				}
					if (showSelfCreated) {
						ContextServiceHolder.get().getActiveMandator().ifPresent(m -> {
							query.and(ModelPackage.Literals.IREMINDER__CREATOR, COMPARATOR.EQUALS, m);

						});
					}
					if (assignedToMe) {
						ContextServiceHolder.get().getActiveMandator().ifPresent(m -> {
							ISubQuery<IReminderResponsibleLink> subQuery = query
									.createSubQuery(IReminderResponsibleLink.class, CoreModelServiceHolder.get());
							subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
							subQuery.and("responsible", COMPARATOR.EQUALS, m);
							query.exists(subQuery);
						});
					}
					
				if (filterDue) {
					applyDueDateFilter(query, false);
				}

				return query.execute();
			}
			return Collections.emptyList();
		}

		public CurrentPatientSupplier showAll(boolean value) {
			this.showAll = value;
			return this;
		}

		public CurrentPatientSupplier filterDue(boolean value) {
			this.filterDue = value;
			return this;
		}

		public CurrentPatientSupplier showSelfCreated(boolean value) {
			this.showSelfCreated = value;
			return this;
		}

		public CurrentPatientSupplier showOnlyDue(boolean showOnlyDueReminders) {
			this.showOnlyDue = showOnlyDueReminders;
			return this;
		}

		public CurrentPatientSupplier popupOnLogin(boolean value) {
			this.popupOnLogin = value;
			return this;
		}

		public CurrentPatientSupplier popupOnPatientSelectionToggleAction(boolean value) {
			this.popupOnPatientSelection = value;
			return this;
		}

		public CurrentPatientSupplier showAssignedToMeAction(boolean value) {
			this.assignedToMe = value;
			return this;
		}

		public CurrentPatientSupplier showNotYetDueReminders(boolean value) {
			this.showNotYetDueReminders = value;
			return this;
		}
	}

	private class GeneralPatientSupplier implements Supplier<List<IReminder>> {

		private IPatient patient;
		private boolean showAll;
		private boolean filterDue;
		private boolean showSelfCreated;
		private boolean showOnlyDue;
		private boolean popupOnLogin;
		private boolean popupOnPatientSelection;
		private boolean assignedToMe;
		private boolean showNotYetDueReminders;

		public GeneralPatientSupplier(IPatient actPatient) {
			patient = actPatient;
		}

		@Override
		public List<IReminder> get() {
			IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);
			if (!showAll) {
				query.andFeatureCompare(ModelPackage.Literals.IREMINDER__CREATOR, COMPARATOR.NOT_EQUALS,
						ModelPackage.Literals.IREMINDER__CONTACT);
				query.and(ModelPackage.Literals.IREMINDER__STATUS, COMPARATOR.NOT_EQUALS, ProcessStatus.CLOSED);
			}

			if (showOnlyDue) {
				query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.LESS_OR_EQUAL, LocalDate.now());
			}
			if (showNotYetDueReminders) {
				query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.GREATER_OR_EQUAL, LocalDate.now());
			}

			if (popupOnLogin || popupOnPatientSelection) {
				query.startGroup();

				if (popupOnLogin) {
					query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
							Visibility.POPUP_ON_LOGIN);
				}

				if (popupOnPatientSelection) {
					if (popupOnLogin) {
						query.or(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
								Visibility.POPUP_ON_PATIENT_SELECTION);
					} else {
						query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
								Visibility.POPUP_ON_PATIENT_SELECTION);
					}
				}

				query.andJoinGroups();
			}

			if (showSelfCreated) {
				ContextServiceHolder.get().getActiveMandator().ifPresent(m -> {
					query.and(ModelPackage.Literals.IREMINDER__CREATOR, COMPARATOR.EQUALS, m);
				});
			}
			if (assignedToMe) {
				ContextServiceHolder.get().getActiveMandator().ifPresent(m -> {
					ISubQuery<IReminderResponsibleLink> subQuery = query.createSubQuery(IReminderResponsibleLink.class,
							CoreModelServiceHolder.get());
					subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
					subQuery.and("responsible", COMPARATOR.EQUALS, m);
					query.exists(subQuery);
				});
			}
			if (filterDue) {
				applyDueDateFilter(query, false);
			}

			return query.execute();
		}

		public GeneralPatientSupplier showAll(boolean value) {
			this.showAll = value;
			return this;
		}

		public GeneralPatientSupplier filterDue(boolean value) {
			this.filterDue = value;
			return this;
		}

		public GeneralPatientSupplier showSelfCreated(boolean value) {
			this.showSelfCreated = value;
			return this;
		}

		public GeneralPatientSupplier showOnlyDue(boolean showOnlyDueReminders) {
			this.showOnlyDue = showOnlyDueReminders;
			return this;
		}

		public GeneralPatientSupplier popupOnLoginReminderToggleAction(boolean value) {
			this.popupOnLogin = value;
			return this;
		}

		public GeneralPatientSupplier popupOnPatientSelectionToggleAction(boolean value) {
			this.popupOnPatientSelection = value;
			return this;
		}

		public GeneralPatientSupplier showAssignedToMeAction(boolean value) {
			this.assignedToMe = value;
			return this;
		}

		public GeneralPatientSupplier showNotYetDueReminders(boolean value) {
			this.showNotYetDueReminders = value;
			return this;
		}
	}

	private class MyRemindersSupplier implements Supplier<List<IReminder>> {

		private boolean showAll;
		private boolean filterDue;
		private boolean showSelfCreated;
		private boolean showOnlyDue;
		private boolean popupOnLogin;
		private boolean popupOnPatientSelection;
		private boolean assignedToMe;
		private boolean showNotYetDueReminders;
		@Override
		public List<IReminder> get() {
			IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);

			if (showOnlyDue) {
				query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.LESS_OR_EQUAL, LocalDate.now());
			}
			if (showNotYetDueReminders) {
				query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.GREATER_OR_EQUAL, LocalDate.now());
			}

			if (popupOnLogin || popupOnPatientSelection) {
				query.startGroup();

				if (popupOnLogin) {
					query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
							Visibility.POPUP_ON_LOGIN);
				}

				if (popupOnPatientSelection) {
					if (popupOnLogin) {
						query.or(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
								Visibility.POPUP_ON_PATIENT_SELECTION);
					} else {
						query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
								Visibility.POPUP_ON_PATIENT_SELECTION);
					}
				}
				query.andJoinGroups();
			}
			if (!showAll) {
				if (showSelfCreated) {
					ContextServiceHolder.get().getActiveMandator().ifPresent(m -> {
						query.and(ModelPackage.Literals.IREMINDER__CREATOR, COMPARATOR.EQUALS, m);
					});
				}
			}

			ContextServiceHolder.get().getActiveMandator().ifPresent(m -> {
				// subQuery to get reminder with active contact
				ISubQuery<IReminderResponsibleLink> subQuery = query.createSubQuery(IReminderResponsibleLink.class,
						CoreModelServiceHolder.get());
				subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
				subQuery.and("responsible", COMPARATOR.EQUALS, m);
				query.exists(subQuery);

				// subQuery to make sure the reminder doesnt have any other contact responsible.
				ISubQuery<IReminderResponsibleLink> secondSubQuery = query
						.createSubQuery(IReminderResponsibleLink.class, CoreModelServiceHolder.get());
				secondSubQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
				secondSubQuery.and("responsible", COMPARATOR.NOT_EQUALS, m);
				query.notExists(secondSubQuery);
			});

			if (filterDue) {
				applyDueDateFilter(query, false);
			}

			return query.execute();
		}

		public MyRemindersSupplier showAll(boolean value) {
			this.showAll = value;
			return this;
		}

		public MyRemindersSupplier filterDue(boolean value) {
			this.filterDue = value;
			return this;
		}

		public MyRemindersSupplier showSelfCreated(boolean value) {
			this.showSelfCreated = value;
			return this;
		}

		public MyRemindersSupplier showOnlyDue(boolean showOnlyDueReminders) {
			this.showOnlyDue = showOnlyDueReminders;
			return this;
		}

		public MyRemindersSupplier popupOnLogin(boolean value) {
			this.popupOnLogin = value;
			return this;
		}

		public MyRemindersSupplier popupOnPatientSelectionToggleAction(boolean value) {
			this.popupOnPatientSelection = value;
			return this;
		}

		public MyRemindersSupplier showAssignedToMeAction(boolean value) {
			this.assignedToMe = value;
			return this;
		}

		public MyRemindersSupplier showNotYetDueReminders(boolean value) {
			this.showNotYetDueReminders = value;
			return this;
		}
	}

	private class GroupRemindersSupplier implements Supplier<List<IReminder>> {

		private boolean showAll;
		private boolean filterDue;
		private boolean showSelfCreated;
		private boolean showOnlyDue;
		private boolean popupOnLogin;
		private boolean popupOnPatientSelection;
		// TODO: is this needed?
		private boolean assignedToMe;
		private boolean showNotYetDueReminders;
		private String groupId;

		public GroupRemindersSupplier(String id) {
			groupId = id;
		}

		@Override
		public List<IReminder> get() {
			IQuery<IReminder> query = CoreModelServiceHolder.get().getQuery(IReminder.class);

			if (showOnlyDue) {
				query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.LESS_OR_EQUAL, LocalDate.now());
			}
			if (showNotYetDueReminders) {
				query.and(ModelPackage.Literals.IREMINDER__DUE, COMPARATOR.GREATER_OR_EQUAL, LocalDate.now());
			}

			if (popupOnLogin || popupOnPatientSelection) {
				query.startGroup();

				if (popupOnLogin) {
					query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
							Visibility.POPUP_ON_LOGIN);
				}

				if (popupOnPatientSelection) {
					if (popupOnLogin) {
						query.or(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
								Visibility.POPUP_ON_PATIENT_SELECTION);
					} else {
						query.and(ModelPackage.Literals.IREMINDER__VISIBILITY, COMPARATOR.EQUALS,
								Visibility.POPUP_ON_PATIENT_SELECTION);
					}
				}
				query.andJoinGroups();
			}
			if (!showAll) {
				if (showSelfCreated) {
					ContextServiceHolder.get().getActiveMandator().ifPresent(m -> {
						query.and(ModelPackage.Literals.IREMINDER__CREATOR, COMPARATOR.EQUALS, m);
					});
				}
			}

			IUserGroup group = null;
			for (IUserGroup g : userGroups) {
				if (g.getId().equalsIgnoreCase(groupId)) {
					group = g;
				}
			}

			if (group != null) {
				List<IContact> contactList = group.getUsers().stream().map(user -> user.getAssignedContact())
						.collect(Collectors.toList());

				// subQuery to get reminders which have any of the contactList assigned.
				for (IContact c : contactList) {
					ISubQuery<IReminderResponsibleLink> subQuery = query.createSubQuery(IReminderResponsibleLink.class,
							CoreModelServiceHolder.get());
					subQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
					subQuery.and("responsible", COMPARATOR.EQUALS, c);
					query.exists(subQuery);
				}
				
				// exclusionQuery to make sure that only reminders are returned which are ONLY
				// assigned to contacts in the contactList.
				ISubQuery<IReminderResponsibleLink> excludeQuery = query.createSubQuery(IReminderResponsibleLink.class,
						CoreModelServiceHolder.get());
				excludeQuery.andParentCompare("id", COMPARATOR.EQUALS, "reminderid");
				for (IContact c : contactList) {
					excludeQuery.and("responsible", COMPARATOR.NOT_EQUALS, c);
				}
				;
				query.notExists(excludeQuery);
			}

			if (filterDue) {
				applyDueDateFilter(query, false);
			}
			return query.execute();
		}

		public GroupRemindersSupplier showAll(boolean value) {
			this.showAll = value;
			return this;
		}

		public GroupRemindersSupplier filterDue(boolean value) {
			this.filterDue = value;
			return this;
		}

		public GroupRemindersSupplier showSelfCreated(boolean value) {
			this.showSelfCreated = value;
			return this;
		}

		public GroupRemindersSupplier showOnlyDue(boolean showOnlyDueReminders) {
			this.showOnlyDue = showOnlyDueReminders;
			return this;
		}

		public GroupRemindersSupplier popupOnLogin(boolean value) {
			this.popupOnLogin = value;
			return this;
		}

		public GroupRemindersSupplier popupOnPatientSelectionToggleAction(boolean value) {
			this.popupOnPatientSelection = value;
			return this;
		}

		public GroupRemindersSupplier showAssignedToMeAction(boolean value) {
			this.assignedToMe = value;
			return this;
		}

		public GroupRemindersSupplier showNotYetDueReminders(boolean value) {
			this.showNotYetDueReminders = value;
			return this;
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
		CURRENT_PATIENT, GENERAL_PATIENT, MYREMINDERS, GROUP
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
		currentPatientViewer.setSelection(clear);
		generalPatientViewer.setSelection(clear);
		myViewer.setSelection(clear);
		for (GroupComponent group : usergroupComponents) {
			group.viewer().setSelection(clear);
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
}
