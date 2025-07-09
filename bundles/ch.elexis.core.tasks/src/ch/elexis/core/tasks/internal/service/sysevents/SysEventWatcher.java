package ch.elexis.core.tasks.internal.service.sysevents;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.RunContextParameter;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

@Component(property = { EventConstants.EVENT_TOPIC + "=" + ElexisEventTopics.BASE + "*",
		EventConstants.EVENT_TOPIC + "=" + "remote/" + ElexisEventTopics.BASE + "*" })
public class SysEventWatcher implements EventHandler {

	private static Map<String, Set<ITaskDescriptor>> incurred;

	@Reference
	private ITaskService taskService;

	static {
		incurred = new HashMap<>();
	}

	public void incur(ITaskDescriptor taskDescriptor) throws TaskException {
		String topic = fetchTopic(taskDescriptor);
		String clazz = fetchClass(taskDescriptor);
		if (!(StringUtils.startsWith(topic, ElexisEventTopics.BASE)
				|| StringUtils.startsWith(topic, "remote/" + ElexisEventTopics.BASE))) {
			throw new TaskException(TaskException.TRIGGER_REGISTER_ERROR,
					"Invalid topic, must start with [" + ElexisEventTopics.BASE + "] or [" + "remote/"
							+ ElexisEventTopics.BASE + "]");
		}
		registerInMap(topic, clazz, taskDescriptor);
	}

	private void registerInMap(String topic, String clazz, ITaskDescriptor taskDescriptor) {
		synchronized (incurred) {
			Set<ITaskDescriptor> topicTds = incurred.get(topic + "_" + clazz);
			if (topicTds == null) {
				topicTds = new HashSet<>();
			}
			topicTds.add(taskDescriptor);
			incurred.put(topic + "_" + clazz, topicTds);
		}
	}

	public void release(ITaskDescriptor taskDescriptor) {
		String topic = fetchTopic(taskDescriptor);
		String clazz = fetchClass(taskDescriptor);
		synchronized (incurred) {
			Set<ITaskDescriptor> topicTds = incurred.get(topic + "_" + clazz);
			if (topicTds != null) {
				topicTds.remove(taskDescriptor);
			}
			incurred.put(topic + "_" + clazz, topicTds);
		}
	}

	private String fetchTopic(ITaskDescriptor taskDescriptor) {
		Map<String, String> triggerParameters = taskDescriptor.getTriggerParameters();
		return triggerParameters.get("topic");
	}

	private String fetchClass(ITaskDescriptor taskDescriptor) {
		Map<String, String> triggerParameters = taskDescriptor.getTriggerParameters();
		return triggerParameters.get(ElexisEventTopics.PROPKEY_CLASS);
	}

	@Override
	public void handleEvent(Event event) {
		String topic = event.getTopic();
		String clazz = (String) event.getProperty(ElexisEventTopics.PROPKEY_CLASS);
		// TODO event origin, is this task descriptor handling all or only self?

		Set<ITaskDescriptor> set = incurred.get(topic + "_" + clazz);
		if (set != null) {
			for (ITaskDescriptor taskDescriptor : set) {
				try {
					Map<String, String> runContext = new HashMap<>();
					runContext.put(RunContextParameter.IDENTIFIABLE_ID,
							(String) event.getProperty(ElexisEventTopics.PROPKEY_ID));

					taskService.trigger(taskDescriptor, null, TaskTriggerType.SYSTEM_EVENT, runContext);
				} catch (TaskException e) {
					LoggerFactory.getLogger(getClass().getName() + "_" + taskDescriptor.getId())
							.warn("Error triggering taskDescriptor", e);
				}
			}
		}
	}

}
