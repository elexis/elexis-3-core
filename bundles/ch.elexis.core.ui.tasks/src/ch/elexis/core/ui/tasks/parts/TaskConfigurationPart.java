
package ch.elexis.core.ui.tasks.parts;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.ModelPackage;
import ch.elexis.core.ui.e4.parts.IRefreshablePart;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.tasks.internal.TaskModelServiceHolder;
import ch.elexis.core.ui.tasks.parts.controls.AbstractTaskDescriptorConfigurationComposite;
import ch.elexis.core.ui.tasks.parts.controls.GeneralConfigurationComposite;
import ch.elexis.core.ui.tasks.parts.controls.RunnableAndContextConfigurationComposite;
import ch.elexis.core.ui.tasks.parts.controls.TaskTriggerTypeConfigurationComposite;
import ch.elexis.core.ui.tasks.parts.handlers.TaskPartSystemFilterHandler;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.inject.Named;

public class TaskConfigurationPart implements IRefreshablePart {

	@Inject
	private ITaskService taskService;

	@Inject
	private ESelectionService selectionService;

	private Table tableDescriptors;

	private GeneralConfigurationComposite gcp;
	private TaskTriggerTypeConfigurationComposite tttcp;
	private RunnableAndContextConfigurationComposite raccp;

	private TableViewer tvTaskDescriptors;
	private TaskLogComparator comparator;

	// TODO only Admin should see all, else only current user

	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {
		parent.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(parent, SWT.NONE);
		GridData gd_composite = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gd_composite.heightHint = 300;
		composite.setLayoutData(gd_composite);
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);

		tvTaskDescriptors = new TableViewer(composite, SWT.BORDER | SWT.FULL_SELECTION);
		tableDescriptors = tvTaskDescriptors.getTable();
		tableDescriptors.setHeaderVisible(true);
		tableDescriptors.setLinesVisible(true);
		tvTaskDescriptors.setContentProvider(ArrayContentProvider.getInstance());
		tvTaskDescriptors.addSelectionChangedListener(
				sel -> selectionChanged((ITaskDescriptor) sel.getStructuredSelection().getFirstElement()));
		comparator = new TaskLogComparator();
		tvTaskDescriptors.setComparator(comparator);

