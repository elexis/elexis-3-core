package ch.elexis.core.tasks.internal.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class Task implements ITask, Runnable {
	
	private Logger logger;
	
	private String id;
	private TaskState state;
	private ITaskDescriptor origin;
	private TaskTriggerType triggerType;
	private Map<String, Serializable> result;
	private TaskServiceImpl taskService;
	private IProgressMonitor progressMonitor;
	private Map<String, Serializable> runContext;
	
	public Task(TaskServiceImpl taskService, ITaskDescriptor origin, TaskTriggerType triggerType,
		IProgressMonitor progressMonitor){
		this.id = UUID.randomUUID().toString();
		this.state = TaskState.DRAFT;
		this.taskService = taskService;
		this.origin = origin;
		this.triggerType = triggerType;
		this.runContext = origin.getRunContext();
		
		logger = LoggerFactory.getLogger(
			"Task [" + id + "] (" + origin.getIdentifiedRunnableId() + ") " + triggerType);
		logger.info("state = {}, origin = {}, originReferenceId = {}", getState(), origin.getId(),
			origin.getReferenceId());
		if (logger.isDebugEnabled()) {
			logger.debug("runContext = {}", origin.getRunContext());
		}
		
		this.progressMonitor =
			(progressMonitor != null) ? progressMonitor : new LogProgressMonitor(logger);
	}
	
	@Override
	public IProgressMonitor getProgressMonitor(){
		return progressMonitor;
	}
	
	@Override
	public Map<String, Serializable> getRunContext(){
		return runContext;
	}
	
	@Override
	public String getId(){
		return id;
	}
	
	@Override
	public TaskState getState(){
		return state;
	}
	
	private void setState(TaskState state){
		this.state = state;
		logger.info("state = {}", getState());
	}
	
	@Override
	public ITaskDescriptor getDescriptor(){
		return origin;
	}
	
	@Override
	public TaskTriggerType getTriggerEvent(){
		return triggerType;
	}
	
	@Override
	public Map<String, ?> getResult(){
		return (result != null) ? result : Collections.emptyMap();
	}
	
	@Override
	public boolean isFinished(){
		return (TaskState.COMPLETED == state || TaskState.FAILED == state);
	}
	
	@Override
	public void run(){
		setState(TaskState.READY);
		
		String runnableWithContextId = origin.getIdentifiedRunnableId();
		
		try {
			IIdentifiedRunnable runnableWithContext =
				taskService.instantiateRunnableById(runnableWithContextId);
			setState(TaskState.IN_PROGRESS);
			result = runnableWithContext.run(runContext, progressMonitor, logger);
			setState(TaskState.COMPLETED);
		} catch (TaskException te) {
			setState(TaskState.FAILED);
			logger.warn(te.getMessage(), te);
		}
		
		if (progressMonitor != null) {
			progressMonitor.done();
		}
		
		taskService.notify(this);
	}
	
}
