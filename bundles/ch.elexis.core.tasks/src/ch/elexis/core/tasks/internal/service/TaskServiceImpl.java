package ch.elexis.core.tasks.internal.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IUser;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.tasks.internal.model.impl.Task;
import ch.elexis.core.tasks.internal.model.impl.TaskDescriptor;
import ch.elexis.core.tasks.internal.service.fs.WatchServiceHolder;
import ch.elexis.core.tasks.internal.service.quartz.QuartzExecutor;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.ModelPackage;
import ch.elexis.core.tasks.model.TaskTriggerType;

@Component(immediate = true)
public class TaskServiceImpl implements ITaskService {
	
	private Logger logger;
	
	private IModelService taskModelService;
	
	private QuartzExecutor quartzExecutor;
	
	private ExecutorService parallelExecutorService;
	private ExecutorService singletonExecutorService;
	private List<ITask> runningTasks;
	
	private WatchServiceHolder watchServiceHolder;
	//TODO EventService
	//TODO OtherTaskService -> this
	
	//private List<ITaskDescriptor> incurredTasks;
	
	@Reference
	private IContextService contextService;
	
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
		
		runningTasks = Collections.synchronizedList(new ArrayList<>());
		parallelExecutorService = Executors.newCachedThreadPool();
		singletonExecutorService = Executors.newSingleThreadExecutor();
		
		quartzExecutor = new QuartzExecutor();
		try {
			quartzExecutor.start();
		} catch (SchedulerException e) {
			logger.warn("Error starting quartz scheduler", e);
		}
		
		watchServiceHolder = new WatchServiceHolder(this);
		if (watchServiceHolder.triggerIsAvailable()) {
			watchServiceHolder.startPolling();
		}
		
