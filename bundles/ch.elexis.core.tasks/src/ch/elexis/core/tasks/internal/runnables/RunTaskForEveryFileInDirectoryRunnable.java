package ch.elexis.core.tasks.internal.runnables;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
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
import ch.elexis.core.tasks.IdentifiedRunnableIdConstants;
import ch.elexis.core.tasks.internal.service.TaskServiceHolder;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;
import jcifs.CloseableIterator;
import jcifs.SmbResource;
import jcifs.smb.SmbFile;

/**
 * Triggers a task, referenced by its task descriptor reference id, for every file found and
 * readable in the provided URL, which has to be a directory.
 */
public class RunTaskForEveryFileInDirectoryRunnable implements IIdentifiedRunnable {
	
	public static final String RESULT_KEY_LIST_ITASK_TASKS_TRIGGERED = "tasksTriggered";
	
	private String taskDescriptorReferenceId;
	private Logger logger;
	
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
		return runContext;
	}
	
	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException{
		
		this.logger = logger;
		
		String urlString = (String) runContext.get(RunContextParameter.STRING_URL);
		taskDescriptorReferenceId =
			(String) runContext.get(RunContextParameter.TASK_DESCRIPTOR_REFID);
		
		boolean startsWithSmb = StringUtils.startsWith(urlString, "smb");
		boolean startsWithFile = StringUtils.startsWith(urlString, "file");
		
		List<ITask> tasksTriggered = new ArrayList<>();
		
		URL url = isValidURL(urlString);
		if (url != null) {
			if (startsWithSmb) {
				tryHandleSambaShare(url, tasksTriggered);
			} else if (startsWithFile) {
				File file;
				try {
					file = new File(url.toURI());
				} catch (URISyntaxException ex) {
					file = new File(url.getPath());
				}
				tryHandleFileShare(file, tasksTriggered);
			}
			throw TaskException
				.EXECUTION_ERROR("Could not handle URL protocol [" + url.getProtocol() + "]");
		}
		
		// lets try if its a file share
		tryHandleFileShare(new File(urlString), tasksTriggered);
		
		return Collections.singletonMap(RESULT_KEY_LIST_ITASK_TASKS_TRIGGERED,
			(Serializable) tasksTriggered);
	}
	
	private void tryHandleFileShare(File file, List<ITask> tasksTriggered) throws TaskException{
		if (file.isDirectory()) {
			File[] listFiles = file.listFiles();
			for (int i = 0; i < listFiles.length; i++) {
				File child = listFiles[i];
				if (child.isFile() && child.canRead()) {
					tasksTriggered.add(runTaskForFile(child.getAbsolutePath()));
				}
			}
		}
		throw TaskException.EXECUTION_ERROR("Not a directory [" + file.getAbsolutePath() + "]");
	}
	
	private void tryHandleSambaShare(URL url, List<ITask> tasksTriggered) throws TaskException{
		try (SmbFile smbFile = (SmbFile) url.openConnection()) {
			if (smbFile.isDirectory()) {
				CloseableIterator<SmbResource> children = smbFile.children();
				while (children.hasNext()) {
					SmbResource smbResource = children.next();
					if (smbResource.isFile() && smbResource.canRead()) {
						tasksTriggered.add(runTaskForFile(smbResource.getName()));
					}
				}
			} else {
				throw TaskException.EXECUTION_ERROR("Not a directory [" + url.toString() + "]");
			}
		} catch (IOException ex) {
			throw TaskException.EXECUTION_ERROR("Samba share access error [" + url.toString() + "]",
				ex);
		}
	}
	
	private ITask runTaskForFile(String url) throws TaskException{
		logger.debug("Triggering task reference id [{}] for url [{}]", taskDescriptorReferenceId,
			url);
		try {
			ITaskService taskService = TaskServiceHolder.get();
			Map<String, String> runContext =
				Collections.singletonMap(RunContextParameter.STRING_URL, url);
			return taskService.trigger(url, null, TaskTriggerType.OTHER_TASK, runContext);
			
		} catch (IllegalStateException e) {
			throw new TaskException(TaskException.EXECUTION_ERROR, e);
		}
	}
	
	private URL isValidURL(String urlString){
		try {
			URL url = new URL(urlString);
			url.toURI();
			return url;
		} catch (Exception exception) {
			return null;
		}
	}
	
}
