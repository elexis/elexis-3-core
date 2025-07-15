package ch.elexis.core.importer.div.tasks.internal;

import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.OwnerTaskNotification;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class HL7ImporterTemplateTaskDescriptor {

	public static void assertTemplate(ITaskService taskService) throws TaskException {
		boolean templateRequired = taskService
				.findTaskDescriptorByIIdentifiedRunnableId(HL7ImporterIIdentifiedRunnable.RUNNABLE_ID).isEmpty();

		if (templateRequired) {
			String referenceId = "template_" + HL7ImporterIIdentifiedRunnable.RUNNABLE_ID;
			LoggerFactory.getLogger(HL7ImporterTemplateTaskDescriptor.class)
					.info("Initializing taskdescriptor-template [{}]", referenceId);

			ITaskDescriptor taskDescriptor = taskService
					.createTaskDescriptor(new HL7ImporterIIdentifiedRunnable(null, null, null));
			taskDescriptor.setReferenceId(referenceId);
			taskDescriptor.setOwnerNotification(OwnerTaskNotification.WHEN_FINISHED_FAILED);
			taskDescriptor.setTriggerType(TaskTriggerType.FILESYSTEM_CHANGE);
			taskDescriptor.setRunner(IElexisEnvironmentService.ES_STATION_ID_DEFAULT);
			taskDescriptor.setSingleton(true);
			taskService.saveTaskDescriptor(taskDescriptor);
		}
	}
}
