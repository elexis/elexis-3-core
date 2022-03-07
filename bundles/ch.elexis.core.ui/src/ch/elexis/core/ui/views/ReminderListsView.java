package ch.elexis.core.ui.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.di.annotations.Optional;
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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.events.Heartbeat.HeartListener;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.issue.Priority;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.dialogs.ReminderDetailDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.locks.LockRequestingAction;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Anwender;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.rgw.io.Settings;
import ch.rgw.tools.TimeTool;

public class ReminderListsView extends ViewPart implements HeartListener, ISelectionProvider {
	public static final String ID = "ch.elexis.core.ui.views.reminderlistsview"; //$NON-NLS-1$
	
	private int filterDueDateDays =
		CoreHub.userCfg.get(Preferences.USR_REMINDER_FILTER_DUE_DAYS, -1);
	private boolean autoSelectPatient =
		CoreHub.userCfg.get(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, false);
	private boolean showOnlyDueReminders =
		CoreHub.userCfg.get(Preferences.USR_REMINDERSOPEN, false);
	private boolean showAllReminders = (CoreHub.userCfg.get(Preferences.USR_REMINDEROTHERS, false)
		&& CoreHub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS));
	private boolean showSelfCreatedReminders =
		CoreHub.userCfg.get(Preferences.USR_REMINDEROWN, false);
	
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
	private HeaderComposite generalHeader;
	private TableViewer generalViewer;
	
	private Font boldFont;
	private Color colorInProgress;
	private Color colorDue;
	private Color colorOverdue;
	private Color colorOpen;
	
	private List<Reminder> currentSelection = new ArrayList<Reminder>();
	private ListenerList<ISelectionChangedListener> selectionChangedListeners =
		new ListenerList<>();
	
	private Patient actPatient;
	private long cvHighestLastUpdate;
	
	private Action reloadAction = new Action(Messages.PatHeuteView_reloadAction) {
		{
			setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
			setToolTipText(Messages.PatHeuteView_reloadToolTip);
		}
		
		@Override
		public void run(){
			refresh();
		}
	};
	
	private Action deleteReminderAction = new Action(Messages.ReminderView_deleteAction) {
		{
			setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
			setToolTipText(Messages.ReminderView_deleteToolTip);
		}
		
		@Override
		public void run(){
			StructuredSelection sel = (StructuredSelection) getSelection();
			if (sel != null && sel.size() == 1 && sel.getFirstElement() instanceof Reminder) {
				Reminder r = (Reminder) sel.getFirstElement();
				LockResponse lockResponse = LocalLockServiceHolder.get().acquireLock(r);
				if (lockResponse.isOk()) {
					r.delete();
					LocalLockServiceHolder.get().releaseLock(r);
				} else {
					LockResponseHelper.showInfo(lockResponse, r, null);
				}
				refreshKeepLabels();
			}
		}
		
		@Override
		public boolean isEnabled(){
			StructuredSelection sel = (StructuredSelection) getSelection();
			return (sel != null && sel.size() == 1 && sel.getFirstElement() instanceof Reminder);
		}
	};
	
	private Action newReminderAction = new Action(Messages.ReminderView_newReminderAction) {
		{
			setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
			setToolTipText(Messages.ReminderView_newReminderToolTip);
		}
		
		@Override
		public void run(){
			ReminderDetailDialog erd = null;
			erd = new ReminderDetailDialog(getViewSite().getShell());
			int retVal = erd.open();
			if (retVal == Dialog.OK) {
				Reminder reminder = erd.getReminder();
				LocalLockServiceHolder.get().acquireLock(reminder);
				LocalLockServiceHolder.get().releaseLock(reminder);
			}
			refresh();
		}
	};
	
	private Action toggleAutoSelectPatientAction =
		new Action(Messages.ReminderView_activatePatientAction, Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
				setToolTipText(Messages.ReminderView_toggleSelectPatientActionTooltip);
				setChecked(autoSelectPatient);
			}
			
			@Override
			public void run(){
				autoSelectPatient = toggleAutoSelectPatientAction.isChecked();
				CoreHub.userCfg.set(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT,
					autoSelectPatient);
			}
		};
	
	private Action showOthersRemindersAction =
		new RestrictedAction(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS,
			Messages.ReminderView_foreignAction, Action.AS_CHECK_BOX) {
			{
				setToolTipText(Messages.ReminderView_foreignTooltip);
				setImageDescriptor(Images.IMG_ACHTUNG.getImageDescriptor());
			}
			
			@Override
			public void doRun(){
				showAllReminders = showOthersRemindersAction.isChecked();
				CoreHub.userCfg.set(Preferences.USR_REMINDEROTHERS, showAllReminders);
				refresh();
			}
		};
	
	private Action showSelfCreatedReminderAction =
		new Action(Messages.ReminderView_myRemindersAction, Action.AS_CHECK_BOX) { // $NON-NLS-1$
			{
				setToolTipText(Messages.ReminderView_myRemindersToolTip); // $NON-NLS-1$
			}
			
			@Override
			public void run(){
				showSelfCreatedReminders = showSelfCreatedReminderAction.isChecked();
				CoreHub.userCfg.set(Preferences.USR_REMINDEROWN, showSelfCreatedReminders);
				refresh();
			}
		};
	
	private Action showOnlyOwnDueReminderToggleAction =
		new Action(Messages.ReminderView_onlyDueAction, Action.AS_CHECK_BOX) { // $NON-NLS-1$
			{
				setToolTipText(Messages.ReminderView_onlyDueToolTip); // $NON-NLS-1$
			}
			
			@Override
			public void run(){
				showOnlyDueReminders = showOnlyOwnDueReminderToggleAction.isChecked();
				CoreHub.userCfg.set(Preferences.USR_REMINDERSOPEN, showOnlyDueReminders);
				refresh();
			}
		};
	
	private RestrictedAction selectPatientAction =
		new RestrictedAction(AccessControlDefaults.PATIENT_DISPLAY,
			Messages.ReminderView_activatePatientAction, Action.AS_UNSPECIFIED) {
			{
				setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
				setToolTipText(Messages.ReminderView_activatePatientTooltip);
			}
			
			public void doRun(){
				StructuredSelection sel = (StructuredSelection) getSelection();
				if (sel != null && sel.size() != 1) {
					SWTHelper.showInfo(Messages.ReminderView_onePatOnly,
						Messages.ReminderView_onlyOnePatientForActivation);
				} else if (sel != null && sel.size() > 0) {
					Reminder reminder = (Reminder) sel.getFirstElement();
					Patient patient = reminder.getKontakt();
					Anwender creator = reminder.getCreator();
					if (patient != null) {
						if (!patient.getId().equals(creator.getId())) {
							ElexisEventDispatcher.fireSelectionEvent(patient);
						}
					}
				}
			}
			
			@Override
			public boolean isEnabled(){
				StructuredSelection sel = (StructuredSelection) getSelection();
				if (sel != null && sel.size() == 1 && sel.getFirstElement() instanceof Reminder) {
					Reminder reminder = (Reminder) sel.getFirstElement();
					return reminder.isPatientRelated();
				}
				return false;
			}
		};
	
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		
		public void runInUi(final ElexisEvent ev){
			if (((Patient) ev.getObject()).equals(actPatient)) {
				return;
			}
			actPatient = (Patient) ev.getObject();
			clearSelection();
			patientRefresh();
			
			/**
			 * ch.elexis.core.data.events.PatientEventListener will be called on opposite
			 * Preferences.USR_SHOWPATCHGREMINDER condition.
			 */
			if (!CoreHub.userCfg.get(Preferences.USR_SHOWPATCHGREMINDER, true)) {
				UiDesk.asyncExec(new Runnable() {
					
					public void run(){
						List<Reminder> list = Reminder.findOpenRemindersResponsibleFor(
							CoreHub.getLoggedInContact(), false, (Patient) ev.getObject(), true);
						if (list.size() != 0) {
							StringBuilder sb = new StringBuilder();
							for (Reminder r : list) {
								sb.append(r.getSubject() + "\n");
								sb.append(r.getMessage() + "\n\n");
							}
							SWTHelper.alert(Messages.ReminderView_importantRemindersCaption,
								sb.toString());
						}
					}
				});
			}
		}
	};
	
	private ElexisEventListener eeli_user =
		new ElexisUiEventListenerImpl(Anwender.class, ElexisEvent.EVENT_USER_CHANGED) {
			
			public void runInUi(ElexisEvent ev){
				refreshUserConfiguration();
				refresh();
			}
		};
	
	private ElexisEventListener eeli_reminder = new ElexisUiEventListenerImpl(Reminder.class,
		ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_CREATE | ElexisEvent.EVENT_UPDATE) {
		public void catchElexisEvent(ElexisEvent ev){
			refresh();
		}
	};
	
	@Override
	public void createPartControl(Composite parent){
		viewParent = new Composite(parent, SWT.NONE);
		viewParent.setLayout(new GridLayout());
		
		txtSearch = new Text(viewParent, SWT.SEARCH);
		txtSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtSearch.setMessage(Messages.ReminderView_txtSearch_message);
		txtSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e){
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
		currentPatientViewer =
			new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		currentPatientViewer.getTable().setHeaderVisible(true);
		currentPatientViewer.setContentProvider(ArrayContentProvider.getInstance());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 100;
		currentPatientViewer.getTable().setLayoutData(gd);
		currentPatientViewer.setComparator(new ReminderComparator());
		currentPatientViewer.addFilter(filter);
		currentPatientViewer.addSelectionChangedListener(getSelectionListener());
		currentPatientViewer.addDoubleClickListener(getDoubleClickListener());
		createTypeColumn(currentPatientViewer, 20, 0);
		createDateColumn(currentPatientViewer, 80, 1);
		createResponsibleColumn(currentPatientViewer, 40, 2);
		createPatientColumn(currentPatientViewer, 120, 3);
		createDescriptionColumn(currentPatientViewer, 400, 4);
		
		generalPatientHeader = new HeaderComposite(viewersParent, SWT.NONE);
		generalPatientHeader.setTextFont(boldFont);
		generalPatientHeader.setText("alle Patienten");
		generalPatientViewer =
			new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		generalPatientViewer.getTable().setHeaderVisible(true);
		generalPatientViewer.setContentProvider(ArrayContentProvider.getInstance());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 100;
		generalPatientViewer.getTable().setLayoutData(gd);
		generalPatientViewer.setComparator(new ReminderComparator());
		generalPatientViewer.addFilter(filter);
		generalPatientViewer.addSelectionChangedListener(getSelectionListener());
		generalPatientViewer.addDoubleClickListener(getDoubleClickListener());
		createTypeColumn(generalPatientViewer, 20, 0);
		createDateColumn(generalPatientViewer, 80, 1);
		createResponsibleColumn(generalPatientViewer, 40, 2);
		createPatientColumn(generalPatientViewer, 120, 3);
		createDescriptionColumn(generalPatientViewer, 400, 4);
		
		generalHeader = new HeaderComposite(viewersParent, SWT.NONE);
		generalHeader.setTextFont(boldFont);
		generalHeader.setText("allgemein");
		generalViewer =
			new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		generalViewer.getTable().setHeaderVisible(true);
		generalViewer.setContentProvider(ArrayContentProvider.getInstance());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 100;
		generalViewer.getTable().setLayoutData(gd);
		generalViewer.setComparator(new ReminderComparator());
		generalViewer.addFilter(filter);
		generalViewer.addSelectionChangedListener(getSelectionListener());
		generalViewer.addDoubleClickListener(getDoubleClickListener());
		createTypeColumn(generalViewer, 20, 0);
		createDateColumn(generalViewer, 80, 1);
		createResponsibleColumn(generalViewer, 40, 2);
		createPatientColumn(generalViewer, 120, 3);
		createDescriptionColumn(generalViewer, 400, 4);
		
		viewerSelectionComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				updateViewerSelection(selection);
				refresh();
			}
		});
		actPatient = ElexisEventDispatcher.getSelectedPatient();
		viewerSelectionComposite.loadSelection();
		updateViewerSelection((StructuredSelection) viewerSelectionComposite.getSelection());
		
		refreshUserConfiguration();
		
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(reloadAction, newReminderAction, toggleAutoSelectPatientAction);
		
		MenuManager timeFilterSubMenu = new MenuManager("Zeitraum Anzeige");
		FilterTimeAction action30 = new FilterTimeAction(30);
		FilterTimeAction action60 = new FilterTimeAction(60);
		FilterTimeAction action90 = new FilterTimeAction(90);
		action30.setOthers(Arrays.asList(action60, action90));
		action60.setOthers(Arrays.asList(action30, action90));
		action90.setOthers(Arrays.asList(action30, action60));
		timeFilterSubMenu.add(action30);
		timeFilterSubMenu.add(action60);
		timeFilterSubMenu.add(action90);
		
		MenuManager menuManager = new MenuManager();
		menuManager.add(new ReminderStatusSubMenu());
		menuManager.add(deleteReminderAction);
		menuManager.add(timeFilterSubMenu);
		menuManager.add(showOnlyOwnDueReminderToggleAction);
		menuManager.add(showSelfCreatedReminderAction);
		menuManager.add(showOthersRemindersAction);
		
		currentPatientViewer.getTable()
			.setMenu(menuManager.createContextMenu(currentPatientViewer.getTable()));
		generalPatientViewer.getTable()
			.setMenu(menuManager.createContextMenu(generalPatientViewer.getTable()));
		generalViewer.getTable().setMenu(menuManager.createContextMenu(generalViewer.getTable()));
		
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat, eeli_user, eeli_reminder);
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat, eeli_user, eeli_reminder);
	}
	
	private void updateViewerSelection(StructuredSelection selection){
		viewersParent.setRedraw(false);
		hideControl(currentPatientHeader);
		hideControl(currentPatientViewer.getTable());
		hideControl(generalPatientHeader);
		hideControl(generalPatientViewer.getTable());
		hideControl(generalHeader);
		hideControl(generalViewer.getTable());
		if (selection != null && !selection.isEmpty()) {
			for (Object selected : selection.toList()) {
				if (selected instanceof String) {
					if (SELECTIONCOMP_CURRENTPATIENT_ID.equals(selected)) {
						showControl(currentPatientHeader);
						showControl(currentPatientViewer.getTable());
					} else if (SELECTIONCOMP_GENERALPATIENT_ID.equals(selected)) {
						showControl(generalPatientHeader);
						showControl(generalPatientViewer.getTable());
					} else if (SELECTIONCOMP_GENERAL_ID.equals(selected)) {
						showControl(generalHeader);
						showControl(generalViewer.getTable());
					}
				}
			}
		}
		viewersParent.setRedraw(true);
		int width = viewersScrolledComposite.getClientArea().width;
		viewersScrolledComposite.setMinSize(viewersParent.computeSize(width, SWT.DEFAULT));
		viewParent.layout(true, true);
	}
	
	private void showControl(Control control){
		if (control != null && control.getLayoutData() != null) {
			control.setVisible(true);
			((GridData) control.getLayoutData()).exclude = false;
		}
	}
	
	private void hideControl(Control control){
		if (control != null && control.getLayoutData() != null) {
			control.setVisible(false);
			((GridData) control.getLayoutData()).exclude = true;
		}
	}
	
	@Override
	public void setFocus(){
		viewersParent.setFocus();
	}
	
	private void refreshKeepLabels(){
		if (generalViewer.getTable().isVisible()) {
			generalViewer.refresh(false);
		}
		if (generalPatientViewer.getTable().isVisible()) {
			generalPatientViewer.refresh(false);
		}
		if (currentPatientViewer.getTable().isVisible()) {
			currentPatientViewer.refresh(false);
		}
	}
	
	private void refresh(){
		Display.getDefault().asyncExec(() -> {
			patientRefresh();
			generalRefresh();
			int width = viewersScrolledComposite.getClientArea().width;
			viewersScrolledComposite.setMinSize(viewersParent.computeSize(width, SWT.DEFAULT));
			viewParent.layout(true, true);
		});
	}
	
	private void patientRefresh(){
		if (actPatient != null) {
			if (currentPatientViewer.getTable().isVisible()) {
				refreshCurrentPatientInput();
			}
		} else {
			currentPatientViewer.setInput(Collections.emptyList());
		}
	}
	
	private void generalRefresh(){
		if (generalViewer.getTable().isVisible()) {
			refreshGeneralInput();
		}
		if (generalPatientViewer.getTable().isVisible()) {
			refreshGeneralPatientInput();
		}
	}
	
	private void refreshCurrentPatientInput(){
		if (actPatient != null) {
			Query<Reminder> query = new Query<>(Reminder.class, null, null, Reminder.TABLENAME,
				new String[] {
					Reminder.FLD_DUE, Reminder.FLD_PRIORITY, Reminder.FLD_ACTION_TYPE,
					Reminder.FLD_CREATOR, Reminder.FLD_KONTAKT_ID
				});
			List<Reminder> reminders = Collections.emptyList();
			if (showAllReminders
				&& CoreHub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS)) {
				query.add(Reminder.FLD_KONTAKT_ID, Query.EQUALS, actPatient.getId());
				if (filterDueDateDays != -1) {
					applyDueDateFilter(query);
				}
				reminders = query.execute();
			} else {
				reminders = Reminder.findOpenRemindersResponsibleFor(CoreHub.getLoggedInContact(),
					showOnlyDueReminders, filterDueDateDays, actPatient, false);
				
				if (showSelfCreatedReminders) {
					query.add(Reminder.FLD_CREATOR, Query.EQUALS,
						CoreHub.getLoggedInContact().getId());
					query.add(Reminder.FLD_KONTAKT_ID, Query.EQUALS, actPatient.getId());
					if (filterDueDateDays != -1) {
						applyDueDateFilter(query);
					}
					reminders.addAll(query.execute());
				}
			}
			List<Reminder> input = reminders;
			Display.getDefault().asyncExec(() -> {
				if (currentPatientViewer != null && !currentPatientViewer.getTable().isDisposed()) {
					currentPatientViewer.setInput(input);
					viewerSelectionComposite.setCount(SELECTIONCOMP_CURRENTPATIENT_ID,
						currentPatientViewer.getTable().getItemCount());
					if (input.size() < 5) {
						if (((GridData) currentPatientViewer.getTable()
							.getLayoutData()).heightHint != 125) {
							((GridData) currentPatientViewer.getTable()
								.getLayoutData()).heightHint = 125;
							currentPatientViewer.getTable().getParent().layout(true, true);
						}
					} else {
						if (((GridData) currentPatientViewer.getTable()
							.getLayoutData()).heightHint != 300) {
							((GridData) currentPatientViewer.getTable()
								.getLayoutData()).heightHint = 300;
							currentPatientViewer.getTable().getParent().layout(true, true);
						}
					}
				}
			});
		}
	}
	
	private void refreshGeneralPatientInput(){
		HashSet<Reminder> uniqueReminders = new HashSet<>();
		uniqueReminders.addAll(Reminder.findOpenRemindersResponsibleFor(
			CoreHub.getLoggedInContact(), showOnlyDueReminders, filterDueDateDays, null, false));
		
		Query<Reminder> query = new Query<>(Reminder.class, null, null, Reminder.TABLENAME,
			new String[] {
				Reminder.FLD_DUE, Reminder.FLD_PRIORITY, Reminder.FLD_ACTION_TYPE,
				Reminder.FLD_CREATOR, Reminder.FLD_KONTAKT_ID
			});
		if (showSelfCreatedReminders) {
			query.add(Reminder.FLD_CREATOR, Query.EQUALS, CoreHub.getLoggedInContact().getId());
			if (filterDueDateDays != -1) {
				applyDueDateFilter(query);
			}
			uniqueReminders.addAll(query.execute());
		}
		List<Reminder> filteredReminders = uniqueReminders.parallelStream()
			.filter(r -> r.isPatientRelated()).collect(Collectors.toList());
		Display.getDefault().asyncExec(() -> {
			if (generalPatientViewer != null && !generalPatientViewer.getTable().isDisposed()) {
				generalPatientViewer.setInput(filteredReminders);
				viewerSelectionComposite.setCount(SELECTIONCOMP_GENERALPATIENT_ID,
					generalPatientViewer.getTable().getItemCount());
				if (generalPatientViewer.getTable().getItemCount() < 5) {
					if (((GridData) generalPatientViewer.getTable()
						.getLayoutData()).heightHint != 125) {
						((GridData) generalPatientViewer.getTable().getLayoutData()).heightHint =
							125;
						generalPatientViewer.getTable().getParent().layout(true, true);
					}
				} else {
					if (((GridData) generalPatientViewer.getTable()
						.getLayoutData()).heightHint != 300) {
						((GridData) generalPatientViewer.getTable().getLayoutData()).heightHint =
							300;
						generalPatientViewer.getTable().getParent().layout(true, true);
					}
				}
			}
		});
	}
	
	private void refreshGeneralInput(){
		HashSet<Reminder> uniqueReminders = new HashSet<>();
		uniqueReminders.addAll(Reminder.findOpenRemindersResponsibleFor(
			CoreHub.getLoggedInContact(), showOnlyDueReminders, filterDueDateDays, null, false));
		
		Query<Reminder> query = new Query<>(Reminder.class, null, null, Reminder.TABLENAME,
			new String[] {
				Reminder.FLD_DUE, Reminder.FLD_PRIORITY, Reminder.FLD_ACTION_TYPE,
				Reminder.FLD_CREATOR, Reminder.FLD_KONTAKT_ID
			});
		if (showSelfCreatedReminders) {
			query.add(Reminder.FLD_CREATOR, Query.EQUALS, CoreHub.getLoggedInContact().getId());
			if (filterDueDateDays != -1) {
				applyDueDateFilter(query);
			}
			uniqueReminders.addAll(query.execute());
		}
		List<Reminder> filteredReminders = uniqueReminders.parallelStream()
			.filter(r -> !r.isPatientRelated()).collect(Collectors.toList());
		Display.getDefault().asyncExec(() -> {
			if (generalViewer != null && !generalViewer.getTable().isDisposed()) {
				generalViewer.setInput(filteredReminders);
				viewerSelectionComposite.setCount(SELECTIONCOMP_GENERAL_ID,
					generalViewer.getTable().getItemCount());
				if (generalViewer.getTable().getItemCount() < 5) {
					if (((GridData) generalViewer.getTable().getLayoutData()).heightHint != 125) {
						((GridData) generalViewer.getTable().getLayoutData()).heightHint = 125;
						generalViewer.getTable().getParent().layout(true, true);
					}
				} else {
					if (((GridData) generalViewer.getTable().getLayoutData()).heightHint != 300) {
						((GridData) generalViewer.getTable().getLayoutData()).heightHint = 300;
						generalViewer.getTable().getParent().layout(true, true);
					}
				}
			}
		});
	}
	
	private void applyDueDateFilter(Query<Reminder> qbe){
		TimeTool dueDateDays = new TimeTool();
		dueDateDays.addDays(filterDueDateDays);
		qbe.add(Reminder.FLD_DUE, Query.NOT_EQUAL, "");
		qbe.add(Reminder.FLD_DUE, Query.LESS_OR_EQUAL, dueDateDays.toString(TimeTool.DATE_COMPACT));
	}
	
	private void refreshUserConfiguration(){
		showOnlyOwnDueReminderToggleAction
			.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDERSOPEN, true));
		showSelfCreatedReminderAction
			.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDEROWN, false));
		toggleAutoSelectPatientAction
			.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, false));
		//
		//		// get state from user's configuration
		//		showOthersRemindersAction.setChecked(CoreHub.userCfg.get(Preferences.USR_REMINDEROTHERS, false));
		//
		//		// update action's access rights
		//		showOthersRemindersAction.reflectRight();
		
		Settings cfg = CoreHub.userCfg.getBranch(Preferences.USR_REMINDERCOLORS, true);
		colorInProgress =
			UiDesk.getColorFromRGB(cfg.get(ProcessStatus.IN_PROGRESS.name(), "FFFFFF")); //$NON-NLS-1$ ;
		colorDue = UiDesk.getColorFromRGB(cfg.get(ProcessStatus.DUE.name(), "FFFFFF")); //$NON-NLS-1$ ;
		colorOverdue = UiDesk.getColorFromRGB(cfg.get(ProcessStatus.OVERDUE.name(), "FF0000")); //$NON-NLS-1$
		colorOpen = UiDesk.getColorFromRGB(cfg.get(ProcessStatus.OPEN.name(), "00FF00")); //$NON-NLS-1$
	}
	
	private TableViewerColumn createTypeColumn(TableViewer viewer, int width, int columnIndex){
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				return "";
			}
			
			@Override
			public Image getImage(Object element){
				if (element instanceof Reminder) {
					Reminder reminder = (Reminder) element;
					Type actionType = reminder.getActionType();
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
	
	private TableViewerColumn createDateColumn(TableViewer viewer, int width, int columnIndex){
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.setText("Datum");
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				Reminder reminder = (Reminder) element;
				return reminder.get(Reminder.FLD_DUE);
			}
			
			@Override
			public Color getBackground(Object element){
				Reminder reminder = (Reminder) element;
				
				switch (reminder.getDueState()) {
				case 1:
					return colorDue;
				case 2:
					return colorOverdue;
				default:
					ProcessStatus processStatus = reminder.getProcessStatus();
					if (ProcessStatus.OPEN == processStatus) {
						return colorOpen;
					} else if (ProcessStatus.IN_PROGRESS == processStatus) {
						return colorInProgress;
					}
					return null;
				}
			}
		});
		return viewerColumn;
	}
	
	private TableViewerColumn createPatientColumn(TableViewer viewer, int width, int columnIndex){
		ColumnViewerToolTipSupport.enableFor(viewer);
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.setText("Patient");
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				Reminder reminder = (Reminder) element;
				Kontakt k = Kontakt.load(reminder.get(Reminder.FLD_KONTAKT_ID));
				return k.getLabel(false);
			}
			
			@Override
			public String getToolTipText(Object element){
				return getText(element);
			}
		});
		return viewerColumn;
	}
	
	private TableViewerColumn createDescriptionColumn(TableViewer viewer, int width,
		int columnIndex){
		ColumnViewerToolTipSupport.enableFor(viewer);
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.setText("Betreff");
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				Reminder reminder = (Reminder) element;
				String[] vals = reminder.get(true, Reminder.FLD_MESSAGE, Reminder.FLD_SUBJECT);
				return (vals[1].length() > 0) ? vals[1] : vals[0];
			}
			
			@Override
			public String getToolTipText(Object element){
				return getText(element);
			}
			
			@Override
			public Font getFont(Object element){
				Reminder reminder = (Reminder) element;
				Priority prio = reminder.getPriority();
				if (Priority.HIGH == prio) {
					return boldFont;
				}
				return null;
			}
		});
		return viewerColumn;
	}
	
	private TableViewerColumn createResponsibleColumn(TableViewer viewer, int width,
		int columnIndex){
		ColumnViewerToolTipSupport.enableFor(viewer);
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.setText("Zust.");
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				Reminder reminder = (Reminder) element;
				List<Anwender> responsibles = reminder.getResponsibles();
				if (responsibles != null) {
					StringJoiner sj = new StringJoiner(", ");
					responsibles.forEach(r -> sj.add(r.getLabel(true)));
					return sj.toString();
				}
				return null;
			}
			
			@Override
			public String getToolTipText(Object element){
				return getText(element);
			}
		});
		return viewerColumn;
	}
	
	private ISelectionChangedListener getSelectionListener(){
		return new ISelectionChangedListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();
				ReminderListsView.this.selectionChanged(selection.toList());
				selectPatientAction.setEnabled(selection.size() <= 1);
				selectPatientAction.reflectRight();
				if (autoSelectPatient && selectPatientAction.isEnabled()) {
					selectPatientAction.doRun();
				}
			}
		};
	}
	
	private IDoubleClickListener getDoubleClickListener(){
		return new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event){
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				if (selection != null && !selection.isEmpty()) {
					Reminder reminder = (Reminder) selection.getFirstElement();
					AcquireLockBlockingUi.aquireAndRun(reminder, new ILockHandler() {
						@Override
						public void lockAcquired(){
							ReminderDetailDialog rdd =
								new ReminderDetailDialog(UiDesk.getTopShell(), reminder);
							int retVal = rdd.open();
							if (retVal == Dialog.OK) {
								ElexisEventDispatcher.getInstance().fire(new ElexisEvent(reminder,
									getClass(), ElexisEvent.EVENT_UPDATE));
							}
						}
						
						@Override
						public void lockFailed(){
							refreshKeepLabels();
						}
					});
				}
			}
		};
	}
	
	private SelectionAdapter getSelectionAdapter(final TableViewer viewer, final TableColumn column,
		final int index){
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
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
	
	private class ReminderComparator extends ViewerComparator implements Comparator<Reminder> {
		
		private int column;
		
		private int direction;
		
		public ReminderComparator(){
			column = -1;
			direction = SWT.DOWN;
		}
		
		@Override
		public int compare(Reminder r1, Reminder r2){
			if (column == 1) {
				if (direction == SWT.UP) {
					return TimeTool.compare(r2.getDateDue(), r1.getDateDue());
				} else {
					return TimeTool.compare(r1.getDateDue(), r2.getDateDue());
				}
			} else {
				return TimeTool.compare(r1.getDateDue(), r2.getDateDue());
			}
		}
		
		@Override
		public int compare(Viewer viewer, Object e1, Object e2){
			return compare((Reminder) e1, (Reminder) e2);
		}
		
		public void setColumn(int index){
			if (column == index) {
				// Same column as last sort; toggle the direction
				direction = ((direction == SWT.DOWN) ? SWT.UP : SWT.DOWN);
			} else {
				// New column; do an ascending sort
				column = index;
				direction = SWT.DOWN;
			}
		}
		
		public int getDirection(){
			return direction;
		}
	}
	
	private static String SELECTIONCOMP_CURRENTPATIENT_ID =
		"reminderlistsview.selection.currentpatient";
	private static String SELECTIONCOMP_GENERALPATIENT_ID =
		"reminderlistsview.selection.generalpatient";
	private static String SELECTIONCOMP_GENERAL_ID = "reminderlistsview.selection.general";
	
	private class ViewerSelectionComposite extends Composite implements ISelectionProvider {
		
		private List<Action> currentSelection;
		
		private ListenerList<ISelectionChangedListener> selectionChangedListeners;
		
		private ToolBarManager manager;
		
		public ViewerSelectionComposite(Composite parent, int style){
			super(parent, style);
			currentSelection = new ArrayList<>();
			selectionChangedListeners = new ListenerList<>();
			
			createContent();
		}
		
		public void setCount(String id, int itemCount){
			for (IContributionItem item : manager.getItems()) {
				if (item.getId().equals(id)) {
					IAction action = ((ActionContributionItem) item).getAction();
					String text = action.getText();
					if (text.indexOf(" (") != -1) {
						text = text.substring(0, text.indexOf(" ("));
					}
					action.setText(text + " (" + itemCount + ")");
					item.update();
				}
			}
			manager.update(true);
			layout();
		}
		
		private void createContent(){
			setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			setLayout(new FillLayout());
			
			manager = new ToolBarManager(SWT.WRAP);
			manager.add(new Action("aktueller Patient", Action.AS_CHECK_BOX) {
				@Override
				public String getId(){
					return SELECTIONCOMP_CURRENTPATIENT_ID;
				}
				
				@Override
				public void run(){
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
				public String getId(){
					return SELECTIONCOMP_GENERALPATIENT_ID;
				}
				
				@Override
				public void run(){
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
			manager.add(new Action("allgemein", Action.AS_CHECK_BOX) {
				
				@Override
				public String getId(){
					return SELECTIONCOMP_GENERAL_ID;
				}
				
				@Override
				public void run(){
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
		
		private void saveSelection(){
			List<String> selectedIds = currentSelection.stream().map(action -> action.getId())
				.collect(Collectors.toList());
			StringJoiner sj = new StringJoiner(",");
			selectedIds.forEach(id -> sj.add(id));
			CoreHub.userCfg.set(Preferences.USR_REMINDER_VIEWER_SELECTION, sj.toString());
		}
		
		public void loadSelection(){
			currentSelection.clear();
			String[] loadedIds =
				CoreHub.userCfg.get(Preferences.USR_REMINDER_VIEWER_SELECTION, "").split(",");
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
		public void addSelectionChangedListener(ISelectionChangedListener listener){
			selectionChangedListeners.add(listener);
		}
		
		@Override
		public void removeSelectionChangedListener(ISelectionChangedListener listener){
			selectionChangedListeners.remove(listener);
		}
		
		@Override
		public ISelection getSelection(){
			return new StructuredSelection(currentSelection.stream().map(action -> action.getId())
				.collect(Collectors.toList()));
		}
		
		@Override
		public void setSelection(ISelection selection){
			// ignore until needed
		}
		
		private void fireSelectionChanged(){
			ISelection selection = getSelection();
			for (ISelectionChangedListener listener : selectionChangedListeners) {
				SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
				listener.selectionChanged(event);
			}
		}
	}
	
	private class FilterTimeAction extends Action {
		
		private List<FilterTimeAction> others;
		private int days;
		
		public FilterTimeAction(int days){
			super(String.format("nächste %d Tage", days), Action.AS_CHECK_BOX);
			this.days = days;
			if (filterDueDateDays == days) {
				setChecked(true);
			}
		}
		
		public void setOthers(List<FilterTimeAction> list){
			this.others = list;
		}
		
		@Override
		public void run(){
			if (isChecked()) {
				CoreHub.userCfg.set(Preferences.USR_REMINDER_FILTER_DUE_DAYS, days);
				filterDueDateDays = days;
				clearSelection();
				refresh();
			} else {
				CoreHub.userCfg.set(Preferences.USR_REMINDER_FILTER_DUE_DAYS, -1);
				filterDueDateDays = -1;
				clearSelection();
				refresh();
			}
			
			if (others != null) {
				for (FilterTimeAction other : others) {
					other.setChecked(false);
				}
			}
		}
	}
	
	private class ReminderStatusSubMenu extends MenuManager {
		
		public ReminderStatusSubMenu(){
			super("Status...");
			setRemoveAllWhenShown(true);
			addMenuListener(new ReminderStatusSubMenuListener());
		}
		
		private class ReminderStatusSubMenuListener implements IMenuListener {
			
			@Override
			public void menuAboutToShow(IMenuManager manager){
				StructuredSelection selection = (StructuredSelection) getSelection();
				if (selection != null && selection.size() == 1) {
					if (selection.getFirstElement() instanceof Reminder) {
						Reminder reminder = (Reminder) selection.getFirstElement();
						manager.add(new StatusAction(ProcessStatus.OPEN, reminder));
						manager.add(new StatusAction(ProcessStatus.IN_PROGRESS, reminder));
						manager.add(new StatusAction(ProcessStatus.CLOSED, reminder));
						manager.add(new StatusAction(ProcessStatus.ON_HOLD, reminder));
					}
				} else {
					manager.add(new Action("Multiple selection") {
						@Override
						public boolean isEnabled(){
							return false;
						}
					});
				}
			}
			
			private class StatusAction extends LockRequestingAction<Reminder> {
				
				private final ProcessStatus representedStatus;
				private Reminder reminder;
				
				public StatusAction(ProcessStatus representedStatus, Reminder reminder){
					super(representedStatus.getLocaleText(), SWT.RADIO);
					this.representedStatus = representedStatus;
					this.reminder = reminder;
					
					ProcessStatus status = reminder.getProcessStatus();
					if (ProcessStatus.DUE == status || ProcessStatus.OVERDUE == status) {
						setChecked(representedStatus == ProcessStatus.OPEN);
					} else {
						setChecked(representedStatus == status);
					}
				}
				
				@Override
				public boolean isChecked(){
					ProcessStatus status = reminder.getProcessStatus();
					if (ProcessStatus.DUE == status || ProcessStatus.OVERDUE == status) {
						return (representedStatus == ProcessStatus.OPEN);
					} else {
						return (representedStatus == reminder.getProcessStatus());
					}
				}
				
				@Override
				public String getText(){
					String text = super.getText();
					ProcessStatus status = reminder.getProcessStatus();
					if ((ProcessStatus.DUE == status || ProcessStatus.OVERDUE == status)
						&& (ProcessStatus.OPEN == representedStatus)) {
						return text + " (" + status.getLocaleText() + ")";
					}
					return text;
				}
				
				@Override
				public Reminder getTargetedObject(){
					return reminder;
				}
				
				@Override
				public void doRun(Reminder element){
					element.setProcessStatus(representedStatus);
					ElexisEventDispatcher.getInstance()
						.fire(new ElexisEvent(element, Reminder.class, ElexisEvent.EVENT_UPDATE));
				}
			}
		}
	}
	
	private class HeaderComposite extends Composite {
		
		private Label header;
		private ToolBarManager toolbarManager;
		
		public HeaderComposite(Composite parent, int style){
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
		
		public void setTextFont(Font font){
			header.setFont(font);
		}
		
		public void setText(String text){
			header.setText(text);
			ReminderListsView.this.viewParent.layout(true, true);
		}
	}
	
	@Override
	public void heartbeat(){
		long highestLastUpdate = PersistentObject.getHighestLastUpdate(Reminder.TABLENAME);
		if (highestLastUpdate > cvHighestLastUpdate) {
			refresh();
			cvHighestLastUpdate = highestLastUpdate;
		}
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		selectionChangedListeners.add(listener);
	}
	
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		selectionChangedListeners.remove(listener);
	}
	
	@Override
	public ISelection getSelection(){
		return new StructuredSelection(currentSelection);
	}
	
	@Override
	public void setSelection(ISelection arg0){
		// currently not supported
	}
	
	private void clearSelection(){
		StructuredSelection clear = new StructuredSelection();
		currentPatientViewer.setSelection(clear);
		generalPatientViewer.setSelection(clear);
		generalViewer.setSelection(clear);
	}
	
	private void selectionChanged(List<Reminder> list){
		currentSelection.clear();
		currentSelection.addAll(list);
		fireSelectionChanged();
	}
	
	private void fireSelectionChanged(){
		ISelection selection = getSelection();
		for (ISelectionChangedListener listener : selectionChangedListeners) {
			SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
			listener.selectionChanged(event);
		}
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
