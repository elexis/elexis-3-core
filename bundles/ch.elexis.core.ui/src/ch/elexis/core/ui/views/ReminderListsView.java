package ch.elexis.core.ui.views;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
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

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.model.issue.Type;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Patient;
import ch.elexis.data.Query;
import ch.elexis.data.Reminder;
import ch.rgw.tools.TimeTool;

public class ReminderListsView extends ViewPart {
	public static final String ID = "ch.elexis.core.ui.views.reminderlistsview"; //$NON-NLS-1$

	private int filterDueDateDays = CoreHub.userCfg.get(Preferences.USR_REMINDER_FILTER_DUE_DAYS, -1);
	private boolean autoSelectPatient = CoreHub.userCfg.get(Preferences.USR_REMINDER_AUTO_SELECT_PATIENT, false);
	private boolean showOnlyDueReminders = CoreHub.userCfg.get(Preferences.USR_REMINDERSOPEN, false);
	private boolean showAllReminders = (CoreHub.userCfg.get(Preferences.USR_REMINDEROTHERS, false)
			&& CoreHub.acl.request(AccessControlDefaults.ADMIN_VIEW_ALL_REMINDERS));
	private boolean showSelfCreatedReminders = CoreHub.userCfg.get(Preferences.USR_REMINDEROWN, false);

	private Composite viewParent;

	private Text txtSearch;
	private ReminderFilter filter = new ReminderFilter();

	private ViewerSelectionComposite viewerSelectionComposite;

	private Composite viewersParent;

	private HeaderComposite currentPatientHeader;
	private TableViewer currentPatientViewer;
	private HeaderComposite generalPatientHeader;
	private TableViewer generalPatientViewer;
	private HeaderComposite generalHeader;
	private TableViewer generalViewer;