		TableViewerColumn tvcStatus = new TableViewerColumn(tvTaskDescriptors, SWT.NONE);
		TableColumn tblclmnStatus = tvcStatus.getColumn();
		tcl_composite.setColumnData(tblclmnStatus, new ColumnPixelData(16, false, true));
		tblclmnStatus.setText("status");
		tblclmnStatus.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(1);
				refresh();
			}
		});
		tvcStatus.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ITaskDescriptor td = (ITaskDescriptor) element;
				if (td.getTransientData().get("incurred") != null) {
					return "INC"; //$NON-NLS-1$
				}
				return td.isActive() ? "ACT" : "NACT"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		});

		TableViewerColumn tvcReferenceId = new TableViewerColumn(tvTaskDescriptors, SWT.NONE);
		TableColumn tblclmnReferenceId = tvcReferenceId.getColumn();
		tcl_composite.setColumnData(tblclmnReferenceId, new ColumnPixelData(150, true, true));
		tblclmnReferenceId.setText("referenceId");
		tblclmnReferenceId.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(1);
				refresh();
			}
		});
		tvcReferenceId.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ITaskDescriptor td = (ITaskDescriptor) element;
				return td.getReferenceId();
			}
		});

		TableViewerColumn tvcRunnableId = new TableViewerColumn(tvTaskDescriptors, SWT.NONE);
		TableColumn tblclmnRunnableId = tvcRunnableId.getColumn();
		tcl_composite.setColumnData(tblclmnRunnableId, new ColumnPixelData(150, true, true));
		tblclmnRunnableId.setText("runnableId");
		tblclmnRunnableId.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(2);
				refresh();
			}
		});
		tvcRunnableId.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ITaskDescriptor td = (ITaskDescriptor) element;
				return td.getIdentifiedRunnableId();
			}
		});

		TableViewerColumn tvcOwner = new TableViewerColumn(tvTaskDescriptors, SWT.NONE);
		TableColumn tblclmnOwner = tvcOwner.getColumn();
		tcl_composite.setColumnData(tblclmnOwner, new ColumnPixelData(150, true, true));
		tblclmnOwner.setText("Owner");
		tblclmnOwner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(3);
				refresh();
			}
		});
		tvcOwner.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				ITaskDescriptor td = (ITaskDescriptor) element;
				String ownerId = td.getOwner() != null ? td.getOwner().getId() : "NO-OWNER";
				return ownerId + "@" + td.getRunner(); //$NON-NLS-1$
			}

			@Override
			public Color getForeground(Object element) {
				ITaskDescriptor td = (ITaskDescriptor) element;
				return (td.getOwner() == null) ? Display.getCurrent().getSystemColor(SWT.COLOR_RED) : null;
			};
		});

		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tabFolder.addListener(SWT.Selection, event -> {
			Control control = ((TabItem) event.item).getControl();
			if (control instanceof AbstractTaskDescriptorConfigurationComposite) {
				((AbstractTaskDescriptorConfigurationComposite) control).refresh();
			}
		});

		// GENERAL
		TabItem tbtmGeneral = new TabItem(tabFolder, SWT.NONE);
		tbtmGeneral.setText("general");

		gcp = new GeneralConfigurationComposite(taskService, tabFolder, SWT.NONE);
		tbtmGeneral.setControl(gcp);

		// TRIGGER
		TabItem tbtmTrigger = new TabItem(tabFolder, SWT.NONE);
		tbtmTrigger.setText("trigger");
		tttcp = new TaskTriggerTypeConfigurationComposite(tabFolder, SWT.None);
		tbtmTrigger.setControl(tttcp);

		// ACTION (Runnable and RunContext)
		TabItem tbtmAction = new TabItem(tabFolder, SWT.NONE);
		tbtmAction.setText("action");
		raccp = new RunnableAndContextConfigurationComposite(tabFolder, SWT.NONE, taskService);
		tbtmAction.setControl(raccp);

		menuService.registerContextMenu(tableDescriptors, "ch.elexis.core.ui.tasks.popupmenu.tabledescriptors"); //$NON-NLS-1$

		refresh();
	}

	private void selectionChanged(ITaskDescriptor taskDescriptor) {
		selectionService.setSelection(taskDescriptor);
		gcp.setSelection(taskDescriptor);
		tttcp.setSelection(taskDescriptor);
		raccp.setSelection(taskDescriptor);
	}

	@Focus
	public boolean setFocus() {
		return tableDescriptors.setFocus();
	}

	@Optional
	@Inject
	void refreshTaskDescriptor(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) ITaskDescriptor taskDescriptor) {
		if (taskDescriptor != null) {
			tvTaskDescriptors.update(taskDescriptor, null);
		}
	}

	@Override
	public void refresh(Map<Object, Object> filterParameters) {
		IQuery<ITaskDescriptor> taskQuery = TaskModelServiceHolder.get().getQuery(ITaskDescriptor.class, true, false);
		boolean showSystemTasks = filterParameters.get(TaskPartSystemFilterHandler.SHOW_SYSTEM_TASKS) != null
				? (boolean) filterParameters.get(TaskPartSystemFilterHandler.SHOW_SYSTEM_TASKS)
				: false;
		if (!showSystemTasks) {
			taskQuery.and(ModelPackage.Literals.ITASK_DESCRIPTOR__SYSTEM, COMPARATOR.EQUALS, false);
		}
		List<ITaskDescriptor> taskDescriptors = taskQuery.execute();

		Set<String> incurredTaskIds = taskService.getIncurredTasks().stream().map(ict -> ict.getId())
				.collect(Collectors.toSet());
		taskDescriptors.forEach(td -> {
			if (incurredTaskIds.contains(td.getId())) {
				td.getTransientData().put("incurred", Boolean.TRUE.toString());
			}
		});

		tvTaskDescriptors.setInput(taskDescriptors);
		tvTaskDescriptors.refresh(true);
	}

	@Optional
	@Inject
	void deleteTaskDescriptor(@UIEventTopic(ElexisEventTopics.EVENT_DELETE) ITaskDescriptor taskDescriptor) {
		refresh();
	}

	@Optional
	@Inject
	void createTaskDescriptor(@UIEventTopic(ElexisEventTopics.EVENT_CREATE) ITaskDescriptor taskDescriptor) {
		refresh();
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	public class TaskLogComparator extends ViewerComparator {

		private int propertyIndex;
		private int direction = 1;

		public TaskLogComparator() {
			this.propertyIndex = 0;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; so lt's toggle the direction
				direction *= -1;
			}
			this.propertyIndex = column;
		}

		@Override
		public int compare(Viewer viewer, Object o1, Object o2) {
			ITaskDescriptor ts1 = (ITaskDescriptor) o1;
			ITaskDescriptor ts2 = (ITaskDescriptor) o2;

			switch (propertyIndex) {
			case 0:
				boolean status1 = ts1.isActive();
				boolean status2 = ts2.isActive();
				return Objects.compare(status1, status2, Comparator.nullsFirst(Comparator.naturalOrder())) * direction;

			case 1:
				String ref1 = ts1.getReferenceId();
				String ref2 = ts2.getReferenceId();
				return Objects.compare(ref1, ref2, Comparator.nullsFirst(Comparator.naturalOrder())) * direction;

			case 2:
				String run1 = ts1.getIdentifiedRunnableId() != null ? ts1.getIdentifiedRunnableId() : "";
				String run2 = ts2.getIdentifiedRunnableId() != null ? ts2.getIdentifiedRunnableId() : "";
				return Objects.compare(run1, run2, Comparator.nullsFirst(Comparator.naturalOrder())) * direction;

			case 3:
				String own1 = ts1.getOwner() != null ? ts1.getOwner().getLabel() : "";
				String own2 = ts2.getOwner() != null ? ts2.getOwner().getLabel() : "";
				return Objects.compare(own1, own2, Comparator.nullsFirst(Comparator.naturalOrder())) * direction;
			}

			return super.compare(viewer, o1, o2);

		}

	}
}