package ch.elexis.core.tasks.internal.runnables;

import java.io.File;
import java.io.Serializable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.tasks.RunnableWithContextIdConstants;
import ch.elexis.core.tasks.TaskTriggerTypeParameterConstants;

public class DeleteFileIdentifiedRunnable implements IIdentifiedRunnable {
	
	@Override
	public String getId(){
		return RunnableWithContextIdConstants.RUNNABLE_ID_DELETEFILE;
	}
	
	@Override
	public Map<String, Serializable> run(Map<String, Serializable> context,
		IProgressMonitor progressMonitor, Logger logger){
		
		String eventFilePath = (String) context
			.get(TaskTriggerTypeParameterConstants.FILESYSTEM_CHANGE_RUNPARAM_EVENTFILE_PATH);
		boolean delete = new File(eventFilePath).delete();
		logger.info("Deleted {} {}", eventFilePath, delete);
		
		return null;
	}
	
}
