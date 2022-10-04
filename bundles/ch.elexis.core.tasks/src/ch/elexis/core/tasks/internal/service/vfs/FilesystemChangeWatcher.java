package ch.elexis.core.tasks.internal.service.vfs;

import java.util.Map;
import java.util.Timer;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.tasks.internal.service.TaskServiceImpl;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskTriggerTypeParameter;
import ch.elexis.core.utils.CoreUtil;

public class FilesystemChangeWatcher {

	private final TaskServiceImpl taskService;
	private final IVirtualFilesystemService virtualFileSystemService;

	private Timer timer;
	private FilesystemChangeWatcherTimerTask timerTask;

	/**
	 * Polls every incurred location every 30 seconds. Works on all
	 * {@link IVirtualFilesystemService} supported urls
	 * 
	 * @param taskServiceImpl
	 */
	public FilesystemChangeWatcher(TaskServiceImpl taskService, IVirtualFilesystemService virtualFileSystemService) {
		this.taskService = taskService;
		this.virtualFileSystemService = virtualFileSystemService;
	}

	public void startPolling() {
		if (timer == null) {
			timer = new Timer("taskservice-filesystemchange-poller");
			timerTask = new FilesystemChangeWatcherTimerTask(taskService, virtualFileSystemService);
			long period = CoreUtil.isTestMode() ? 1 * 1000 : 30 * 1000;
			timer.schedule(timerTask, 0, period);
		}
	}

	public void stopPolling() {
		timer.cancel();
		timer = null;
		timerTask = null;
	}

	public void incur(ITaskDescriptor taskDescriptor) {
		Map<String, String> triggerParameters = taskDescriptor.getTriggerParameters();
		String url = triggerParameters.get(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.URL);
		String fileExtensionFilter = triggerParameters
				.get(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.FILE_EXTENSION_FILTER);

		timerTask.incur(taskDescriptor.getId(), url, fileExtensionFilter);
	}

	public void release(ITaskDescriptor taskDescriptor) {
		timerTask.release(taskDescriptor.getId());
	}

}
