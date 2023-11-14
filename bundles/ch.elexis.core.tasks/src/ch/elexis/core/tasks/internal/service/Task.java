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
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
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
import ch.elexis.core.model.tasks.IIdentifiedRunnable.RunContextParameter;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.tasks.internal.model.service.ContextServiceHolder;
import ch.elexis.core.tasks.internal.model.service.CoreModelServiceHolder;
import ch.elexis.core.tasks.internal.model.service.TaskModelAdapterFactory;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskState;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class Task extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Task>
		implements Identifiable, ITask, Runnable {

	private static final Gson GSON = new Gson();

	private Logger logger;
	private IProgressMonitor progressMonitor;

	private String taskId;
	private boolean isTriggerSync = false;

	public Task(ch.elexis.core.jpa.entities.Task entity) {
		super(entity);
	}

	/**
	 *
	 * @param taskDescriptor
	 * @param triggerType
	 * @param progressMonitor
	 * @param runContext      the context parameters to use, on effectively running
	 *                        this task, the default values will be initialized as
	 *                        follows: 1. Use
	 *                        {@link IIdentifiedRunnable#getDefaultRunContext()}, 2.
	 *                        overlay with {@link ITaskDescriptor#getRunContext()}
	 *                        3. overlay with runContext value as provided
	 */
	@SuppressWarnings("unchecked")
	public Task(ITaskDescriptor taskDescriptor, TaskTriggerType triggerType, IProgressMonitor progressMonitor,
			Map<String, String> runContext) {
		this(new ch.elexis.core.jpa.entities.Task());

		taskId = ((taskDescriptor.isSingleton()) ? "Task-S-" : "Task---") + getId();

		getEntity().setState(TaskState.DRAFT.getValue());
		getEntity().setTriggerEvent(triggerType.getValue());
		getEntity()
				.setTaskDescriptor(((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.TaskDescriptor>) taskDescriptor)
						.getEntityMarkDirty());
		if (runContext != null) {
			getEntity().setRunContext(GSON.toJson(runContext));
			isTriggerSync = Boolean.valueOf(runContext.get("isTriggerSync"));
		}
		String stationIdentifier = ContextServiceHolder.get().getRootContext().getStationIdentifier();
		getEntity().setRunner(StringUtils.abbreviate(stationIdentifier, 64));
		getEntity().setCreatedAt(System.currentTimeMillis());

		logger = LoggerFactory.getLogger("Task [" + taskDescriptor.getReferenceId() + "/" + getId() + "] ");
		logger.debug("state = {}, origin = {}, originReferenceId = {}", getState(), taskDescriptor.getId(),
				taskDescriptor.getReferenceId());

		this.progressMonitor = (progressMonitor != null) ? progressMonitor : new LogProgressMonitor(logger);

		AccessControlServiceHolder.get().doPrivileged(() -> CoreModelServiceHolder.get().save(this));
	}

	@Override
	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Serializable> getRunContext() {
		String json = getEntity().getRunContext();
		if (json != null) {
			return GSON.fromJson(json, Map.class);
		}
		return new HashMap<>();
	}

	@Override
	public TaskState getState() {
		int val = getEntity().getState();
		return TaskState.get(val);
	}

	void setState(TaskState state) {
		getEntity().setState(state.getValue());

		if (TaskState.READY == state) {
			String userId = ContextServiceHolder.get().getActiveUser().map(u -> u.getId()).orElse(null);
			String mandatorId = ContextServiceHolder.get().getActiveMandator().map(u -> u.getId()).orElse(null);
			logger.info("state = {}, activeUserId = {}, activeMandatorId = {}", getState(), userId, mandatorId);
		} else if (TaskState.FAILED == state) {
			logger.warn("state = {}, result = [{}]", getState(), getResult());
		} else if (TaskState.COMPLETED == state || TaskState.CANCELLED == state) {
			logger.info("state = {}, result = [{}]", getState(), getResult());
		} else {
			logger.debug("state = {}", getState());
		}
		CoreModelServiceHolder.get().save(this);
		TaskServiceImpl ts = (TaskServiceImpl) TaskServiceHolder.get();
		ts.notify(this);
	}

	private void setResult(Map<String, Serializable> result) {
		String json = GSON.toJson(result);
		getEntity().setResult(json);
		CoreModelServiceHolder.get().save(this);
	}

	@Override
	public ITaskDescriptor getTaskDescriptor() {
		Optional<Identifiable> adapter = TaskModelAdapterFactory.getInstance()
				.getModelAdapter(getEntity().getTaskDescriptor(), ITaskDescriptor.class, true, false);
		return (ITaskDescriptor) adapter.orElse(null);
	}

	@Override
	public TaskTriggerType getTriggerEvent() {
		int val = getEntity().getTriggerEvent();
		return TaskTriggerType.get(val);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Serializable> getResult() {
		String json = getEntity().getResult();
		if (json != null) {
			return GSON.fromJson(json, Map.class);
		}
		return new HashMap<>();
	}

	@Override
	public <T> List<T> getResultEntryAsTypedList(String key, Class<T> clazz) {
		List<?> list = (List<?>) getResult().get(key);
		if (list != null && !list.isEmpty()) {
			String json = GSON.toJson(list);
			Type type = TypeToken.getParameterized(ArrayList.class, clazz).getType();
			return GSON.fromJson(json, type);
		}
		return Collections.emptyList();
	}

	@Override
	public <T> T getResultEntryTyped(String key, Class<T> clazz) {
		String json = getEntity().getResult();
		if (json != null) {
			@SuppressWarnings("rawtypes")
			Map map = GSON.fromJson(json, Map.class);
			if (!map.isEmpty()) {
				String valueToString = GSON.toJson(map.get(key));
				return GSON.fromJson(valueToString, clazz);
			}
		}
		return null;
	}

	@Override
	public <T> T getRunContextEntryTyped(String key, Class<T> clazz) {
		Map<String, Serializable> map = getRunContext();
		String valueToString = GSON.toJson(map.get(key));
		return GSON.fromJson(valueToString, clazz);
	}

	@Override
	public void setStateCompletedManual(String remark) {
		String userId = ContextServiceHolder.get().getActiveUser().map(u -> u.getId()).orElse(null);
		String stationId = ContextServiceHolder.get().getStationIdentifier();
		String note = System.currentTimeMillis() + "#" + userId + "@" + stationId + ":" + remark;

		setState(TaskState.COMPLETED_MANUAL);
		Map<String, Serializable> result = getResult();
		result.put(TaskState.COMPLETED_MANUAL.name(), note);
		setResult(result);
	}

	@Override
	public boolean isSucceeded() {
		return TaskState.COMPLETED == getState();
	}

	@Override
	public boolean isFailed() {
		return TaskState.FAILED == getState();
	}

	@Override
	public boolean isFinished() {
		return (TaskState.COMPLETED == getState() || TaskState.COMPLETED_WARN == getState()
				|| TaskState.FAILED == getState() || TaskState.CANCELLED == getState());
	}

	private void removeTaskRecord() {
		AccessControlServiceHolder.get().doPrivileged(() -> CoreModelServiceHolder.get().remove(this));
	}

	@Override
	public void run() {

		Thread.currentThread().setName(taskId);
		ITaskDescriptor originTaskDescriptor = getTaskDescriptor();

		try {
			IUser owner;
			if (isTriggerSync) {
				owner = ContextServiceHolder.get().getActiveUser().orElse(null);
			} else {
				owner = originTaskDescriptor.getOwner();
				if (owner == null) {
					throw new TaskException(TaskException.EXECUTION_REJECTED, "No task owner defined");
				}
			}

			if (isThreadLocalContextService()) {
				ContextServiceHolder.get().setActiveUser(owner);
				IContact user_assignedContact = owner.getAssignedContact();
				if (user_assignedContact != null && user_assignedContact.isMandator()) {
					IMandator mandator = CoreModelServiceHolder.get()
							.load(user_assignedContact.getId(), IMandator.class).orElse(null);
					ContextServiceHolder.get().setActiveMandator(mandator);
				}
			}

			getEntity().setRunAt(System.currentTimeMillis());
			setState(TaskState.READY);

			String runnableWithContextId = originTaskDescriptor.getIdentifiedRunnableId();

			IIdentifiedRunnable runnableWithContext = TaskServiceHolder.get()
					.instantiateRunnableById(runnableWithContextId);

			Map<String, Serializable> effectiveRunContext = new HashMap<>();
			effectiveRunContext.putAll(runnableWithContext.getDefaultRunContext());
			effectiveRunContext.putAll(originTaskDescriptor.getRunContext());
			effectiveRunContext.putAll(getRunContext());

			assertRequiredRunContextParameters(effectiveRunContext);

			getEntity().setRunContext(GSON.toJson(effectiveRunContext));
			setState(TaskState.IN_PROGRESS);
			// TODO what if it runs forever?
			// TODO progressMonitor handling
			try {
				Map<String, Serializable> result = runnableWithContext.run(effectiveRunContext, progressMonitor,
						logger);
				if (result == null) {
					result = Collections.emptyMap();
				}

				setResult(result);
				getEntity().setFinishedAt(System.currentTimeMillis());
				TaskState exitState = (result.containsKey(ReturnParameter.MARKER_WARN)) ? TaskState.COMPLETED_WARN
						: TaskState.COMPLETED;
				setState(exitState);

				if (effectiveRunContext.containsKey(ReturnParameter.MARKER_DO_NOT_PERSIST)
						|| getResult().containsKey(ReturnParameter.MARKER_DO_NOT_PERSIST)) {
					// only if completion was successful
					removeTaskRecord();
				}
			} catch (OperationCanceledException oce) {
				setState(TaskState.CANCELLED);
				getEntity().setFinishedAt(System.currentTimeMillis());
				setResult(Collections.singletonMap(IIdentifiedRunnable.ReturnParameter.RESULT_DATA, oce.getMessage()));
			}
			progressMonitor.done();

		} catch (Exception | Error e) {
			// if we end up here, there is most certainly a bug in the IIdentifiedRunnable
			// which will have to be handled by the programmers ...
			setResult(Collections.singletonMap(IIdentifiedRunnable.ReturnParameter.FAILED_TASK_EXCEPTION_MESSAGE,
					e.getMessage()));
			Throwable throwable = (e.getCause() != null) ? e.getCause() : e;
			logger.warn(e.getMessage(), throwable);
			getEntity().setFinishedAt(System.currentTimeMillis());
			setState(TaskState.FAILED);
		} finally {
			if (isThreadLocalContextService()) {
				ContextServiceHolder.get().setActiveUser(null);
				ContextServiceHolder.get().setActiveMandator(null);
			}
		}
	}

	/**
	 * assert that all required run context parameters are set
	 *
	 * @param effectiveRunContext
	 * @throws TaskException
	 */
	private void assertRequiredRunContextParameters(Map<String, Serializable> effectiveRunContext)
			throws TaskException {
		// TODO validate all required parameters are set, validate url ?
		boolean hasMissingRequiredRunContextParameter = effectiveRunContext.values()
				.contains(RunContextParameter.VALUE_MISSING_REQUIRED);
		if (hasMissingRequiredRunContextParameter) {
			List<String> missingValues = effectiveRunContext.keySet().stream()
					.filter(key -> effectiveRunContext.get(key).equals(RunContextParameter.VALUE_MISSING_REQUIRED))
					.collect(Collectors.toList());
			throw new TaskException(TaskException.EXECUTION_REJECTED,
					"Missing required run-context-parameter(s): " + missingValues);
		}
	}

	private boolean isThreadLocalContextService() {
		return !ContextServiceHolder.get().getClass().getName().startsWith("ch.elexis.core.ui.services");
	}

	@Override
	public boolean addXid(String domain, String id, boolean updateIfExists) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IXid getXid(String domain) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getLabel() {
		return "Task [" + getId() + "/" + getTaskDescriptor().getReferenceId() + "] <= (" + getTriggerEvent() + "): "
				+ getState();
	}

	@Override
	public String getRunner() {
		return getEntity().getRunner();
	}

	@Override
	public LocalDateTime getCreatedAt() {
		return getEntity().getCreatedAtLocalDateTime();
	}

	@Override
	public LocalDateTime getRunAt() {
		return getEntity().getRunAtLocalDateTime();
	}

	@Override
	public LocalDateTime getFinishedAt() {
		return getEntity().getFinishedAtLocalDateTime();
	}

	@Override
	public boolean isSystem() {
		return getEntity().isSystem();
	}

	@Override
	public void setSystem(boolean value) {
		getEntity().setSystem(value);
	}
}
