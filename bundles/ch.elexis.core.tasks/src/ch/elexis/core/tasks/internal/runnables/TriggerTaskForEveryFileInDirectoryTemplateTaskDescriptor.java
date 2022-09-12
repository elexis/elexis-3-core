package ch.elexis.core.tasks.internal.runnables;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.IdentifiedRunnableIdConstants;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class TriggerTaskForEveryFileInDirectoryTemplateTaskDescriptor {

	public static void assertTemplate(ITaskService taskService) throws TaskException {
		boolean templateRequired = taskService
				.findTaskDescriptorByIIdentifiedRunnableId(IdentifiedRunnableIdConstants.TRIGGER_TASK_FOR_EVERY_FILE)
				.isEmpty();

		if (templateRequired) {
			String referenceId = "template_" + IdentifiedRunnableIdConstants.TRIGGER_TASK_FOR_EVERY_FILE;
			LoggerFactory.getLogger(TriggerTaskForEveryFileInDirectoryRunnable.class)
					.info("Initializing taskdescriptor-template [{}]", referenceId);

			Map<String, Serializable> runContext = new HashMap<String, Serializable>();
			runContext.put("url", "missingUrl");
			runContext.put("taskDescriptorReferenceId", "hl7ImportTask");
			runContext.put(TriggerTaskForEveryFileInDirectoryRunnable.RCP_STRING_FILE_EXTENSION_FILTER, "hl7");

			ITaskDescriptor taskDescriptor = taskService
					.createTaskDescriptor(new TriggerTaskForEveryFileInDirectoryRunnable(null));
			taskDescriptor.setReferenceId(referenceId);
			taskDescriptor.setTriggerType(TaskTriggerType.CRON);
			taskDescriptor.setTriggerParameter("cron", "0/30 * * * * ?");
			taskDescriptor.setRunner(IElexisEnvironmentService.ES_STATION_ID_DEFAULT);
			taskDescriptor.setRunContext(runContext);
			taskDescriptor.setSingleton(true);
			taskService.saveTaskDescriptor(taskDescriptor);
		}

	}
}
