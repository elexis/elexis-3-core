package ch.elexis.core.tasks.internal.service;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.ReturnParameter;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.internal.model.service.ContextServiceHolder;
import ch.elexis.core.tasks.internal.model.service.CoreModelServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class Task extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Task>
		implements Identifiable, ITask, Runnable {
	
	private Logger logger;
	
	private IProgressMonitor progressMonitor;
	
	private final Gson gson;
	
	public Task(ch.elexis.core.jpa.entities.Task entity){
		super(entity);
		gson = new Gson();
	}
	
	/**
	 * 
	 * @param taskDescriptor
	 * @param triggerType
	 * @param progressMonitor
	 * @param runContext
	 *            the context parameters to use, on effectively running this task, the default
	 *            values will be initialized as follows: 1. Use
	 *            {@link IIdentifiedRunnable#getDefaultRunContext()}, 2. overlay with
	 *            {@link ITaskDescriptor#getRunContext()} 3. overlay with runContext value as
	 *            provided
	 */
	public Task(ITaskDescriptor taskDescriptor, TaskTriggerType triggerType,
		IProgressMonitor progressMonitor, Map<String, String> runContext){
		this(new ch.elexis.core.jpa.entities.Task());
		
		getEntity().setState(TaskState.DRAFT.getValue());
		getEntity().setTriggerEvent(triggerType.getValue());
		getEntity().setDescriptorId(taskDescriptor.getId());
		if (runContext != null) {
			getEntity().setRunContext(gson.toJson(runContext));
		}
		String stationIdentifier =
			ContextServiceHolder.get().getRootContext().getStationIdentifier();
		getEntity().setRunner(StringUtils.abbreviate(stationIdentifier, 64));
		
		
		logger = LoggerFactory.getLogger(
			"Task [" + getId() + "] (" + taskDescriptor.getIdentifiedRunnableId() + ") ");
		logger.debug("state = {}, origin = {}, originReferenceId = {}", getState(),
			taskDescriptor.getId(), taskDescriptor.getReferenceId());
		
		this.progressMonitor =
			(progressMonitor != null) ? progressMonitor : new LogProgressMonitor(logger);
		
		CoreModelServiceHolder.get().save(this);
	}
	
	@Override
	public IProgressMonitor getProgressMonitor(){
		return progressMonitor;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Serializable> getRunContext(){
		String json = getEntity().getRunContext();
		if (json != null) {
			return gson.fromJson(json, Map.class);
		}
		return new HashMap<>();
	}
	
	@Override
	public TaskState getState(){
		int val = getEntity().getState();
		return TaskState.get(val);
	}
	
	private void setState(TaskState state){
		getEntity().setState(state.getValue());
		if (TaskState.FAILED == state) {
			logger.warn("state = {}", getState());
		} else if (TaskState.COMPLETED == state) {
			logger.info("state = {} result = [{}]", getState(), getResult());
		} else {
			logger.debug("state = {}", getState());
		}
		CoreModelServiceHolder.get().save(this);
		TaskServiceImpl ts = (TaskServiceImpl) TaskServiceHolder.get();
		ts.notify(this);
	}
	
	private void setResult(Map<String, Serializable> result){
		String json = gson.toJson(result);
		getEntity().setResult(json);
		CoreModelServiceHolder.get().save(this);
	}
	
	@Override
	public String getDescriptorId(){
		return getEntity().getDescriptorId();
	}
	
	@Override
	public TaskTriggerType getTriggerEvent(){
		int val = getEntity().getTriggerEvent();
		return TaskTriggerType.get(val);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, ?> getResult(){
		String json = getEntity().getResult();
		if (json != null) {
			return gson.fromJson(json, Map.class);
		}
		return new HashMap<>();
	}
	
	@Override
	public boolean isFinished(){
		return (TaskState.COMPLETED == getState() || TaskState.FAILED == getState());
	}
	
	private void removeTaskRecord(){
		logger.debug("removing record");
		CoreModelServiceHolder.get().remove(this);
	}
	
	@Override
	public void run(){
		Optional<ITaskDescriptor> originTaskDescriptor = TaskServiceHolder.get()
			.findTaskDescriptorByIdOrReferenceId(getEntity().getDescriptorId());
		if (originTaskDescriptor.isPresent()) {
			
			setState(TaskState.READY);
			String runnableWithContextId = originTaskDescriptor.get().getIdentifiedRunnableId();
			
			// TODO persist executing station
			
			try {
				IIdentifiedRunnable runnableWithContext =
					TaskServiceHolder.get().instantiateRunnableById(runnableWithContextId);
				
				Map<String, Serializable> effectiveRunContext = new HashMap<>();
				effectiveRunContext.putAll(runnableWithContext.getDefaultRunContext());
				effectiveRunContext.putAll(originTaskDescriptor.get().getRunContext());
				effectiveRunContext.putAll(getRunContext());
				
				getEntity().setRunContext(gson.toJson(effectiveRunContext));
				// TODO validate all required parameters are set, validate url
				
				setState(TaskState.IN_PROGRESS);
				setResult(runnableWithContext.run(effectiveRunContext, progressMonitor, logger));
				setState(TaskState.COMPLETED);
				
				if (effectiveRunContext.containsKey(ReturnParameter.MARKER_DO_NOT_PERSIST)
					|| getResult().containsKey(ReturnParameter.MARKER_DO_NOT_PERSIST)) {
					// only if completion was successful
					removeTaskRecord();
				}
				
			} catch (TaskException te) {
				setResult(Collections.singletonMap(
					IIdentifiedRunnable.ReturnParameter.FAILED_TASK_EXCEPTION_MESSAGE,
					te.getMessage()));
				logger.warn(te.getMessage(), te);
				setState(TaskState.FAILED);
			}
			
			if (progressMonitor != null) {
				progressMonitor.done();
			}
			
		} else {
			logger.warn("Could not resolve task descriptor [{}]", getEntity().getDescriptorId());
			setState(TaskState.FAILED);
		}
		
	}
	
	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IXid getXid(String domain){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getLabel(){
		return "Task [" + getId() + "] (triggered by " + getTriggerEvent() + "): " + getState();
	}
	
}