	private Patient actPatient;

	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {

		public void runInUi(final ElexisEvent ev) {
			if (((Patient) ev.getObject()).equals(actPatient)) {
				return;
			}
			actPatient = (Patient) ev.getObject();
			patientRefresh();

			/**
			 * ch.elexis.core.data.events.PatientEventListener will be called on opposite
			 * Preferences.USR_SHOWPATCHGREMINDER condition.
			 */
			if (!CoreHub.userCfg.get(Preferences.USR_SHOWPATCHGREMINDER, true)) {
				UiDesk.asyncExec(new Runnable() {

					public void run() {
						List<Reminder> list = Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser, false,
								(Patient) ev.getObject(), true);
						if (list.size() != 0) {
							StringBuilder sb = new StringBuilder();
							for (Reminder r : list) {
								sb.append(r.getSubject() + "\n");
								sb.append(r.getMessage() + "\n\n");
							}
							SWTHelper.alert(Messages.ReminderView_importantRemindersCaption, sb.toString());
						}
					}
				});
			}
		}
	};

	private ScrolledComposite viewersScrolledComposite;

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
				filter.setFilterText(txtSearch.getText());
				filterRefresh();
			}
		});

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
		currentPatientHeader.setText("aktueller Patient");
		currentPatientViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		currentPatientViewer.getTable().setHeaderVisible(true);
		currentPatientViewer.setContentProvider(ArrayContentProvider.getInstance());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 100;
		currentPatientViewer.getTable().setLayoutData(gd);
		currentPatientViewer.setComparator(new ReminderComparator());
		currentPatientViewer.addFilter(filter);
		createTypeColumn(currentPatientViewer, 20, 0);
		createDateColumn(currentPatientViewer, 80, 1);
		createPatientColumn(currentPatientViewer, 120, 2);
		createDescriptionColumn(currentPatientViewer, 400, 3);

		generalPatientHeader = new HeaderComposite(viewersParent, SWT.NONE);
		generalPatientHeader.setText("nicht aktueller Patient");
		generalPatientViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		generalPatientViewer.getTable().setHeaderVisible(true);
		generalPatientViewer.setContentProvider(ArrayContentProvider.getInstance());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 100;
		generalPatientViewer.getTable().setLayoutData(gd);
		generalPatientViewer.setComparator(new ReminderComparator());
		generalPatientViewer.addFilter(filter);
		createTypeColumn(generalPatientViewer, 20, 0);
		createDateColumn(generalPatientViewer, 80, 1);
		createPatientColumn(generalPatientViewer, 120, 2);
		createDescriptionColumn(generalPatientViewer, 400, 3);

		generalHeader = new HeaderComposite(viewersParent, SWT.NONE);
		generalHeader.setText("allgemein");
		generalViewer = new TableViewer(viewersParent, SWT.FULL_SELECTION | SWT.BORDER | SWT.VIRTUAL);
		generalViewer.getTable().setHeaderVisible(true);
		generalViewer.setContentProvider(ArrayContentProvider.getInstance());
		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 100;
		generalViewer.getTable().setLayoutData(gd);
		generalViewer.setComparator(new ReminderComparator());
		generalViewer.addFilter(filter);
		createTypeColumn(generalViewer, 20, 0);
		createDateColumn(generalViewer, 80, 1);
		createPatientColumn(generalViewer, 120, 2);
		createDescriptionColumn(generalViewer, 400, 3);

		viewerSelectionComposite.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				updateViewerSelection(selection);
				refresh();
			}
		});
		updateViewerSelection((StructuredSelection) viewerSelectionComposite.getSelection());

		ElexisEventDispatcher.getInstance().addListeners(eeli_pat);
		actPatient = ElexisEventDispatcher.getSelectedPatient();
		refresh();
	}

	@Override
	public void dispose() {
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat);
	}

	private void updateViewerSelection(StructuredSelection selection) {
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
					if ("reminderlistsview.selection.currentpatient".equals(selected)) {
						showControl(currentPatientHeader);
						showControl(currentPatientViewer.getTable());
					} else if ("reminderlistsview.selection.generalpatient".equals(selected)) {
						showControl(generalPatientHeader);
						showControl(generalPatientViewer.getTable());
					} else if ("reminderlistsview.selection.general".equals(selected)) {
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

	private void filterRefresh() {
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

	private void refresh() {
		patientRefresh();
		generalRefresh();
		int width = viewersScrolledComposite.getClientArea().width;
		viewersScrolledComposite.setMinSize(viewersParent.computeSize(width, SWT.DEFAULT));
		viewParent.layout(true, true);
	}

	private void patientRefresh() {
		if (actPatient != null) {
			if (currentPatientViewer.getTable().isVisible()) {
				refreshCurrentPatientInput();
			}
		} else {
			currentPatientViewer.setInput(Collections.emptyList());
		}
	}

	private void generalRefresh() {
		if (generalViewer.getTable().isVisible()) {
			refreshGeneralInput();
		}
		if (generalPatientViewer.getTable().isVisible()) {
			refreshGeneralPatientInput();
		}
	}

	private void refreshCurrentPatientInput() {
		if (actPatient != null) {
			List<Reminder> reminders = Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser, showOnlyDueReminders,
					filterDueDateDays, actPatient, false);

			Query<Reminder> query = new Query<>(Reminder.class, null, null, Reminder.TABLENAME,
					new String[] { Reminder.FLD_DUE, Reminder.FLD_PRIORITY, Reminder.FLD_ACTION_TYPE,
							Reminder.FLD_CREATOR, Reminder.FLD_KONTAKT_ID });
			if (showSelfCreatedReminders) {
				query.add(Reminder.FLD_CREATOR, Query.EQUALS, CoreHub.actUser.getId());
				query.add(Reminder.FLD_KONTAKT_ID, Query.EQUALS, actPatient.getId());
				if (filterDueDateDays != -1) {
					applyDueDateFilter(query);
				}
				reminders.addAll(query.execute());
			}
			Display.getDefault().asyncExec(() -> {
				if (currentPatientViewer != null && !currentPatientViewer.getTable().isDisposed()) {
					currentPatientViewer.setInput(reminders);
					if (reminders.size() < 5) {
						((GridData) currentPatientViewer.getTable().getLayoutData()).heightHint = 100;
					} else {
						((GridData) currentPatientViewer.getTable().getLayoutData()).heightHint = SWT.DEFAULT;
					}
				}
			});
		}
	}

	private void refreshGeneralPatientInput() {
		HashSet<Reminder> uniqueReminders = new HashSet<>();
		uniqueReminders.addAll(Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser, showOnlyDueReminders,
				filterDueDateDays, null, false));

		Query<Reminder> query = new Query<>(Reminder.class, null, null, Reminder.TABLENAME,
				new String[] { Reminder.FLD_DUE, Reminder.FLD_PRIORITY, Reminder.FLD_ACTION_TYPE, Reminder.FLD_CREATOR,
						Reminder.FLD_KONTAKT_ID });
		if (showSelfCreatedReminders) {
			query.add(Reminder.FLD_CREATOR, Query.EQUALS, CoreHub.actUser.getId());
			if (filterDueDateDays != -1) {
				applyDueDateFilter(query);
			}
			uniqueReminders.addAll(query.execute());
		}
		List<Reminder> filteredReminders = uniqueReminders.parallelStream().filter(r -> r.isPatientRelated())
				.collect(Collectors.toList());
		Display.getDefault().asyncExec(() -> {
			if (generalPatientViewer != null && !generalPatientViewer.getTable().isDisposed()) {
				generalPatientViewer.setInput(filteredReminders);
				if (generalPatientViewer.getTable().getItemCount() < 5) {
					((GridData) generalPatientViewer.getTable().getLayoutData()).heightHint = 100;
				} else {
					((GridData) generalPatientViewer.getTable().getLayoutData()).heightHint = SWT.DEFAULT;
				}
			}
		});
	}

	private void refreshGeneralInput() {
		HashSet<Reminder> uniqueReminders = new HashSet<>();
		uniqueReminders.addAll(Reminder.findOpenRemindersResponsibleFor(CoreHub.actUser, showOnlyDueReminders,
				filterDueDateDays, null, false));

		Query<Reminder> query = new Query<>(Reminder.class, null, null, Reminder.TABLENAME,
				new String[] { Reminder.FLD_DUE, Reminder.FLD_PRIORITY, Reminder.FLD_ACTION_TYPE, Reminder.FLD_CREATOR,
						Reminder.FLD_KONTAKT_ID });
		if (showSelfCreatedReminders) {
			query.add(Reminder.FLD_CREATOR, Query.EQUALS, CoreHub.actUser.getId());
			if (filterDueDateDays != -1) {
				applyDueDateFilter(query);
			}
			uniqueReminders.addAll(query.execute());
		}
		List<Reminder> filteredReminders = uniqueReminders.parallelStream().filter(r -> !r.isPatientRelated())
				.collect(Collectors.toList());
		Display.getDefault().asyncExec(() -> {
			if (generalViewer != null && !generalViewer.getTable().isDisposed()) {
				generalViewer.setInput(filteredReminders);
				if (filteredReminders.size() < 5) {
					((GridData) generalViewer.getTable().getLayoutData()).heightHint = 100;
				} else {
					((GridData) generalViewer.getTable().getLayoutData()).heightHint = SWT.DEFAULT;
				}
			}
		});
	}

	private void applyDueDateFilter(Query<Reminder> qbe) {
		TimeTool dueDateDays = new TimeTool();
		dueDateDays.addDays(filterDueDateDays);
		qbe.add(Reminder.FLD_DUE, Query.NOT_EQUAL, "");
		qbe.add(Reminder.FLD_DUE, Query.LESS_OR_EQUAL, dueDateDays.toString(TimeTool.DATE_COMPACT));
	}

	private TableViewerColumn createTypeColumn(TableViewer viewer, int width, int columnIndex) {
		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tableColumn = viewerColumn.getColumn();
		tableColumn.setWidth(width);
		tableColumn.addSelectionListener(getSelectionAdapter(viewer, tableColumn, columnIndex));
		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				return "";
			}

			@Override
			public Image getImage(Object element) {
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
			@Override
			public String getText(Object element) {
				Reminder reminder = (Reminder) element;
				return reminder.get(Reminder.FLD_DUE);
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
				Reminder reminder = (Reminder) element;
				Kontakt k = Kontakt.load(reminder.get(Reminder.FLD_KONTAKT_ID));
				return k.getLabel(false);
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
				Reminder reminder = (Reminder) element;
				String[] vals = reminder.get(true, Reminder.FLD_MESSAGE, Reminder.FLD_SUBJECT);
				return (vals[1].length() > 0) ? vals[1] : vals[0];
			}

			@Override
			public String getToolTipText(Object element) {
				return getText(element);
			}
		});
		return viewerColumn;
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

	private class ReminderComparator extends ViewerComparator implements Comparator<Reminder> {

		private int column;

		private int direction;

		public ReminderComparator() {
			column = -1;
			direction = SWT.DOWN;
		}

		@Override
		public int compare(Reminder r1, Reminder r2) {
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
		public int compare(Viewer viewer, Object e1, Object e2) {
			return compare((Reminder) e1, (Reminder) e2);
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

		private void createContent() {
			setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			setLayout(new FillLayout());

			manager = new ToolBarManager(SWT.WRAP);
			manager.add(new Action("aktueller Patient", Action.AS_CHECK_BOX) {
				@Override
				public String getId() {
					return "reminderlistsview.selection.currentpatient";
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
				}
			});
			manager.add(new Action("nicht aktueller Patient", Action.AS_CHECK_BOX) {
				@Override
				public String getId() {
					return "reminderlistsview.selection.generalpatient";
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
				}
			});
			manager.add(new Action("allgemein", Action.AS_CHECK_BOX) {

				@Override
				public String getId() {
					return "reminderlistsview.selection.general";
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
				}
			});
			manager.createControl(this);
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

		public void setText(String text) {
			header.setText(text);
			ReminderListsView.this.viewParent.layout(true, true);
		}
	}
}
