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
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.ReturnParameter;
import ch.elexis.core.model.tasks.TaskException;
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
		
		taskId = ((taskDescriptor.isSingleton()) ? "Task-S-" : "Task---") + getId();
		
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
		getEntity().setCreatedAt(System.currentTimeMillis());
		
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
		
		if (TaskState.READY == state) {
			String userId =
				ContextServiceHolder.get().getActiveUser().map(u -> u.getId()).orElse(null);
			String mandatorId =
				ContextServiceHolder.get().getActiveMandator().map(u -> u.getId()).orElse(null);
			logger.info("state = {}, activeUserId = {}, activeMandatorId = {}", getState(), userId,
				mandatorId);
		} else if (TaskState.FAILED == state) {
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
	public <T> T getResultEntryTyped(String key, Class<T> clazz){
		String json = getEntity().getResult();
		if (json != null) {
			JSONObject map = new JSONObject(json);
			String valueToString = JSONObject.valueToString(map.get(key));
			return gson.fromJson(valueToString, clazz);
		}
		return null;
	}
	
	@Override
	public boolean isSucceeded(){
		return (TaskState.COMPLETED == getState() || TaskState.COMPLETED_WARN == getState());
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
		ITaskDescriptor originTaskDescriptor = getTaskDescriptor();
		
		try {
			IUser owner = originTaskDescriptor.getOwner();
			if (owner == null) {
				throw new TaskException(TaskException.EXECUTION_REJECTED, "No task owner defined");
			}
			
			ContextServiceHolder.get().setActiveUser(owner);
			IContact user_assignedContact = owner.getAssignedContact();
			if (user_assignedContact != null && user_assignedContact.isMandator()) {
				IMandator mandator = CoreModelServiceHolder.get()
					.load(user_assignedContact.getId(), IMandator.class).orElse(null);
				ContextServiceHolder.get().setActiveMandator(mandator);
			}
			
			getEntity().setRunAt(System.currentTimeMillis());
			setState(TaskState.READY);
			
			String runnableWithContextId = originTaskDescriptor.getIdentifiedRunnableId();
			
			IIdentifiedRunnable runnableWithContext =
				TaskServiceHolder.get().instantiateRunnableById(runnableWithContextId);
			
			Map<String, Serializable> effectiveRunContext = new HashMap<>();
			effectiveRunContext.putAll(runnableWithContext.getDefaultRunContext());
			effectiveRunContext.putAll(originTaskDescriptor.getRunContext());
			effectiveRunContext.putAll(getRunContext());
			
			getEntity().setRunContext(gson.toJson(effectiveRunContext));
			// TODO validate all required parameters are set, validate url
			setState(TaskState.IN_PROGRESS);
			// TODO what if it runs forever?
			Map<String, Serializable> result =
				runnableWithContext.run(effectiveRunContext, progressMonitor, logger);
			if (result == null) {
				result = Collections.emptyMap();
			}
			
			setResult(result);
			TaskState exitState =
				(result.containsKey(ReturnParameter.MARKER_WARN)) ? TaskState.COMPLETED_WARN
						: TaskState.COMPLETED;
			getEntity().setFinishedAt(System.currentTimeMillis());
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
			getEntity().setFinishedAt(System.currentTimeMillis());
			setState(TaskState.FAILED);
		} finally {
			ContextServiceHolder.get().setActiveUser(null);
			ContextServiceHolder.get().setActiveMandator(null);
			
			if (progressMonitor != null) {
				progressMonitor.done();
			}
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
	public String getRunner(){
		return getEntity().getRunner();
	}
	
	@Override
	public LocalDateTime getCreatedAt(){
		return getEntity().getCreatedAtLocalDateTime();
	}
	
	@Override
	public LocalDateTime getRunAt(){
		return getEntity().getRunAtLocalDateTime();
	}
	
	@Override
	public LocalDateTime getFinishedAt(){
		return getEntity().getFinishedAtLocalDateTime();
	}
}
