package ch.elexis.core.tasks.internal.service;

import static ch.elexis.core.tasks.IdentifiedRunnableIdConstants.DELETEFILE;
import static ch.elexis.core.tasks.IdentifiedRunnableIdConstants.LOGRESULTCONTEXT;
import static ch.elexis.core.tasks.IdentifiedRunnableIdConstants.TRIGGER_TASK_FOR_EVERY_FILE;

import java.util.HashMap;
import java.util.Map;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.tasks.internal.runnables.DeleteFileIdentifiedRunnable;
import ch.elexis.core.tasks.internal.runnables.LogResultContextIdentifiedRunnable;
import ch.elexis.core.tasks.internal.runnables.RunTaskForEveryFileInDirectoryRunnable;

@Component
public class IdentifiedRunnableFactoryImpl implements IIdentifiedRunnableFactory {
	
	@Reference
	private IVirtualFilesystemService virtualFilsystemService;
	
	@Override
	public IIdentifiedRunnable createRunnableWithContext(String runnableWithContextId){
		if (runnableWithContextId != null) {
			switch (runnableWithContextId) {
			case LOGRESULTCONTEXT:
				return new LogResultContextIdentifiedRunnable();
			case DELETEFILE:
				return new DeleteFileIdentifiedRunnable();
			case TRIGGER_TASK_FOR_EVERY_FILE:
				return new RunTaskForEveryFileInDirectoryRunnable(virtualFilsystemService);
			default:
				break;
			}
		}
		return null;
	}
	
	@Override
	public Map<String, String> getProvidedRunnables(){
		Map<String, String> ret = new HashMap<>();
		ret.put(DELETEFILE, "TODO");
		ret.put(LOGRESULTCONTEXT, "TODO");
		return ret;
	}
	
}
