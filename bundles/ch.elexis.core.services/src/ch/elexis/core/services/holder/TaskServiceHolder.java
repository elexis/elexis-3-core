package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.tasks.model.ITaskService;

@Component
public class TaskServiceHolder {
	
	private static ITaskService taskService;
	
	@Reference
	public void setTaskService(ITaskService taskService){
		TaskServiceHolder.taskService = taskService;
	}
	
	public static ITaskService get() {
		if (taskService == null) {
			throw new IllegalStateException("No ITaskService available");
		}
		return taskService;
	}
}