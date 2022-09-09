package ch.elexis.core.tasks.internal.service;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.ComponentException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.tasks.internal.runnables.DeleteFileIdentifiedRunnable;
import ch.elexis.core.tasks.internal.runnables.LogResultContextIdentifiedRunnable;
import ch.elexis.core.tasks.internal.runnables.RemoveTaskLogEntriesRunnable;
import ch.elexis.core.tasks.internal.runnables.TriggerTaskForEveryFileInDirectoryRunnable;
import ch.elexis.core.tasks.internal.runnables.TriggerTaskForEveryFileInDirectoryTemplateTaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;

@Component
public class IdentifiedRunnableFactoryImpl implements IIdentifiedRunnableFactory {

	@Reference
	private IVirtualFilesystemService virtualFilsystemService;

	private IModelService taskModelService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.tasks.model)")
	private void setModelService(IModelService modelService) {
		taskModelService = modelService;
	}

	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables() {
		List<IIdentifiedRunnable> ret = new ArrayList<>();
		ret.add(new LogResultContextIdentifiedRunnable());
		ret.add(new DeleteFileIdentifiedRunnable());
		ret.add(new TriggerTaskForEveryFileInDirectoryRunnable(virtualFilsystemService));
		ret.add(new RemoveTaskLogEntriesRunnable(taskModelService));
		return ret;
	}

	@Override
	public void initialize(Object taskService) {
		try {
			TriggerTaskForEveryFileInDirectoryTemplateTaskDescriptor.assertTemplate((ITaskService) taskService);
		} catch (TaskException e) {
			LoggerFactory.getLogger(getClass()).error("initialize", e);
			throw new ComponentException(e);
		}
	}

}
