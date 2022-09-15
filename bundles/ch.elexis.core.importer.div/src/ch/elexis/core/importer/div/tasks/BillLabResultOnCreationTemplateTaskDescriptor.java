package ch.elexis.core.importer.div.tasks;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import ch.elexis.core.eenv.IElexisEnvironmentService;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class BillLabResultOnCreationTemplateTaskDescriptor {

	public static void assertTemplate(ITaskService taskService) throws TaskException {
		boolean templateRequired = taskService
				.findTaskDescriptorByIIdentifiedRunnableId(BillLabResultOnCreationIdentifiedRunnable.RUNNABLE_ID)
				.isEmpty();

		Map<String, String> triggerParam = new HashMap<String, String>();
		triggerParam.put("topic", "info/elexis/po/create");
		triggerParam.put("class", "ch.elexis.data.LabResult");

		if (templateRequired) {
			String referenceId = "template_" + BillLabResultOnCreationIdentifiedRunnable.RUNNABLE_ID;
			LoggerFactory.getLogger(BillLabResultOnCreationIdentifiedRunnable.class)
					.info("Initializing taskdescriptor-template [{}]", referenceId);

			ITaskDescriptor taskDescriptor = taskService
					.createTaskDescriptor(new BillLabResultOnCreationIdentifiedRunnable(null, null));
			taskDescriptor.setReferenceId(referenceId);
			taskDescriptor.setTriggerType(TaskTriggerType.SYSTEM_EVENT);
			taskDescriptor.setRunner(IElexisEnvironmentService.ES_STATION_ID_DEFAULT);
			taskDescriptor.setTriggerParameters(triggerParam);
			taskService.saveTaskDescriptor(taskDescriptor);
		}
	}

}
