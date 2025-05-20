package ch.elexis.core.mediorder.mail.task;

import static com.cronutils.model.field.expression.FieldExpressionFactory.always;
import static com.cronutils.model.field.expression.FieldExpressionFactory.on;
import static com.cronutils.model.field.expression.FieldExpressionFactory.questionMark;

import com.cronutils.builder.CronBuilder;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;

import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;
import ch.elexis.core.tasks.model.TaskTriggerType;

public class MediorderMailTaskDescriptor {

	public static ITaskDescriptor getOrCreate(ITaskService taskService) throws TaskException {
		ITaskDescriptor taskDescriptor = taskService.findTaskDescriptorByIdOrReferenceId(Constants.RUNNABLE_ID)
				.orElse(null);
		if (taskDescriptor == null) {
			taskDescriptor = taskService
					.createTaskDescriptor(new MediorderMailIdentifiedRunnable(null, null, null, null, null));
			taskDescriptor.setReferenceId(Constants.RUNNABLE_ID);
			taskDescriptor.setTriggerType(TaskTriggerType.CRON);
			taskDescriptor.setSystem(true);
			taskDescriptor.setSingleton(true);
			taskDescriptor.setActive(true);
			
			CronBuilder cron = CronBuilder.cron(CronDefinitionBuilder.instanceDefinitionFor(CronType.QUARTZ));
			String cronString = cron.withSecond(on(0)).withMinute(on(0)).withHour(always()).withDoM(questionMark())
					.withMonth(always()).withDoW(always()).withYear(always()).instance().asString();
			taskDescriptor.setTriggerParameter("cron", cronString);
			taskService.saveTaskDescriptor(taskDescriptor);
		}
		return taskDescriptor;

	}
	
}
