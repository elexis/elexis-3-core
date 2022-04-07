
package ch.elexis.core.ui.tasks.parts;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

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
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
import ch.elexis.core.ui.tasks.parts.controls.GeneralConfigurationComposite;
import ch.elexis.core.ui.tasks.parts.controls.RunnableAndContextConfigurationComposite;
import ch.elexis.core.ui.tasks.parts.controls.TaskTriggerTypeConfigurationComposite;
import ch.elexis.core.ui.tasks.parts.handlers.TaskPartSystemFilterHandler;

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
	
	// TODO only Admin should see all, else only current user
	
	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService){
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
		tvTaskDescriptors.addSelectionChangedListener(sel -> selectionChanged(
			(ITaskDescriptor) sel.getStructuredSelection().getFirstElement()));
		
		TableViewerColumn tvcStatus = new TableViewerColumn(tvTaskDescriptors, SWT.NONE);
		TableColumn tblclmnStatus = tvcStatus.getColumn();
		tcl_composite.setColumnData(tblclmnStatus, new ColumnPixelData(16, false, true));
		tblclmnStatus.setText("status");
		tvcStatus.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				ITaskDescriptor td = (ITaskDescriptor) element;
				return td.isActive() ? "ACT" : "NACT";
			}
		});
		
		TableViewerColumn tvcReferenceId = new TableViewerColumn(tvTaskDescriptors, SWT.NONE);
		TableColumn tblclmnReferenceId = tvcReferenceId.getColumn();
		tcl_composite.setColumnData(tblclmnReferenceId, new ColumnPixelData(150, true, true));
		tblclmnReferenceId.setText("referenceId");
		tvcReferenceId.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				ITaskDescriptor td = (ITaskDescriptor) element;
				return td.getReferenceId();
			}
		});
		
		TableViewerColumn tvcRunnableId = new TableViewerColumn(tvTaskDescriptors, SWT.NONE);
		TableColumn tblclmnRunnableId = tvcRunnableId.getColumn();
		tcl_composite.setColumnData(tblclmnRunnableId, new ColumnPixelData(150, true, true));
		tblclmnRunnableId.setText("runnableId");
		tvcRunnableId.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				ITaskDescriptor td = (ITaskDescriptor) element;
				return td.getIdentifiedRunnableId();
			}
		});
		
		TableViewerColumn tvcOwner = new TableViewerColumn(tvTaskDescriptors, SWT.NONE);
		TableColumn tblclmnOwner = tvcOwner.getColumn();
		tcl_composite.setColumnData(tblclmnOwner, new ColumnPixelData(150, true, true));
		tblclmnOwner.setText("Owner");
		tvcOwner.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				ITaskDescriptor td = (ITaskDescriptor) element;
				String ownerId = td.getOwner() != null ? td.getOwner().getId() : "NO-OWNER";
				return ownerId + "@" + td.getRunner();
			}
			
			@Override
			public Color getForeground(Object element){
				ITaskDescriptor td = (ITaskDescriptor) element;
				return (td.getOwner() == null) ? Display.getCurrent().getSystemColor(SWT.COLOR_RED)
						: null;
			};
		});
		
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		// GENERAL
		TabItem tbtmGeneral = new TabItem(tabFolder, SWT.NONE);
		tbtmGeneral.setText("general");
		
		gcp = new GeneralConfigurationComposite(tabFolder, SWT.NONE);
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
		
		menuService.registerContextMenu(tableDescriptors,
			"ch.elexis.core.ui.tasks.popupmenu.tabledescriptors");
		
		refresh();
	}
	
	private void selectionChanged(ITaskDescriptor taskDescriptor){
		selectionService.setSelection(taskDescriptor);
		gcp.setSelection(taskDescriptor);
		tttcp.setSelection(taskDescriptor);
		raccp.setSelection(taskDescriptor);
	}
	
	@Focus
	public boolean setFocus(){
		return tableDescriptors.setFocus();
	}
	
	@Optional
	@Inject
	void refreshTaskDescriptor(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE)
	ITaskDescriptor taskDescriptor){
		if (taskDescriptor != null) {
			tvTaskDescriptors.update(taskDescriptor, null);
		}
	}
	
	@Override
	public void refresh(Map<Object, Object> filterParameters){
		IQuery<ITaskDescriptor> taskQuery =
			TaskModelServiceHolder.get().getQuery(ITaskDescriptor.class, true, false);
		boolean showSystemTasks =
			filterParameters.get(TaskPartSystemFilterHandler.SHOW_SYSTEM_TASKS) != null
					? (boolean) filterParameters.get(TaskPartSystemFilterHandler.SHOW_SYSTEM_TASKS)
					: false;
		if (!showSystemTasks) {
			taskQuery.and(ModelPackage.Literals.ITASK_DESCRIPTOR__SYSTEM, COMPARATOR.EQUALS, false);
		}
		List<ITaskDescriptor> taskDescriptors = taskQuery.execute();
		tvTaskDescriptors.setInput(taskDescriptors);
		tvTaskDescriptors.refresh(true);
	}
	
	@Optional
	@Inject
	void deleteTaskDescriptor(@UIEventTopic(ElexisEventTopics.EVENT_DELETE)
	ITaskDescriptor taskDescriptor){
		refresh();
	}
	
	@Optional
	@Inject
	void createTaskDescriptor(@UIEventTopic(ElexisEventTopics.EVENT_CREATE)
	ITaskDescriptor taskDescriptor){
		refresh();
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}