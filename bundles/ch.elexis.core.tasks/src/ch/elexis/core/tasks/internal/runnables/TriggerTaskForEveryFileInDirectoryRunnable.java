package ch.elexis.core.tasks.internal.runnables;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.tasks.IdentifiedRunnableIdConstants;
import ch.elexis.core.tasks.internal.service.TaskServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

/**
 * Triggers a task, referenced by its task descriptor reference id, for every file found and
 * readable in the provided URL, which has to be a directory.
 */
public class TriggerTaskForEveryFileInDirectoryRunnable implements IIdentifiedRunnable {
	
	public static final String RESULT_KEY_LIST_ITASK_TASKS_TRIGGERED = "tasksTriggered";
	
	public static final String RCP_STRING_FILE_EXTENSION_FILTER = "fileExtensionFilter";
	
	private String taskDescriptorReferenceId;
	private Logger logger;
	
	private IVirtualFilesystemService virtualFilsystemService;
	
	public TriggerTaskForEveryFileInDirectoryRunnable(
		IVirtualFilesystemService virtualFilsystemService){
		this.virtualFilsystemService = virtualFilsystemService;
	}
	
	@Override
	public String getId(){
		return IdentifiedRunnableIdConstants.TRIGGER_TASK_FOR_EVERY_FILE;
	}
	
	@Override
	public String getLocalizedDescription(){
		return "Execute a task on every file found in a given directory.";
	}
	
	@Override
	public Map<String, Serializable> getDefaultRunContext(){
		Map<String, Serializable> runContext = new HashMap<>();
		runContext.put(RunContextParameter.STRING_URL, RunContextParameter.VALUE_MISSING_REQUIRED);
		runContext.put(RunContextParameter.TASK_DESCRIPTOR_REFID,
			RunContextParameter.VALUE_MISSING_REQUIRED);
		runContext.put(RCP_STRING_FILE_EXTENSION_FILTER, null);
		
		return runContext;
	}
	
	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException{
		
		this.logger = logger;
		
		String urlString = (String) runContext.get(RunContextParameter.STRING_URL);
		taskDescriptorReferenceId =
			(String) runContext.get(RunContextParameter.TASK_DESCRIPTOR_REFID);
		String fileExtensionFilter = (String) runContext.get(RCP_STRING_FILE_EXTENSION_FILTER);
		
		List<Serializable> tasksTriggered = new ArrayList<>();
		
		IVirtualFilesystemHandle of;
		IVirtualFilesystemHandle[] listHandles;
		try {
			of = virtualFilsystemService.of(urlString);
			if (StringUtils.isNotBlank(fileExtensionFilter)) {
				listHandles = of.listHandles(
					handle -> fileExtensionFilter.equalsIgnoreCase(handle.getExtension()));
			} else {
				listHandles = of.listHandles();
			}
			
		} catch (IOException e) {
			throw new TaskException(TaskException.EXECUTION_ERROR, e.getMessage());
		}
		
		for (IVirtualFilesystemHandle fileHandle : listHandles) {
			tasksTriggered.add(runTaskForFile(fileHandle.getAbsolutePath()).getId());
		}
		
		if(tasksTriggered.isEmpty()) {
			return Collections.singletonMap(ReturnParameter.MARKER_DO_NOT_PERSIST, true);
		}
		
		return Collections.singletonMap(RESULT_KEY_LIST_ITASK_TASKS_TRIGGERED,
			(Serializable) tasksTriggered);
	}
	
	private ITask runTaskForFile(String url) throws TaskException{
		logger.debug("Triggering task reference id [{}] for url [{}]", taskDescriptorReferenceId,
			url);
		try {
			ITaskService taskService = TaskServiceHolder.get();
			Map<String, String> runContext =
				Collections.singletonMap(RunContextParameter.STRING_URL, url);
			return taskService.trigger(taskDescriptorReferenceId, null, TaskTriggerType.OTHER_TASK,
				runContext);
			
		} catch (IllegalStateException e) {
			throw new TaskException(TaskException.EXECUTION_ERROR, e);
		}
	}
	
}
