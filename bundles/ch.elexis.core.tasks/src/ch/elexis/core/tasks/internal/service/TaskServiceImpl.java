package ch.elexis.core.tasks.internal.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IUser;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.tasks.internal.model.impl.TaskDescriptor;
import ch.elexis.core.tasks.internal.service.fs.WatchServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

@Component(immediate = true)
public class TaskServiceImpl implements ITaskService {
	
	private Logger logger;
	
	private IModelService taskModelService;
	
	private ExecutorService executorService;
	private List<ITask> runningTasks;
	
	private WatchServiceHolder watchServiceHolder;
	//TODO CronService
	//TODO EventService
	//TODO OtherTaskService -> this
	
	//private List<ITaskDescriptor> incurredTasks;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.tasks.model)")
	private void setModelService(IModelService modelService){
		taskModelService = modelService;
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, bind = "bindRunnableWithContextFactory", unbind = "unbindRunnableWithContextFactory")
	private List<IIdentifiedRunnableFactory> runnableWithContextFactories;
	
	protected void bindRunnableWithContextFactory(
		IIdentifiedRunnableFactory runnableWithContextFactory){
		if (runnableWithContextFactories == null) {
			runnableWithContextFactories = new ArrayList<>();
		}
		runnableWithContextFactories.add(runnableWithContextFactory);
	}
	
	protected void unbindRunnableWithContextFactory(
		IIdentifiedRunnableFactory runnableWithContextFactory){
		runnableWithContextFactories.remove(runnableWithContextFactory);
	}
	
	@Activate
	private void activateComponent(){
		logger = LoggerFactory.getLogger(getClass());
		logger.debug("Activating");
		
		//		incurredTasks = Collections.synchronizedList(new ArrayList<>());
		runningTasks = Collections.synchronizedList(new ArrayList<>());
		executorService = Executors.newCachedThreadPool();
		
		watchServiceHolder = new WatchServiceHolder(this);
		if (watchServiceHolder.triggerIsAvailable()) {
			watchServiceHolder.startPolling();
		}
		
		reloadIncurredTasks();
		// TODO whats my name?
		// Instantiate the trigger handlers
	}
	
	@Deactivate
	private void deactivateComponent(){
		watchServiceHolder.stopPolling();
	}
	
	private void reloadIncurredTasks(){
		// TODO on system startup, load the ITaskDescriptors we have to incurr
	}
	
	/**
	 * Become responsible for executing this task when required. This includes asserting that the
	 * necessary execution events are generated.
	 * 
	 * @param task
	 * @throws TaskException
	 */
	private void incur(ITaskDescriptor taskDescriptor) throws TaskException{
		if (TaskTriggerType.FILESYSTEM_CHANGE == taskDescriptor.getTriggerType()) {
			watchServiceHolder.incur(taskDescriptor);
		}
	}
	
	/**
	 * Release responsibility for executing this task when required.
	 * 
	 * @param taskDescriptor
	 */
	private void release(ITaskDescriptor taskDescriptor){
		if (TaskTriggerType.FILESYSTEM_CHANGE == taskDescriptor.getTriggerType()) {
			watchServiceHolder.release(taskDescriptor);
		}
	}
	
	@Override
	public ITaskDescriptor createTaskDescriptor(IUser owner,
		IIdentifiedRunnable runnableWithContext) throws TaskException{
		
		if (owner == null || runnableWithContext == null) {
			throw new TaskException(TaskException.PARAMETERS_MISSING, null);
		}
		
		ITaskDescriptor taskDescriptor = taskModelService.create(ITaskDescriptor.class);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setRunnableWithContextId(runnableWithContext.getId());
		//		TODO taskDescriptor.setRunner("thatshouldbeme");
		
		saveTaskDescriptor(taskDescriptor);
		
		return taskDescriptor;
	}
	
	void notify(ITask runningTask){
		if (runningTask.isFinished()) {
			// TODO persistence; user notification; tasks that are triggered by this task
			runningTasks.remove(runningTask);
		}
		System.out.println("notify " + runningTask);
	}
	
	@Override
	public ITask trigger(ITaskDescriptor taskDescriptor, IProgressMonitor progressMonitor,
		TaskTriggerType triggerType, Map<String, String> runContext) throws TaskException{
		
		ITask task = new Task(this, taskDescriptor, triggerType, progressMonitor);
		if (runContext != null) {
			task.getRunContext().putAll(runContext);
		}
		try {
			executorService.execute((Runnable) task);
			runningTasks.add(task);
		} catch (RejectedExecutionException re) {
			throw new TaskException(TaskException.EXECUTION_REJECTED, re);
		}
		
		return task;
	}
	
	@Override
	public IIdentifiedRunnable instantiateRunnableById(String runnableId) throws TaskException{
		if (runnableId == null || runnableId.length() == 0) {
			throw new TaskException(TaskException.RWC_INVALID_ID, null);
		}
		
		Optional<IIdentifiedRunnable> result = runnableWithContextFactories.stream()
			.map(rwcf -> rwcf.createRunnableWithContext(runnableId)).filter(Objects::nonNull)
			.findFirst();
		if (result.isPresent()) {
			return result.get();
		}
		
		throw new TaskException(TaskException.RWC_NO_INSTANCE_FOUND, null);
	}
	
	@Override
	public void saveTaskDescriptor(ITaskDescriptor taskDescriptor) throws TaskException{
		boolean save = taskModelService.save((TaskDescriptor) taskDescriptor);
		if (!save) {
			throw new TaskException(TaskException.PERSISTENCE_ERROR, null);
		}
	}
	
	@Override
	public void setActive(ITaskDescriptor taskDescriptor, boolean active) throws TaskException{
		taskDescriptor.setActive(active);
		saveTaskDescriptor(taskDescriptor);
		
		if (active) {
			incur(taskDescriptor);
		} else {
			release(taskDescriptor);
		}
	}
	
}
