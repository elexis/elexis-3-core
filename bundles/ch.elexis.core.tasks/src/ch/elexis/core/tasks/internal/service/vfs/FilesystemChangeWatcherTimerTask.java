package ch.elexis.core.tasks.internal.service.vfs;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.tasks.IIdentifiedRunnable.RunContextParameter;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class FilesystemChangeWatcherTimerTask extends TimerTask {

	private final ITaskService taskService;
	private final IVirtualFilesystemService virtualFileSystemService;

	private final Logger logger;
	private final Map<String, String[]> pollMap;

	FilesystemChangeWatcherTimerTask(ITaskService taskService, IVirtualFilesystemService virtualFileSystemService) {
		this.taskService = taskService;
		this.virtualFileSystemService = virtualFileSystemService;
		this.logger = LoggerFactory.getLogger(getClass());
		this.pollMap = Collections.synchronizedMap(new HashMap<String, String[]>());
	}

	void incur(String taskDescriptorId, String url, String fileExtensionFilter) {
		pollMap.put(taskDescriptorId, new String[] { url, fileExtensionFilter });
	}

	void release(String taskDescriptorId) {
		pollMap.remove(taskDescriptorId);
	}

	@Override
	public void run() {
		Set<Entry<String, String[]>> pollEntrySet = pollMap.entrySet();
		for (Iterator<Entry<String, String[]>> iterator = pollEntrySet.iterator(); iterator.hasNext();) {
			Entry<String, String[]> entry = iterator.next();

			String taskDescriptorId = entry.getKey();
			String urlString = entry.getValue()[0];
			String fileExtensionFilter = entry.getValue()[1];

			logger.debug("[{}] poll run for url [{}] fileExtensionFilter [{}]", taskDescriptorId, urlString,
					fileExtensionFilter);

			IVirtualFilesystemHandle of;
			IVirtualFilesystemHandle[] listHandles = new IVirtualFilesystemHandle[] {};
			try {
				of = virtualFileSystemService.of(urlString);
				if (StringUtils.isNotBlank(fileExtensionFilter)) {
					listHandles = of.listHandles(handle -> fileExtensionFilter.equalsIgnoreCase(handle.getExtension()));
				} else {
					listHandles = of.listHandles();
				}

			} catch (IOException e) {
				logger.warn("[{}] Error on listHandle", taskDescriptorId, e);
			}

			for (IVirtualFilesystemHandle fileHandle : listHandles) {
				try {
					runTaskForFile(taskDescriptorId, fileHandle.getAbsolutePath());
				} catch (TaskException e) {
					logger.warn("[{}] Error triggering taskDescriptor", taskDescriptorId, e);
				}
			}

		}

	}

	private ITask runTaskForFile(String taskDescriptorId, String url) throws TaskException {
		logger.debug("[{}] Triggering for url [{}]", taskDescriptorId, url);
		try {
			Map<String, String> runContext = Collections.singletonMap(RunContextParameter.STRING_URL, url);
			return taskService.trigger(taskDescriptorId, null, TaskTriggerType.FILESYSTEM_CHANGE, runContext);

		} catch (IllegalStateException e) {
			throw new TaskException(TaskException.EXECUTION_ERROR, e);
		}
	}

	public Set<String[]> getIncurred() {
		Set<String[]> incurred = new HashSet<String[]>();
		pollMap.entrySet()
				.forEach(es -> incurred.add(new String[] { es.getKey(), es.getValue()[0], es.getValue()[1] }));
		return incurred;
	}
}