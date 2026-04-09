package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.Heartbeat.HeartListener;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.events.MessageEvent;
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
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.dnd.ReminderDndManager;
import ch.elexis.core.ui.views.reminder.ReminderFilterManager;
import ch.elexis.core.ui.views.reminder.ReminderSelectionComposite;
import ch.elexis.core.ui.views.reminder.ReminderStatusSubMenu;
import ch.elexis.core.ui.views.reminder.ReminderUIWidgets;
import ch.elexis.core.ui.views.reminder.ReminderUIWidgets.CustomTimePopupDialog;
import ch.elexis.core.ui.views.reminder.ReminderUIWidgets.HeaderComposite;
import ch.elexis.core.ui.views.reminder.ReminderUIWidgets.TableViewerResizer;
import ch.elexis.core.ui.views.reminder.service.ReminderQueryService;
import ch.elexis.core.ui.views.reminder.viewers.ReminderColumnFactory;
import ch.elexis.core.ui.views.reminder.viewers.ReminderColumnFactory.ReminderComparator;
import ch.elexis.core.ui.views.reminder.viewers.ReminderColumnType;
import ch.elexis.data.Reminder;
import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * 
 */
public class ReminderListsView extends ViewPart implements HeartListener, IRefreshable, ISelectionProvider {
	public static final String ID = "ch.elexis.core.ui.views.reminderlistsview"; //$NON-NLS-1$

	private final static String CURRENTPATIENT = "currentpatient"; //$NON-NLS-1$
	private final static String ALLPATIENTS = "allpatients"; //$NON-NLS-1$
	private final static String GENERALREMINDERS = "generalreminders"; //$NON-NLS-1$
	private final static String MYREMINDERS = "myreminders"; //$NON-NLS-1$


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

	private ReminderSelectionComposite viewerSelectionComposite;

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
	private List<GroupComponent> usergroupComponents = new ArrayList<>();
	private List<IUserGroup> userGroups = getUserGroups();

	private Font boldFont;

	private List<IReminder> currentSelection = new ArrayList<>();
	private ListenerList<ISelectionChangedListener> selectionChangedListeners = new ListenerList<>();

	private IPatient actPatient;
	private long cvHighestLastUpdate;

	public static final String GLOBALFILTERS = "global"; //$NON-NLS-1$
	private ReminderFilterManager filterManager = new ReminderFilterManager(this);

	public boolean isUseGlobalFilters() {
		return useGlobalFilters;
	}

	public void setAutoSelectPatient(boolean autoSelect) {
		this.autoSelectPatient = autoSelect;
		this.toggleAutoSelectPatientAction.setChecked(autoSelect);
	}

	public void refreshKeepLabels() {
		for (Entry<TableViewer, String> set : allViewers.entrySet()) {
			TableViewer viewer = set.getKey();
			if (viewer.getTable().isVisible()) {
				viewer.refresh(false);
			}
		}
	}

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

