package ch.elexis.core.tasks.internal.model.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.internal.model.service.CoreModelServiceHolder;
import ch.elexis.core.tasks.internal.service.LogProgressMonitor;
import ch.elexis.core.tasks.internal.service.TaskServiceHolder;
import ch.elexis.core.tasks.internal.service.TaskServiceImpl;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class Task extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Task>
		implements IdentifiableWithXid, ITask, Runnable {
	
	private Logger logger;
	
	private IProgressMonitor progressMonitor;
	
	public Task(ch.elexis.core.jpa.entities.Task entity){
		super(entity);
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
			getEntity().setRunContext(new Gson().toJson(runContext));
		}
		
		logger = LoggerFactory.getLogger("Task [" + getId() + "] ("
			+ taskDescriptor.getIdentifiedRunnableId() + ") " + triggerType);
		logger.info("state = {}, origin = {}, originReferenceId = {}", getState(),
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
			return new Gson().fromJson(json, Map.class);
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
		logger.info("state = {}", getState());
		CoreModelServiceHolder.get().save(this);
	}
	
	private void setResult(Map<String, Serializable> result){
		String json = new Gson().toJson(result);
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
			return new Gson().fromJson(json, Map.class);
		}
		return new HashMap<>();
	}
	
	@Override
	public boolean isFinished(){
		return (TaskState.COMPLETED == getState() || TaskState.FAILED == getState());
	}
	
	@Override
	public void run(){
		Optional<ITaskDescriptor> originTaskDescriptor = TaskServiceHolder.get()
			.findTaskDescriptorByIdOrReferenceId(getEntity().getDescriptorId());
		if (originTaskDescriptor.isPresent()) {
			
			setState(TaskState.READY);
			String runnableWithContextId = originTaskDescriptor.get().getIdentifiedRunnableId();
			
			// TODO persist executing station
			
			Map<String, Serializable> _result = null;
			try {
				IIdentifiedRunnable runnableWithContext =
					TaskServiceHolder.get().instantiateRunnableById(runnableWithContextId);
				
				Map<String, Serializable> effectiveRunContext = new HashMap<>();
				effectiveRunContext.putAll(runnableWithContext.getDefaultRunContext());
				effectiveRunContext.putAll(originTaskDescriptor.get().getRunContext());
				effectiveRunContext.putAll(getRunContext());
				
				setState(TaskState.IN_PROGRESS);
				_result = runnableWithContext.run(effectiveRunContext, progressMonitor, logger);
				setState(TaskState.COMPLETED);
			} catch (TaskException te) {
				setState(TaskState.FAILED);
				logger.warn(te.getMessage(), te);
			}
			
			if (progressMonitor != null) {
				progressMonitor.done();
			}
			
			setResult(_result);
			
		} else {
			logger.warn("Could not resolve task descriptor [{}]", getEntity().getDescriptorId());
			setState(TaskState.FAILED);
		}
		
		TaskServiceImpl ts = (TaskServiceImpl) TaskServiceHolder.get();
		ts.notify(this);
	}
	
}
