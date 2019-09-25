package ch.elexis.core.tasks.internal.service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

import org.apache.commons.lang3.StringUtils;
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

import ch.elexis.core.model.IMessage;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.message.MessageCode;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.ReturnParameter;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IMessageService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.tasks.internal.service.fs.WatchServiceHolder;
import ch.elexis.core.tasks.internal.service.quartz.QuartzExecutor;
import ch.elexis.core.tasks.internal.service.sysevents.SysEventWatcher;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.ModelPackage;
import ch.elexis.core.tasks.model.OwnerTaskNotification;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;

@Component(immediate = true)
public class TaskServiceImpl implements ITaskService {
	
	private Logger logger;
	
	private IModelService taskModelService;
	
	private ExecutorService parallelExecutorService;
	private ExecutorService singletonExecutorService;
	private QuartzExecutor quartzExecutor;
	private WatchServiceHolder watchServiceHolder;
	private SysEventWatcher sysEventWatcher;
	
	private List<ITask> triggeredTasks;
	
	//TODO OtherTaskService -> this
	
	//private List<ITaskDescriptor> incurredTasks;
	
	@Reference
	private IContextService contextService;
	
	@Reference
	private IMessageService messageService;
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.tasks.model)")
	private void setModelService(IModelService modelService){
		taskModelService = modelService;
	}
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, bind = "bindRunnableWithContextFactory", unbind = "unbindRunnableWithContextFactory")
	private List<IIdentifiedRunnableFactory> runnableWithContextFactories;
	
	/**
	 * do not execute these instances, they are used for documentation listing only
	 */
	private List<IIdentifiedRunnable> identifiedRunnables;
	
	private Map<String, IIdentifiedRunnableFactory> runnableIdToFactoryMap;
	
	protected void bindRunnableWithContextFactory(
		IIdentifiedRunnableFactory runnableWithContextFactory){
		if (runnableWithContextFactories == null) {
			runnableWithContextFactories = new ArrayList<>();
		}
		runnableWithContextFactories.add(runnableWithContextFactory);
		
		if (identifiedRunnables == null) {
			identifiedRunnables = new ArrayList<>();
		}
		if (runnableIdToFactoryMap == null) {
			runnableIdToFactoryMap = new HashMap<>();
		}
		List<IIdentifiedRunnable> providedRunnables =
			runnableWithContextFactory.getProvidedRunnables();
		for (IIdentifiedRunnable iIdentifiedRunnable : providedRunnables) {
			runnableIdToFactoryMap.put(iIdentifiedRunnable.getId(), runnableWithContextFactory);
			identifiedRunnables.add(iIdentifiedRunnable);
		}
	}
	
	protected void unbindRunnableWithContextFactory(
		IIdentifiedRunnableFactory runnableWithContextFactory){
		runnableWithContextFactories.remove(runnableWithContextFactory);
		List<IIdentifiedRunnable> providedRunnables =
			runnableWithContextFactory.getProvidedRunnables();
		for (IIdentifiedRunnable iIdentifiedRunnable : providedRunnables) {
			runnableIdToFactoryMap.remove(iIdentifiedRunnable.getId());
			identifiedRunnables.remove(iIdentifiedRunnable);
		}
	}
	
	@Activate
	private void activateComponent(){
		logger = LoggerFactory.getLogger(getClass());
		logger.debug("Activating");
		
		triggeredTasks = Collections.synchronizedList(new ArrayList<>());
		parallelExecutorService = Executors.newCachedThreadPool();
		singletonExecutorService = Executors.newSingleThreadExecutor();
		
		quartzExecutor = new QuartzExecutor();
		try {
			quartzExecutor.start();
		} catch (SchedulerException e) {
			logger.warn("Error starting quartz scheduler", e);
		}
		
		sysEventWatcher = new SysEventWatcher();
		
		watchServiceHolder = new WatchServiceHolder(this);
		if (watchServiceHolder.triggerIsAvailable()) {
			watchServiceHolder.startPolling();
		}
		
		reloadIncurredTasks();
	}
	
	@Deactivate
	private void deactivateComponent(){
		// TODO what about the running tasks in separate threads?
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
		query.andJoinGroups();
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
		} else if (TaskTriggerType.SYSTEM_EVENT == taskDescriptor.getTriggerType()) {
			sysEventWatcher.incur(taskDescriptor);
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
		} else if (TaskTriggerType.SYSTEM_EVENT == taskDescriptor.getTriggerType()) {
			sysEventWatcher.release(taskDescriptor);
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
		String stationIdentifier = contextService.getRootContext().getStationIdentifier();
		taskDescriptor.setRunner(StringUtils.abbreviate(stationIdentifier, 64));
		
		saveTaskDescriptor(taskDescriptor);
		
		return taskDescriptor;
	}
	
	@Override
	public boolean removeTaskDescriptor(ITaskDescriptor taskDescriptor) throws TaskException{
		
		if (taskDescriptor == null) {
			throw new TaskException(TaskException.PARAMETERS_MISSING);
		}
		
		setActive(taskDescriptor, false);
		return taskModelService.remove(taskDescriptor);
	}
	
	void notify(ITask task){
		if (task.isFinished()) {
			// TODO tasks that are triggered by this task
			
			triggeredTasks.remove(task);
			
			ITaskDescriptor taskDescriptor =
				findTaskDescriptorByIdOrReferenceId(task.getDescriptorId()).orElse(null);
			OwnerTaskNotification ownerNotification = taskDescriptor.getOwnerNotification();
			
			TaskState state = task.getState();
			if (OwnerTaskNotification.WHEN_FINISHED == ownerNotification
				|| (OwnerTaskNotification.WHEN_FINISHED_FAILED == ownerNotification
					&& TaskState.FAILED == state)) {
				sendMessageToOwner(task, taskDescriptor.getOwner(), state);
			}
			
		}
		
		logger.debug("notify {}", task);
	}
	
	private void sendMessageToOwner(ITask task, IUser owner, TaskState state){
		IMessage message = messageService
			.prepare(contextService.getRootContext().getStationIdentifier(), owner.getId());
		message.addMessageCode(MessageCode.Key.SenderSubId, "tasks.taskservice");
		message.setSenderAcceptsAnswer(false);
		
		String resultText;
		if (TaskState.FAILED == state) {
			resultText =
				(String) task.getResult().get(ReturnParameter.FAILED_TASK_EXCEPTION_MESSAGE);
			message.addMessageCode(MessageCode.Key.Severity, MessageCode.Value.Severity_WARN);
		} else {
			resultText = (String) task.getResult().get(ReturnParameter.RESULT_DATA);
			message.addMessageCode(MessageCode.Key.Severity, MessageCode.Value.Severity_INFO);
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(task.getLabel());
		if (StringUtils.isNotBlank(resultText)) {
			sb.append("\n" + resultText);
		}
		message.setMessageText(sb.toString());
		
		messageService.send(message);
	}
	
	@Override
	public ITask trigger(ITaskDescriptor taskDescriptor, IProgressMonitor progressMonitor,
		TaskTriggerType triggerType, Map<String, String> runContext) throws TaskException{
		
		logger.info("[{}] trigger taskDesc [{}/{}] runContext [{}]", triggerType,
			taskDescriptor.getId(), taskDescriptor.getReferenceId(), runContext);
		
		ITask task = new Task(taskDescriptor, triggerType, progressMonitor, runContext);
		
		try {
			if (taskDescriptor.isSingleton()) {
				// TODO per runnable singletonExecutorService
				// no need to share one singleton executor for all runnables
				singletonExecutorService.execute((Runnable) task);
			} else {
				parallelExecutorService.execute((Runnable) task);
			}
			triggeredTasks.add(task);
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
		
		IIdentifiedRunnableFactory iIdentifiedRunnableFactory =
			runnableIdToFactoryMap.get(runnableId);
		if (iIdentifiedRunnableFactory != null) {
			List<IIdentifiedRunnable> providedRunnables =
				iIdentifiedRunnableFactory.getProvidedRunnables();
			for (IIdentifiedRunnable iIdentifiedRunnable : providedRunnables) {
				if (runnableId.equalsIgnoreCase(iIdentifiedRunnable.getId())) {
					return iIdentifiedRunnable;
				}
			}
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
		
		if (taskDescriptor.isActive() == active) {
			return;
		}
		
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
		
		if (TaskTriggerType.OTHER_TASK == taskDescriptor.getTriggerType()) {
			// we will not check activation here, as the required parameters
			// will be supplied by the other task invoking us (we don't know about the
			// supplied parameters)
			return;
		}
		
		if (TaskTriggerType.SYSTEM_EVENT == taskDescriptor.getTriggerType()) {
			// we will not check activation here, no formal required parameters
			// system event will only pass what's available
			return;
		}
		
		Set<Entry<String, Serializable>> entrySet = runnable.getDefaultRunContext().entrySet();
		for (Entry<String, Serializable> entry : entrySet) {
			if (IIdentifiedRunnable.RunContextParameter.VALUE_MISSING_REQUIRED
				.equals(entry.getValue())) {
				Serializable value = taskDescriptor.getRunContext().get(entry.getKey());
				if (value == null || IIdentifiedRunnable.RunContextParameter.VALUE_MISSING_REQUIRED
					.equals(value)) {
					throw new TaskException(TaskException.PARAMETERS_MISSING,
						"Missing required parameter [" + entry.getKey() + "]");
				}
			}
		}
		
	}
	
	@Override
	public List<IIdentifiedRunnable> getIdentifiedRunnables(){
		return identifiedRunnables;
	}
	
	@Override
	public Optional<ITaskDescriptor> findTaskDescriptorByIdOrReferenceId(String idOrReferenceId){
		IQuery<ITaskDescriptor> query =
			taskModelService.getQuery(ITaskDescriptor.class, true, false);
		query.and(ModelPackage.Literals.ITASK_DESCRIPTOR__ID, COMPARATOR.EQUALS, idOrReferenceId);
		query.or(ModelPackage.Literals.ITASK_DESCRIPTOR__REFERENCE_ID, COMPARATOR.EQUALS,
			idOrReferenceId);
		return query.executeSingleResult();
	}
	
	@Override
	public Optional<ITask> findLatestExecution(ITaskDescriptor taskDescriptor){
		IQuery<ITask> query = taskModelService.getQuery(ITask.class);
		query.and(ModelPackage.Literals.ITASK__DESCRIPTOR_ID, COMPARATOR.EQUALS,
			taskDescriptor.getId());
		query.orderBy("lastupdate", ORDER.DESC);
		query.limit(1);
		List<ITask> result = query.execute();
		return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
	}
}