	private RestrictedAction selectPatientAction = new RestrictedAction(EvACE.of(IPatient.class, Right.VIEW),
			Messages.ReminderView_activatePatientAction, Action.AS_UNSPECIFIED) {
		{
			setImageDescriptor(Images.IMG_PERSON.getImageDescriptor());
			setToolTipText(Messages.ReminderView_activatePatientTooltip);
		}

		@Override
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

					@Override
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
							MessageEvent.fireInformation(Messages.ReminderView_importantRemindersCaption, sb.toString(),
									false);
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

		viewerSelectionComposite = new ReminderSelectionComposite(viewParent, SWT.NONE, userGroups);
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

		currentPatientHeader = new ReminderUIWidgets.HeaderComposite(viewersParent, SWT.NONE,
				() -> viewParent.layout(true, true));
		currentPatientHeader.setTextFont(boldFont);
		currentPatientHeader.setText(Messages.ReminderView_currentPatient);
		currentPatientViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(currentPatientViewer, false);
		ReminderDndManager.addDragSupport(currentPatientViewer);
		ReminderDndManager.addDropSupport(currentPatientViewer, ReminderDndManager.TableType.CURRENT_PATIENT,
				viewParent.getShell(), null, this::refresh);

		generalPatientHeader = new ReminderUIWidgets.HeaderComposite(viewersParent, SWT.NONE,
				() -> viewParent.layout(true, true));
		generalPatientHeader.setTextFont(boldFont);
		generalPatientHeader.setText(Messages.ReminderView_allPatients);
		generalPatientViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(generalPatientViewer, true);
		ReminderDndManager.addDragSupport(generalPatientViewer);
		ReminderDndManager.addDropSupport(generalPatientViewer, ReminderDndManager.TableType.GENERAL_PATIENT,
				viewParent.getShell(), null, this::refresh);
		((GridData) generalPatientViewer.getTable().getLayoutData()).heightHint = 300;

		generalRemindersHeader = new ReminderUIWidgets.HeaderComposite(viewersParent, SWT.NONE,
				() -> viewParent.layout(true, true));
		generalRemindersHeader.setTextFont(boldFont);
		generalRemindersHeader.setText(Messages.ReminderView_generalReminders);
		generalRemindersViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(generalRemindersViewer, false);
		ReminderDndManager.addDragSupport(generalRemindersViewer);
		ReminderDndManager.addDropSupport(generalRemindersViewer, ReminderDndManager.TableType.GENERALREMINDERS,
				viewParent.getShell(), null, this::refresh);
		((GridData) generalPatientViewer.getTable().getLayoutData()).heightHint = 300;

		myHeader = new ReminderUIWidgets.HeaderComposite(viewersParent, SWT.NONE, () -> viewParent.layout(true, true));
		myHeader.setTextFont(boldFont);
		myHeader.setText(Messages.ReminderView_myReminders);
		myViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		setupViewer(myViewer, true);
		ReminderDndManager.addDragSupport(myViewer);
		ReminderDndManager.addDropSupport(myViewer, ReminderDndManager.TableType.MYREMINDERS, viewParent.getShell(),
				null, this::refresh);

		for (IUserGroup group : userGroups) {
			HeaderComposite groupHeader = new ReminderUIWidgets.HeaderComposite(viewersParent, SWT.NONE,
					() -> viewParent.layout(true, true));
			groupHeader.setTextFont(boldFont);
			groupHeader.setText(group.getId() + "-Pendenzen");
			TableViewer groupViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
			setupViewer(groupViewer, true);
			ReminderDndManager.addDragSupport(groupViewer);
			ReminderDndManager.addDropSupport(groupViewer, ReminderDndManager.TableType.GROUP, viewParent.getShell(),
					group, this::refresh);
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
		allViewers.put(generalRemindersViewer, GENERALREMINDERS);
		allViewers.put(myViewer, MYREMINDERS);

		for (GroupComponent group : usergroupComponents) {
			allViewers.put(group.viewer(), group.id());
		}

		for (Map.Entry<TableViewer, String> entry : allViewers.entrySet()) {
			String id = entry.getValue();
			Table table = entry.getKey().getTable();

			ReminderFilterManager.FilterActions actions = filterManager.createFilterActions(id, entry.getKey());

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
			menuManager.add(new ReminderStatusSubMenu(entry.getKey()));
			menuManager.add(actions.deleteReminderAction());
			menuManager.add(timeFilterSubMenu);
			menuManager.add(actions.showOnlyOwnDueReminderToggleAction());
			menuManager.add(actions.showNotYetDueReminderToggleAction());
			menuManager.add(actions.showPastDueReminderToggleAction());
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
		for (GroupComponent group : usergroupComponents) {
			hideControl(group.header());
			hideControl(group.viewer().getTable());
		}
		if (selection != null && !selection.isEmpty()) {
			for (Object selected : selection.toList()) {
				if (selected instanceof String) {
					if (ReminderSelectionComposite.SELECTIONCOMP_CURRENTPATIENT_ID.equals(selected)) {
						showControl(currentPatientHeader);
						showControl(currentPatientViewer.getTable());
					} else if (ReminderSelectionComposite.SELECTIONCOMP_GENERALPATIENT_ID.equals(selected)) {
						showControl(generalPatientHeader);
						showControl(generalPatientViewer.getTable());
					} else if (ReminderSelectionComposite.SELECTIONCOMP_GENERALREMINDERS_ID.equals(selected)) {
						showControl(generalRemindersHeader);
						showControl(generalRemindersViewer.getTable());
					} else if (ReminderSelectionComposite.SELECTIONCOMP_MYREMINDERS_ID.equals(selected)) {
						showControl(myHeader);
						showControl(myViewer.getTable());
					} else if (!userGroups.isEmpty()) {
						for (GroupComponent group : usergroupComponents) {
							if ((ReminderSelectionComposite.SELECTIONCOMP_GROUPREMINDERS_PREFIX + group.id())
									.equals(selected)) {
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

	@Override
	public void refresh() {
		Display.getDefault().asyncExec(() -> {

				filterManager.reloadAllFilters();

			patientRefresh();
			generalRefresh();
			generalRemindersRefresh();
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
	
	private void groupRemindersRefresh() {
		for (GroupComponent group : usergroupComponents) {
			if (group.viewer().getTable().isVisible()) {
				refreshGroupRemindersInput(group);
			}
		}
	}

	private void refreshCurrentPatientInput(String config) {
		refreshViewerInput(config, currentPatientViewer, ReminderSelectionComposite.SELECTIONCOMP_CURRENTPATIENT_ID,
				actPatient, null, false);
	}

	private void refreshGeneralPatientInput(String config) {
		refreshViewerInput(config, generalPatientViewer, ReminderSelectionComposite.SELECTIONCOMP_GENERALPATIENT_ID,
				null, null, false);
	}

	private void refreshGeneralInput(String config) {
		refreshViewerInput(config, generalRemindersViewer, ReminderSelectionComposite.SELECTIONCOMP_GENERALREMINDERS_ID,
				null, null, false);
	}

	private void refreshMyRemindersInput(String config) {
		refreshViewerInput(config, myViewer, ReminderSelectionComposite.SELECTIONCOMP_MYREMINDERS_ID, null, null,
				false);
	}

	private void refreshGroupRemindersInput(GroupComponent groupComponent) {
		IUserGroup group = getUserGroupById(groupComponent.id());
		refreshViewerInput(groupComponent.id(), groupComponent.viewer(),
				ReminderSelectionComposite.SELECTIONCOMP_GROUPREMINDERS_PREFIX + groupComponent.id(), null, group,
				false);
	}

	/**
	 * Führt eine Reminder-Abfrage asynchron aus und aktualisiert den angegebenen
	 * Viewer.
	 */
	private void refreshViewerInput(String configKey, TableViewer viewer, String counterId, IPatient patient,
			IUserGroup group, boolean visibleOnly) {

		if (visibleOnly && !viewer.getTable().isVisible()) {
	        return;
	    }

		var filter = filterManager.getFiltersFor(configKey);
		if (filter == null)
			return;

		ReminderQueryService.Config cfg = new ReminderQueryService.Config().showAll(false)
				.filterDue(filterDueDateDays > 0).dueInDays(filterDueDateDays).showOnlyDue(false)
				.showNotYetDueReminders(false).showSelfCreated(false).assignedToMe(false).popupOnLogin(false)
				.popupOnPatientSelection(false);

		if (filter.showOnlyOwnDueReminderToggleAction().isChecked())
			cfg.showOnlyDue(true);
		if (filter.showNotYetDueReminderToggleAction().isChecked())
			cfg.showNotYetDueReminders(true);
		if (filter.showPastDueReminderToggleAction().isChecked())
			cfg.showPastDueReminders(true);
		if (filter.showSelfCreatedReminderAction().isChecked())
			cfg.showSelfCreated(true);
		if (filter.showAssignedToMeAction().isChecked())
			cfg.assignedToMe(true);
		if (filter.popupOnLoginReminderToggleAction().isChecked())
			cfg.popupOnLogin(true);
		if (filter.popupOnPatientSelectionReminderToggleAction().isChecked())
			cfg.popupOnPatientSelection(true);
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
			cfg.group(null);
			cfg.noPatient(true);
			break;

		case MYREMINDERS:
			cfg.patient(null);
			cfg.group(null);
			cfg.assignedToMe(true);
			cfg.showSelfCreated(false);
			break;

		default:
			if (group != null) {
				cfg.group(group);
			}
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
		filterManager.reloadAllFilters();
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
				CustomTimePopupDialog dialog = new CustomTimePopupDialog(getViewSite().getShell());
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
