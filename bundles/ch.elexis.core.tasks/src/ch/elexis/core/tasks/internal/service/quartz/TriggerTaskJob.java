package ch.elexis.core.tasks.internal.service.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class TriggerTaskJob implements Job {

	@Override
	public void execute(JobExecutionContext executionContext) throws JobExecutionException {
		ITaskService taskService = (ITaskService) executionContext.getMergedJobDataMap().get("taskService");
		ITaskDescriptor taskDescriptor = (ITaskDescriptor) executionContext.getMergedJobDataMap().get("taskDescriptor");

		AccessControlServiceHolder.get().doPrivileged(() -> {
			try {
				taskService.trigger(taskDescriptor, null, TaskTriggerType.CRON, null);
			} catch (TaskException e) {
				LoggerFactory.getLogger(getClass().getName() + "_" + taskDescriptor.getId())
						.warn("[{}] Error triggering task for quartz ", taskDescriptor.getId(), e);
			}
		});
	}

}
