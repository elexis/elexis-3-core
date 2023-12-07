package ch.elexis.core.tasks.test;

import java.util.List;

import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ModelPackage;

public class TaskServiceTestUtil {

	private IModelService taskModelService;

	public TaskServiceTestUtil(IModelService taskModelService) {
		this.taskModelService = taskModelService;
	}

	public List<ITask> getTasks(ITaskDescriptor taskDescriptor) {
		IQuery<ITask> query = taskModelService.getQuery(ITask.class);
		query.and(ModelPackage.Literals.ITASK__TASK_DESCRIPTOR, COMPARATOR.EQUALS, taskDescriptor);
		return query.execute();
	}

}
