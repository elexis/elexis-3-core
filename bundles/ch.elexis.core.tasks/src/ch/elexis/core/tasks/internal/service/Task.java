package ch.elexis.core.tasks.internal.service;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.ReturnParameter;
import ch.elexis.core.tasks.internal.model.service.ContextServiceHolder;
import ch.elexis.core.tasks.internal.model.service.CoreModelServiceHolder;
import ch.elexis.core.tasks.internal.model.service.TaskModelAdapterFactory;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class Task extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Task>
		implements Identifiable, ITask, Runnable {
	
	private Logger logger;
	
	private IProgressMonitor progressMonitor;
	
	private final Gson gson;
	private String taskId;
	
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
	@SuppressWarnings("unchecked")
	public Task(ITaskDescriptor taskDescriptor, TaskTriggerType triggerType,
		IProgressMonitor progressMonitor, Map<String, String> runContext){
		this(new ch.elexis.core.jpa.entities.Task());
		
		taskId = ((taskDescriptor.isSingleton()) ? "Task-S-" : "Task-") + getId();
		
		getEntity().setState(TaskState.DRAFT.getValue());
		getEntity().setTriggerEvent(triggerType.getValue());
		getEntity().setTaskDescriptor(
			((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.TaskDescriptor>) taskDescriptor)
				.getEntityMarkDirty());
		if (runContext != null) {
			getEntity().setRunContext(gson.toJson(runContext));
		}
		String stationIdentifier =
			ContextServiceHolder.get().getRootContext().getStationIdentifier();
		getEntity().setRunner(StringUtils.abbreviate(stationIdentifier, 64));
		getEntity().setCreatedAt(LocalDateTime.now());
		
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
	public ITaskDescriptor getTaskDescriptor(){
		Optional<Identifiable> adapter = TaskModelAdapterFactory.getInstance()
			.getModelAdapter(getEntity().getTaskDescriptor(), ITaskDescriptor.class, true, false);
		return (ITaskDescriptor) adapter.orElse(null);
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
	public <T> List<T> getResultEntryAsTypedList(String key, Class<T> clazz){
		List<?> list = (List<?>) getResult().get(key);
		if (list != null && !list.isEmpty()) {
			String json = gson.toJson(list);
			Type type = TypeToken.getParameterized(ArrayList.class, clazz).getType();
			return gson.fromJson(json, type);
		}
		return Collections.emptyList();
	}
	
	@Override
	public boolean isFinished(){
		return (TaskState.COMPLETED == getState() || TaskState.COMPLETED_WARN == getState()
			|| TaskState.FAILED == getState());
	}
	
	private void removeTaskRecord(){
		logger.debug("removing record");
		CoreModelServiceHolder.get().remove(this);
	}
	
	@Override
	public void run(){
		
		Thread.currentThread().setName(taskId);
		
		getEntity().setRunAt(LocalDateTime.now());
		setState(TaskState.READY);
		
		ITaskDescriptor originTaskDescriptor = getTaskDescriptor();
		String runnableWithContextId = originTaskDescriptor.getIdentifiedRunnableId();
				
		try {
			IIdentifiedRunnable runnableWithContext =
				TaskServiceHolder.get().instantiateRunnableById(runnableWithContextId);
			
			Map<String, Serializable> effectiveRunContext = new HashMap<>();
			effectiveRunContext.putAll(runnableWithContext.getDefaultRunContext());
			effectiveRunContext.putAll(originTaskDescriptor.getRunContext());
			effectiveRunContext.putAll(getRunContext());
			
			getEntity().setRunContext(gson.toJson(effectiveRunContext));
			// TODO validate all required parameters are set, validate url
			
			setState(TaskState.IN_PROGRESS);
			long beginTimeMillis = System.currentTimeMillis();
			// TODO what if it runs forever?
			Map<String, Serializable> result =
				runnableWithContext.run(effectiveRunContext, progressMonitor, logger);
			long endTimeMillis = System.currentTimeMillis();
			if (result == null || !result.containsKey("runnableExecDuration")) {
				// returned map may be unmodifiable
				result = new HashMap<>(result);
				result.put("runnableExecDuration", Long.toString(endTimeMillis - beginTimeMillis));
			}
			
			setResult(result);
			TaskState exitState =
				(result.containsKey(ReturnParameter.MARKER_WARN)) ? TaskState.COMPLETED_WARN
						: TaskState.COMPLETED;
			setState(exitState);
			
			if (effectiveRunContext.containsKey(ReturnParameter.MARKER_DO_NOT_PERSIST)
				|| getResult().containsKey(ReturnParameter.MARKER_DO_NOT_PERSIST)) {
				// only if completion was successful
				removeTaskRecord();
			}
			
		} catch (Exception e) {
			setResult(Collections.singletonMap(
				IIdentifiedRunnable.ReturnParameter.FAILED_TASK_EXCEPTION_MESSAGE, e.getMessage()));
			logger.warn(e.getMessage(), e);
			setState(TaskState.FAILED);
		}
		
		if (progressMonitor != null) {
			progressMonitor.done();
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
	
	@Override
	public LocalDateTime getCreatedAt(){
		return getEntity().getCreatedAt();
	}
	
	@Override
	public LocalDateTime getRunAt(){
		return getEntity().getRunAt();
	}
	
}
