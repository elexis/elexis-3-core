package ch.elexis.core.ui.tasks.parts;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.deferred.DeferredContentProvider;
import org.eclipse.jface.viewers.deferred.SetModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.ui.e4.parts.IRefreshablePart;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.tasks.internal.TaskModelServiceHolder;
import ch.elexis.core.ui.tasks.parts.handlers.TaskPartSystemFilterHandler;

public class TaskLogPart implements IDoubleClickListener, IRefreshablePart {
	
	@Inject
	private ESelectionService selectionService;
	
	@Inject
	private EModelService modelService;
	
	@Inject
	private MApplication application;
	
	@Inject
	private EPartService partService;
	
	private Composite tableViewerComposite;
	private Table tableResults;
	private TableViewer tableViewerResults;
	private SetModel inputModel;
	private DeferredContentProvider contentProvider;
	
	// TODO only Admin should see all, else only current user
	
	@PostConstruct
	public void createControls(Composite parent, EMenuService menuService){
		parent.setLayout(new GridLayout(1, false));
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		GridLayout gl_composite = new GridLayout(1, false);
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		composite.setLayout(gl_composite);
		
		tableViewerComposite = new Composite(parent, SWT.NONE);
		tableViewerComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcLayout = new TableColumnLayout();
		tableViewerComposite.setLayout(tcLayout);
		
		tableViewerResults = new TableViewer(tableViewerComposite,
			SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
		tableResults = tableViewerResults.getTable();
		tableResults.setHeaderVisible(true);
		tableResults.setLinesVisible(true);
		contentProvider = new DeferredContentProvider(ITaskComparators.ofRunAt());
		tableViewerResults.setContentProvider(contentProvider);
		tableViewerResults.setUseHashlookup(true);
		tableViewerResults.addDoubleClickListener(this);
		
		TableViewerColumn tvcTaskDescriptor = new TableViewerColumn(tableViewerResults, SWT.NONE);
		tvcTaskDescriptor.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				ITask task = (ITask) element;
				String referenceId = task.getTaskDescriptor().getReferenceId();
				if (referenceId != null) {
					return referenceId;
				}
				return task.getTaskDescriptor().getId();
			}
		});
		TableColumn tblclmnTaskDescriptor = tvcTaskDescriptor.getColumn();
		tcLayout.setColumnData(tblclmnTaskDescriptor, new ColumnPixelData(110, true, true));
		tblclmnTaskDescriptor.setText("Task");
		tblclmnTaskDescriptor.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				tableResults.setSortColumn(tblclmnTaskDescriptor);
				if (tableResults.getSortDirection() == SWT.DOWN) {
					contentProvider.setSortOrder(ITaskComparators.ofTaskDescriptorId());
					tableResults.setSortDirection(SWT.UP);
				} else {
					contentProvider.setSortOrder(ITaskComparators.ofTaskDescriptorId().reversed());
					tableResults.setSortDirection(SWT.DOWN);
				}
			}
		});
		
		TableViewerColumn tvcTrigger = new TableViewerColumn(tableViewerResults, SWT.NONE);
		tvcTrigger.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element){
				ITask task = (ITask) element;
				switch (task.getTriggerEvent()) {
				case MANUAL:
					return Images.IMG_HAND.getImage();
				case CRON:
					return Images.IMG_CLOCK.getImage();
				case SYSTEM_EVENT:
					return Images.IMG_SYSTEM_MONITOR.getImage();
				case OTHER_TASK:
					return Images.IMG_TASK.getImage();
				default:
					break;
				}
				return super.getImage(element);
			}
			
			@Override
			public String getText(Object element){
				return null;
			}
		});
		TableColumn tblclmnTrigger = tvcTrigger.getColumn();
		tblclmnTrigger.setAlignment(SWT.CENTER);
		tcLayout.setColumnData(tblclmnTrigger, new ColumnPixelData(22, true, false));
		
		TableViewerColumn tvcStartTime = new TableViewerColumn(tableViewerResults, SWT.NONE);
		tvcStartTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				ITask task = (ITask) element;
				return TimeUtil.formatSafe(task.getRunAt());
			}
		});
		TableColumn tblclmnStartTime = tvcStartTime.getColumn();
		tcLayout.setColumnData(tblclmnStartTime, new ColumnPixelData(110, true, true));
		tblclmnStartTime.setText("Startzeit");
		tblclmnStartTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				tableResults.setSortColumn(tblclmnStartTime);
				if (tableResults.getSortDirection() == SWT.DOWN) {
					contentProvider.setSortOrder(ITaskComparators.ofRunAt());
					tableResults.setSortDirection(SWT.UP);
				} else {
					contentProvider.setSortOrder(ITaskComparators.ofRunAt().reversed());
					tableResults.setSortDirection(SWT.DOWN);
				}
			}
		});
		tableResults.setSortColumn(tblclmnStartTime);
		tableResults.setSortDirection(SWT.DOWN);
		
		TableViewerColumn tvcFinishTime = new TableViewerColumn(tableViewerResults, SWT.NONE);
		tvcFinishTime.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				ITask task = (ITask) element;
				return TimeUtil.formatSafe(task.getFinishedAt());
			}
		});
		TableColumn tblclmnFinishTime = tvcFinishTime.getColumn();
		tcLayout.setColumnData(tblclmnFinishTime, new ColumnPixelData(100, true, true));
		tblclmnFinishTime.setText("Endzeit");
		tblclmnFinishTime.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				tableResults.setSortColumn(tblclmnFinishTime);
				if (tableResults.getSortDirection() == SWT.DOWN) {
					contentProvider.setSortOrder(ITaskComparators.ofFinishedAt());
					tableResults.setSortDirection(SWT.UP);
				} else {
					contentProvider.setSortOrder(ITaskComparators.ofFinishedAt().reversed());
					tableResults.setSortDirection(SWT.DOWN);
				}
			}
		});
		
		TableViewerColumn tvcState = new TableViewerColumn(tableViewerResults, SWT.NONE);
		tvcState.setLabelProvider(TaskResultLabelProvider.getInstance());
		TableColumn tblclmnState = tvcState.getColumn();
		tcLayout.setColumnData(tblclmnState, new ColumnPixelData(22, true, false));
		tblclmnState.setText("");
		
		// OWNER
		TableViewerColumn tvcOwner = new TableViewerColumn(tableViewerResults, SWT.NONE);
		tvcOwner.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				ITask task = (ITask) element;
				IUser owner = task.getTaskDescriptor().getOwner();
				return (owner != null) ? owner.getId() : "NO-OWNER";
			}
		});
		TableColumn tblclmnOwner = tvcOwner.getColumn();
		tcLayout.setColumnData(tblclmnOwner, new ColumnPixelData(70, true, true));
		tblclmnOwner.setText("User");
		tblclmnOwner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				tableResults.setSortColumn(tblclmnOwner);
				if (tableResults.getSortDirection() == SWT.DOWN) {
					contentProvider.setSortOrder(ITaskComparators.ofOwner());
					tableResults.setSortDirection(SWT.UP);
				} else {
					contentProvider.setSortOrder(ITaskComparators.ofOwner().reversed());
					tableResults.setSortDirection(SWT.DOWN);
				}
			}
		});
		
		// RUNNER
		TableViewerColumn tvcRunner = new TableViewerColumn(tableViewerResults, SWT.NONE);
		tvcRunner.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				ITask task = (ITask) element;
				return task.getRunner();
			}
		});
		TableColumn tblclmnRunner = tvcRunner.getColumn();
		tcLayout.setColumnData(tblclmnRunner, new ColumnPixelData(80, true, true));
		tblclmnRunner.setText("Runner");
		tblclmnRunner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				tableResults.setSortColumn(tblclmnRunner);
				if (tableResults.getSortDirection() == SWT.DOWN) {
					contentProvider.setSortOrder(ITaskComparators.ofRunner());
					tableResults.setSortDirection(SWT.UP);
				} else {
					contentProvider.setSortOrder(ITaskComparators.ofRunner().reversed());
					tableResults.setSortDirection(SWT.DOWN);
				}
			}
		});
		
		tableViewerResults.addSelectionChangedListener(event -> {
			IStructuredSelection selection = tableViewerResults.getStructuredSelection();
			selectionService.setSelection(selection.toList());
		});
		
		menuService.registerContextMenu(tableResults,
			"ch.elexis.core.ui.tasks.popupmenu.tableresults");
		
		inputModel = new SetModel();
		tableViewerResults.setInput(inputModel);
		
		refresh();
	}
	
	public DeferredContentProvider getContentProvider(){
		return contentProvider;
	}
	
	@Override
	public void refresh(Map<Object, Object> filterParameters){
		// TODO only show all if Administrator or owner (-> TaskDescriptor)
		Job job = Job.create("Update table", (ICoreRunnable) monitor -> {
			IQuery<ITask> taskQuery = TaskModelServiceHolder.get().getQuery(ITask.class);
			taskQuery.orderBy(ModelPackage.Literals.IDENTIFIABLE__LASTUPDATE, ORDER.DESC);
			boolean showSystemTasks =
				filterParameters.get(TaskPartSystemFilterHandler.SHOW_SYSTEM_TASKS) != null
						? (boolean) filterParameters
							.get(TaskPartSystemFilterHandler.SHOW_SYSTEM_TASKS)
						: false;
			if (!showSystemTasks) {
				taskQuery.and(ch.elexis.core.tasks.model.ModelPackage.Literals.ITASK__SYSTEM,
					COMPARATOR.EQUALS, false);
			}
			List<ITask> results = taskQuery.execute();
			inputModel.set(results.toArray());
		});
		job.schedule();
	}
	
	@Focus
	public void setFocus(){
		tableResults.setFocus();
	}
	
	@Optional
	@Inject
	void deleteTask(@UIEventTopic(ElexisEventTopics.EVENT_DELETE)
	ITask iTask){
		inputModel.removeAll(new ITask[] {
			iTask
		});
		tableResults.deselectAll();
		refresh();
	}
	
	@Override
	public void doubleClick(DoubleClickEvent event){
		ITask selectedTask = (ITask) ((StructuredSelection) event.getSelection()).getFirstElement();
		MPart taskDetailPart =
			partService.createPart("ch.elexis.core.ui.tasks.partdescriptor.taskdetail");
		taskDetailPart.getTransientData().put("task", selectedTask);
		
		MPartStack detailPartStack = (MPartStack) modelService
			.find("ch.elexis.core.ui.tasks.partstack.details", application);
		if (detailPartStack != null && detailPartStack.isVisible()) {
			detailPartStack.getChildren().add(taskDetailPart);
			partService.activate(taskDetailPart);
		} else {
			partService.showPart(taskDetailPart, PartState.CREATE);
		}
	}
	
	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT)
	boolean currentState){
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
