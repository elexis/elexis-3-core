package ch.elexis.core.tasks.internal.runnables;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.tasks.IdentifiedRunnableIdConstants;

public class DeleteFileIdentifiedRunnable implements IIdentifiedRunnable {
	
	@Override
	public String getId(){
		return IdentifiedRunnableIdConstants.DELETEFILE;
	}
	
	@Override
	public Map<String, Serializable> run(Map<String, Serializable> context,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException{
		
		String eventFilePath = (String) context.get(RunContextParameter.STRING_URL);
		
		// TODO switch to VirtualFilesystemService
		
		Path path = Paths.get(eventFilePath);
		try {
			Files.delete(path);
			logger.info("Deleted {}", eventFilePath);
		} catch (IOException e) {
			throw new TaskException(TaskException.EXECUTION_ERROR, "Error deleting file [{}]", e);
		}
		
		return null;
	}
	
	@Override
	public String getLocalizedDescription(){
		return "Delete a single file";
	}
	
	@Override
	public Map<String, Serializable> getDefaultRunContext(){
		return Collections.singletonMap(RunContextParameter.STRING_URL,
			RunContextParameter.VALUE_MISSING_REQUIRED);
	}
	
}
