package ch.elexis.core.tasks.internal.service.vfs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;

import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.tasks.internal.service.TaskServiceImpl;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskTriggerTypeParameter;
import ch.elexis.core.utils.CoreUtil;

public class FilesystemChangeWatcher {

	private final TaskServiceImpl taskService;
	private final IVirtualFilesystemService virtualFileSystemService;
	private final IAccessControlService accessControlService;

	private Timer timer;
	private FilesystemChangeWatcherTimerTask timerTask;

	/**
	 * Polls every incurred location every 30 seconds. Works on all
	 * {@link IVirtualFilesystemService} supported urls
	 *
	 * @param taskServiceImpl
	 */
	public FilesystemChangeWatcher(TaskServiceImpl taskService, IVirtualFilesystemService virtualFileSystemService,
			IAccessControlService accessControlService) {
		this.taskService = taskService;
		this.virtualFileSystemService = virtualFileSystemService;
		this.accessControlService = accessControlService;
	}

	private synchronized void startPolling() {
		if (timer == null) {
			timer = new Timer("taskservice-filesystemchange-poller");
			timerTask = new FilesystemChangeWatcherTimerTask(taskService, virtualFileSystemService,
					accessControlService);
			long period = CoreUtil.isTestMode() ? 1 * 1000 : 30 * 1000;
			timer.schedule(timerTask, 0, period);
		}
	}

	public synchronized void stopPolling() {
		if (timer != null) {
			timer.cancel();
			timerTask = null;
			timer = null;
		}
	}

	public void validate(ITaskDescriptor taskDescriptor) throws TaskException {
		Map<String, String> triggerParameters = taskDescriptor.getTriggerParameters();
		String url = triggerParameters.get(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.URL);
		try {
			IVirtualFilesystemHandle vfh = virtualFileSystemService.of(url);
			if (!vfh.isDirectory()) {
				throw new TaskException(TaskException.EXECUTION_REJECTED, "[{}] is not a directory");
			}
		} catch (IOException e) {
			throw new TaskException(TaskException.EXECUTION_REJECTED, e);
		}

	}

	public void incur(ITaskDescriptor taskDescriptor) {
		startPolling();

		Map<String, String> triggerParameters = taskDescriptor.getTriggerParameters();
		String url = triggerParameters.get(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.URL);
		String fileExtensionFilter = triggerParameters
				.get(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.FILE_EXTENSION_FILTER);

		timerTask.incur(taskDescriptor.getId(), url, fileExtensionFilter);
	}

	public void release(ITaskDescriptor taskDescriptor) {
		timerTask.release(taskDescriptor.getId());

		if (getIncurred().isEmpty()) {
			stopPolling();
		}
	}

	public Set<String[]> getIncurred() {
		Set<String[]> incurred = new HashSet<>();
		if (timerTask != null) {
			incurred.addAll(timerTask.getIncurred());
		}
		return incurred;
	}

}
