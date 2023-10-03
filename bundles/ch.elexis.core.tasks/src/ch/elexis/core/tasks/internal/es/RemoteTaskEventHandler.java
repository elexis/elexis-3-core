package ch.elexis.core.tasks.internal.es;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

/**
 * Incoming event from E to ES
 * 
 * @see info.elexis.server.core.connector.elexis.rest.legacy.EventService
 */
@Component(property = EventConstants.EVENT_TOPIC + "=" + "remote/" + ElexisEventTopics.TASK_SERVICE + "*")
public class RemoteTaskEventHandler implements EventHandler {

	@Reference
	private ITaskService taskService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.tasks.model)")
	private IModelService taskModelService;

	@Override
	public void handleEvent(Event event) {
		String taskDescriptorId = (String) event.getProperty(ElexisEventTopics.PROPKEY_ID);
		ITaskDescriptor taskDescriptor = taskModelService.load(taskDescriptorId, ITaskDescriptor.class, true, true)
				.orElse(null);

		String topic = event.getTopic();
		try {
			if (topic.endsWith("trigger")) {
				taskService.trigger(taskDescriptor, null, TaskTriggerType.MANUAL, null);
			} else if (topic.endsWith("refresh")) {
				taskService.refresh(taskDescriptor);
			}
		} catch (TaskException e) {
			LoggerFactory.getLogger(getClass()).warn("Error refreshing taskDescriptor [{}]", taskDescriptorId, e);
		}

	}

}