		reloadIncurredTasks();
	}
	
	@Deactivate
	private void deactivateComponent(){
		try {
			quartzExecutor.shutdown();
		} catch (SchedulerException e) {
			logger.warn("Error stopping quartz scheduler", e);
		}
		watchServiceHolder.stopPolling();
	}
	
	/**
	 * Load all task descriptors we are responsible for and start (incur) them
	 */
	private void reloadIncurredTasks(){
		IQuery<ITaskDescriptor> query = taskModelService.getQuery(ITaskDescriptor.class);
		query.and(ModelPackage.Literals.ITASK_DESCRIPTOR__ACTIVE, COMPARATOR.EQUALS, true);
		query.startGroup();
		query.and(ModelPackage.Literals.ITASK_DESCRIPTOR__RUNNER, COMPARATOR.EQUALS,
			contextService.getStationIdentifier());
		query.or(ModelPackage.Literals.ITASK_DESCRIPTOR__RUNNER, COMPARATOR.EQUALS, null);
		query.orJoinGroups();
		List<ITaskDescriptor> execute = query.execute();
		for (ITaskDescriptor iTaskDescriptor : execute) {
			try {
				logger.info("incurring task descriptor [{}] reference id [{}]",
					iTaskDescriptor.getId(), iTaskDescriptor.getReferenceId());
				incur(iTaskDescriptor);
			} catch (TaskException e) {
				logger.warn("Can not incur taskdescriptor [{}]", iTaskDescriptor.getId(), e);
			}
		}
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
		} else if (TaskTriggerType.CRON == taskDescriptor.getTriggerType()) {
			quartzExecutor.incur(this, taskDescriptor);
		} else if (TaskTriggerType.MANUAL == taskDescriptor.getTriggerType()) {
			// nothing to be done
		} else if (TaskTriggerType.OTHER_TASK == taskDescriptor.getTriggerType()) {
			// nothing to be done
		} else {
			throw new TaskException(TaskException.TRIGGER_NOT_AVAILABLE,
				"Trigger type not yet implemented [" + taskDescriptor.getTriggerType() + "]");
		}
	}
	
	/**
	 * Release responsibility for executing this task when required.
	 * 
	 * @param taskDescriptor
	 * @throws TaskException
	 */
	private void release(ITaskDescriptor taskDescriptor) throws TaskException{
		if (TaskTriggerType.FILESYSTEM_CHANGE == taskDescriptor.getTriggerType()) {
			watchServiceHolder.release(taskDescriptor);
		} else if (TaskTriggerType.CRON == taskDescriptor.getTriggerType()) {
			quartzExecutor.release(taskDescriptor);
		}
	}
	
	@Override
	public ITaskDescriptor createTaskDescriptor(IUser owner, IIdentifiedRunnable identifiedRunnable)
		throws TaskException{
		
		if (owner == null || identifiedRunnable == null) {
			throw new TaskException(TaskException.PARAMETERS_MISSING);
		}
		
		ITaskDescriptor taskDescriptor = taskModelService.create(ITaskDescriptor.class);
		taskDescriptor.setOwner(owner);
		taskDescriptor.setIdentifiedRunnableId(identifiedRunnable.getId());
		taskDescriptor.setRunner(contextService.getRootContext().getStationIdentifier());
		
		saveTaskDescriptor(taskDescriptor);
		
		return taskDescriptor;
	}
	
	public void notify(ITask runningTask){
		if (runningTask.isFinished()) {
			// TODO user notification; tasks that are triggered by this task
			runningTasks.remove(runningTask);
		}
		System.out.println("notify " + runningTask);
	}
	
	@Override
	public ITask trigger(ITaskDescriptor taskDescriptor, IProgressMonitor progressMonitor,
		TaskTriggerType triggerType, Map<String, String> runContext) throws TaskException{
		
		logger.info("[{}] triggered [{}/{}]", triggerType, taskDescriptor.getId(), taskDescriptor.getReferenceId());
		logger.info("runContext [{}]", runContext);
		
		ITask task = new Task(taskDescriptor, triggerType, progressMonitor, runContext);
		
		try {
			if (taskDescriptor.isSingleton()) {
				singletonExecutorService.execute((Runnable) task);
			} else {
				parallelExecutorService.execute((Runnable) task);
			}
			runningTasks.add(task);
		} catch (RejectedExecutionException re) {
			// TODO triggering failed, where to show?
			throw new TaskException(TaskException.EXECUTION_REJECTED, re);
		}
		
		return task;
	}
	
	@Override
	public ITask trigger(String taskDescriptorReferenceId, IProgressMonitor progressMonitor,
		TaskTriggerType triggerType, Map<String, String> runContext) throws TaskException{
		
		IQuery<ITaskDescriptor> query = taskModelService.getQuery(ITaskDescriptor.class);
		query.and(ModelPackage.Literals.ITASK_DESCRIPTOR__REFERENCE_ID, COMPARATOR.EQUALS,
			taskDescriptorReferenceId);
		Optional<ITaskDescriptor> taskDescriptor = query.executeSingleResult();
		if (taskDescriptor.isPresent()) {
			return trigger(taskDescriptor.get(), progressMonitor, triggerType, runContext);
		}
		throw new TaskException(TaskException.EXECUTION_REJECTED,
			"Could not find task descriptor reference id [" + taskDescriptorReferenceId + "]");
	}
	
	@Override
	public IIdentifiedRunnable instantiateRunnableById(String runnableId) throws TaskException{
		if (runnableId == null || runnableId.length() == 0) {
			throw new TaskException(TaskException.RWC_INVALID_ID);
		}
		
		Optional<IIdentifiedRunnable> result = runnableWithContextFactories.stream()
			.map(rwcf -> rwcf.createRunnableWithContext(runnableId)).filter(Objects::nonNull)
			.findFirst();
		if (result.isPresent()) {
			return result.get();
		}
		
		throw new TaskException(TaskException.RWC_NO_INSTANCE_FOUND,
			"Could not instantiate runnable id [" + runnableId + "]");
	}
	
	@Override
	public void saveTaskDescriptor(ITaskDescriptor taskDescriptor) throws TaskException{
		boolean save = taskModelService.save((TaskDescriptor) taskDescriptor);
		if (!save) {
			throw new TaskException(TaskException.PERSISTENCE_ERROR);
		}
	}
	
	@Override
	public void setActive(ITaskDescriptor taskDescriptor, boolean active) throws TaskException{
		
		if (active) {
			validateTaskDescriptor(taskDescriptor);
		}
		
		taskDescriptor.setActive(active);
		saveTaskDescriptor(taskDescriptor);
		
		if (active) {
			incur(taskDescriptor);
		} else {
			release(taskDescriptor);
		}
	}
	
	/**
	 * validate if the required parameters are set, else we must not allow activating it.
	 * 
	 * @param taskDescriptor
	 */
	private void validateTaskDescriptor(ITaskDescriptor taskDescriptor) throws TaskException{
		
		IIdentifiedRunnable runnable =
				instantiateRunnableById(taskDescriptor.getIdentifiedRunnableId());
		
		if(TaskTriggerType.OTHER_TASK == taskDescriptor.getTriggerType()) {
			// we will not check activation here, as the required parameters
			// will be supplied by the other task invoking us
			return;
		}
		
		Set<Entry<String, Serializable>> entrySet = runnable.getDefaultRunContext().entrySet();
		for (Entry<String, Serializable> entry : entrySet) {
			if (IIdentifiedRunnable.RunContextParameter.VALUE_MISSING_REQUIRED
				.equals(entry.getValue())) {
				Serializable value = taskDescriptor.getRunContext().get(entry.getKey());
				if (value == null || IIdentifiedRunnable.RunContextParameter.VALUE_MISSING_REQUIRED
					.equals(value)) {
					throw new TaskException(TaskException.EXECUTION_REJECTED,
						"Missing required parameter [" + entry.getKey() + "]");
				}
			}
		}
		
	}
	
	@Override
	public Map<String, String> listAvailableRunnables(){
		Map<String, String> result = new HashMap<>();
		runnableWithContextFactories.stream().forEach(c -> result.putAll(c.getProvidedRunnables()));
		return result;
	}
	
	@Override
	public Optional<ITaskDescriptor> findTaskDescriptorByIdOrReferenceId(String idOrReferenceId){
		IQuery<ITaskDescriptor> query = taskModelService.getQuery(ITaskDescriptor.class);
		query.and(ModelPackage.Literals.ITASK_DESCRIPTOR__ID, COMPARATOR.EQUALS, idOrReferenceId);
		query.or(ModelPackage.Literals.ITASK_DESCRIPTOR__REFERENCE_ID, COMPARATOR.EQUALS,
			idOrReferenceId);
		return query.executeSingleResult();
	}
	
	@Override
	public List<ITask> findExecutions(ITaskDescriptor taskDescriptor){
		IQuery<ITask> query = taskModelService.getQuery(ITask.class);
		query.and(ModelPackage.Literals.ITASK__DESCRIPTOR_ID, COMPARATOR.EQUALS,
			taskDescriptor.getId());
		query.orderBy("lastupdate", ORDER.DESC);
		return query.execute();
	}
	
}
