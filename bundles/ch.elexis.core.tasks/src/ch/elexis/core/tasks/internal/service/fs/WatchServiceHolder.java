package ch.elexis.core.tasks.internal.service.fs;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.TaskTriggerTypeParameterConstants;
import ch.elexis.core.tasks.internal.service.TaskServiceImpl;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class WatchServiceHolder {
	
	private Logger logger;
	private final TaskServiceImpl taskService;
	
	private final WatchService watchService;
	private WatchServicePoller pollerThread;
	
	private Map<WatchKey, ITaskDescriptor> incurredTasks;
	
	public WatchServiceHolder(TaskServiceImpl taskServiceImpl){
		logger = LoggerFactory.getLogger(getClass());
		taskService = taskServiceImpl;
		
		incurredTasks = Collections.synchronizedMap(new HashMap<WatchKey, ITaskDescriptor>());
		
		WatchService initWatchService;
		try {
			initWatchService = FileSystems.getDefault().newWatchService();
		} catch (IOException ioe) {
			initWatchService = null;
			logger.error(
				"Error instantiating WatchService, filesystem events will not be picked up.", ioe);
		}
		watchService = initWatchService;
		
		pollerThread = new WatchServicePoller();
	}
	
	public boolean triggerIsAvailable(){
		return (watchService != null && pollerThread != null);
	}
	
	public void startPolling(){
		pollerThread.start();
	}
	
	public void stopPolling(){
		// TODO
	}
	
	public void incur(ITaskDescriptor taskDescriptor) throws TaskException{
		if (watchService != null) {
			String pathParameter = taskDescriptor.getTriggerParameters()
				.get(TaskTriggerTypeParameterConstants.FILESYSTEM_CHANGE_PARAM_DIRECTORY_PATH);
			if (pathParameter != null && pathParameter.length() > 0) {
				try {
					Path path = Paths.get(pathParameter);
					if (path.toFile().isDirectory() && path.toFile().canRead()) {
						WatchEvent.Kind<?>[] events = {
							StandardWatchEventKinds.ENTRY_CREATE
						};
						WatchKey watchKey = path.register(watchService, events);
						logger.debug("Watching [{}]", pathParameter);
						incurredTasks.put(watchKey, taskDescriptor);
					} else {
						throw new TaskException(TaskException.TRIGGER_REGISTER_ERROR,
							new Throwable("path is not a directory or not readable"));
					}
				} catch (InvalidPathException | IOException e) {
					throw new TaskException(TaskException.TRIGGER_REGISTER_ERROR, e);
				}
			}
		} else {
			throw new TaskException(TaskException.TRIGGER_NOT_AVAILABLE, new Throwable());
		}
	}
	
	public void release(ITaskDescriptor taskDescriptor){
		//TODO
	}
	
	private class WatchServicePoller extends Thread {
		
		@Override
		public void run(){
			logger.debug("Start polling");
			WatchKey key = null;
			for (;;) {
				try {
					// on os x it can take up to 10 seconds until changes are picked up
					// https://www.reddit.com/r/java/comments/3vtv8i/beware_javaniofilewatchservice_is_subtly_broken/
					key = watchService.poll(250, TimeUnit.MILLISECONDS);
					if (key == null) {
						continue;
					}
					ITaskDescriptor taskDescriptor = incurredTasks.get(key);
					List<WatchEvent<?>> pollEvents = key.pollEvents();
					for (WatchEvent<?> watchEvent : pollEvents) {
						if (taskDescriptor != null) {
							// the watchkey does not know its base path
							String watcherPath = taskDescriptor.getTriggerParameters().get(
								TaskTriggerTypeParameterConstants.FILESYSTEM_CHANGE_PARAM_DIRECTORY_PATH);
							
							Path name = (Path) watchEvent.context();
							String fullPath = Paths.get(watcherPath, name.toString()).toString();
							logger.debug("{} -> {}", watchEvent.kind(), fullPath);
							
							Map<String, String> runContext = new HashMap<>();
							
							runContext.put(
								TaskTriggerTypeParameterConstants.FILESYSTEM_CHANGE_RUNPARAM_EVENTFILE_PATH,
								fullPath);
							
							trigger(taskDescriptor, runContext);
						} else {
							logger.error(
								"No taskDescriptor registered for the provided watchKey [{}], removing key",
								key);
							incurredTasks.remove(key);
						}
					}
					key.reset();
				} catch (InterruptedException e) {
					logger.error("Interrupted", e);
				}
			}
		}
		
		private void trigger(ITaskDescriptor taskDescriptor, Map<String, String> runContext){
			try {
				logger.debug("Triggering {}", taskDescriptor);
				taskService.trigger(taskDescriptor, null, TaskTriggerType.FILESYSTEM_CHANGE,
					runContext);
			} catch (TaskException e) {
				logger.warn("Could not trigger task [" + taskDescriptor.getId() + "]", e);
			}
		}
		
	}
	
}
