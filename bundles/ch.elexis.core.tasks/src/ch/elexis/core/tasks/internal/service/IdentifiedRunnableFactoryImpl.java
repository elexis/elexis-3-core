package ch.elexis.core.tasks.internal.service;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.tasks.internal.runnables.DeleteFileIdentifiedRunnable;
import ch.elexis.core.tasks.internal.runnables.LogResultContextIdentifiedRunnable;
import ch.elexis.core.tasks.internal.runnables.TriggerTaskForEveryFileInDirectoryRunnable;

@Component
public class IdentifiedRunnableFactoryImpl implements IIdentifiedRunnableFactory {
	
	@Reference
	private IVirtualFilesystemService virtualFilsystemService;
	
	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables(){
		List<IIdentifiedRunnable> ret = new ArrayList<>();
		ret.add(new LogResultContextIdentifiedRunnable());
		ret.add(new DeleteFileIdentifiedRunnable());
		ret.add(new TriggerTaskForEveryFileInDirectoryRunnable(virtualFilsystemService));
		return ret;
	}
	
}
