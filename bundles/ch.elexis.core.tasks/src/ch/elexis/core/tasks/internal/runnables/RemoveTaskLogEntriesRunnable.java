package ch.elexis.core.tasks.internal.runnables;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.SerializableBoolean;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.tasks.model.ITask;
import ch.elexis.core.tasks.model.ModelPackage;
import ch.elexis.core.tasks.model.TaskState;

public class RemoveTaskLogEntriesRunnable implements IIdentifiedRunnable {
	
	public static final String RUNNABLE_ID = "removeTaskLogEntries";
	public static final String DESCRIPTION = "Remove task log entries considering parameters";
	
	/**
	 * run parameter: entries lower equal finished at age in minutes, default: <code>80640</code>
	 * (8*7*24*60) - 8 weeks
	 */
	public static final String RCP_INTEGER_BEQ_FINISHEDAT_AGE_IN_MINUTES = "beqFinishedAtAge";
	/**
	 * run parameter: hard delete (delete from db) <code>true</code> or mark as deleted
	 * <code>false</code>, default: <code>true</code>
	 */
	public static final String RCP_BOOLEAN_HARD_DELETE = "hardDelete";
	
	/**
	 * run parameter: delete only completed tasks without warning or error <code>true</code> or all
	 * <code>false</code>, default: <code>true</code>
	 */
	public static final String RCP_BOOLEAN_ONLY_NO_ERROR = "deleteOnlyNoError";
	
	private IModelService taskModelService;
	
	public RemoveTaskLogEntriesRunnable(IModelService taskModelService){
		this.taskModelService = taskModelService;
	}
	
	@Override
	public String getId(){
		return RUNNABLE_ID;
	}
	
	@Override
	public String getLocalizedDescription(){
		return DESCRIPTION;
	}
	
	@Override
	public Map<String, Serializable> getDefaultRunContext(){
		Map<String, Serializable> defaultRunContext = new HashMap<>();
		defaultRunContext.put(RCP_INTEGER_BEQ_FINISHEDAT_AGE_IN_MINUTES, 80640);
		defaultRunContext.put(RCP_BOOLEAN_HARD_DELETE, Boolean.TRUE);
		defaultRunContext.put(RCP_BOOLEAN_ONLY_NO_ERROR, Boolean.TRUE);
		return defaultRunContext;
	}
	
	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException{
		
		Number beqAgeInMinutes =
			(Number) runContext.get(RCP_INTEGER_BEQ_FINISHEDAT_AGE_IN_MINUTES);
		Long ageBarrierInMilliseconds = System.currentTimeMillis() - (beqAgeInMinutes.longValue() * 60 * 1000);
		
		boolean onlyNoError = SerializableBoolean.valueOf(runContext, RCP_BOOLEAN_ONLY_NO_ERROR);
		boolean hardDelete = SerializableBoolean.valueOf(runContext, RCP_BOOLEAN_HARD_DELETE);
		
		IQuery<ITask> query = taskModelService.getQuery(ITask.class);
		query.and(ModelPackage.Literals.ITASK__FINISHED_AT, COMPARATOR.LESS_OR_EQUAL,
			ageBarrierInMilliseconds);
		if (onlyNoError) {
			query.and(ModelPackage.Literals.ITASK__STATE, COMPARATOR.EQUALS, TaskState.COMPLETED.getValue());
		}
		
		List<ITask> itemsToDelete = query.execute();
		int itemsToDeleteSize = itemsToDelete.size();
		if (hardDelete) {
			taskModelService.remove(itemsToDelete);
		} else {
			taskModelService.delete(itemsToDelete);
		}
		
		String message =
			itemsToDeleteSize + ((hardDelete) ? " removed from database" : " marked as deleted");
		return Collections.singletonMap(ReturnParameter.RESULT_DATA, message);
	}
	
}
