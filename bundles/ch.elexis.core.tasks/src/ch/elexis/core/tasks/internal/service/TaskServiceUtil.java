package ch.elexis.core.tasks.internal.service;

import java.util.List;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ModelPackage;

public class TaskServiceUtil {
	
	/**
	 * Load the (active) task descriptors for a given runnable we are responsible for (that is, assigned as
	 * runner)
	 * 
	 * @param identifiedRunnable
	 * @param taskModelService
	 * @param contextService
	 * @return
	 */
	List<ITaskDescriptor> loadForIdentifiedRunnable(IIdentifiedRunnable identifiedRunnable,
		IModelService taskModelService, IContextService contextService){
		IQuery<ITaskDescriptor> query = taskModelService.getQuery(ITaskDescriptor.class);
		query.and(ModelPackage.Literals.ITASK_DESCRIPTOR__ACTIVE, COMPARATOR.EQUALS, true);
		query.and(ModelPackage.Literals.ITASK_DESCRIPTOR__IDENTIFIED_RUNNABLE_ID, COMPARATOR.EQUALS,
			identifiedRunnable.getId());
		query.startGroup();
		query.and(ModelPackage.Literals.ITASK_DESCRIPTOR__RUNNER, COMPARATOR.EQUALS,
			contextService.getStationIdentifier());
		query.or(ModelPackage.Literals.ITASK_DESCRIPTOR__RUNNER, COMPARATOR.EQUALS, null);
		query.andJoinGroups();
		return query.execute();
	}
	
}
