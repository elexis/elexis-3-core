package ch.elexis.core.tasks.internal.runnables;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.tasks.IdentifiedRunnableIdConstants;

public class DeleteFileIdentifiedRunnable implements IIdentifiedRunnable {

	private IVirtualFilesystemService virtualFilesystemService;

	public DeleteFileIdentifiedRunnable(IVirtualFilesystemService virtualFilesystemService) {
		this.virtualFilesystemService = virtualFilesystemService;
	}

	@Override
	public String getId() {
		return IdentifiedRunnableIdConstants.DELETEFILE;
	}

	@Override
	public Map<String, Serializable> run(Map<String, Serializable> context, IProgressMonitor progressMonitor,
			Logger logger) throws TaskException {

		String eventFilePath = (String) context.get(RunContextParameter.STRING_URL);

		try {
			IVirtualFilesystemHandle vfsHandle = virtualFilesystemService.of(eventFilePath);
			vfsHandle.delete();
			logger.info("Deleted {}", eventFilePath);
		} catch (IOException e) {
			throw new TaskException(TaskException.EXECUTION_ERROR, "Error deleting file [" + eventFilePath + "]", e);
		}

		return null;
	}

	@Override
	public String getLocalizedDescription() {
		return "Delete a single file";
	}

	@Override
	public Map<String, Serializable> getDefaultRunContext() {
		return Collections.singletonMap(RunContextParameter.STRING_URL, RunContextParameter.VALUE_MISSING_REQUIRED);
	}

}
