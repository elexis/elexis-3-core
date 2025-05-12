package ch.elexis.core.mediorder.mail.task;

import java.util.ArrayList;
import java.util.List;
import org.osgi.service.component.ComponentException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IStickerService;
import ch.elexis.core.services.IStockService;
import ch.elexis.core.services.ITextReplacementService;
import ch.elexis.core.tasks.model.ITaskService;

@Component(immediate = true)
public class MediorderMailRunnableFactory implements IIdentifiedRunnableFactory {

	@Reference
	private IAccessControlService accessControlService;

	@Reference
	private ITaskService taskService;

	@Reference
	private IStockService stockService;

	@Reference
	private IStickerService stickerService;

	@Reference
	private IContextService contextService;

	@Reference
	private ITextReplacementService textReplacementService;

	private IModelService coreModelService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private void setModelService(IModelService modelService) {
		coreModelService = modelService;
	}

	@Activate
	public void activate() {
		accessControlService.doPrivileged(() -> {
			try {
				MediorderMailTaskDescriptor.getOrCreate((ITaskService) taskService);
			} catch (TaskException e) {
				throw new ComponentException(e);
			}
		});
		taskService.bindIIdentifiedRunnableFactory(this);
	}
	
	@Deactivate
	public void deactivate() {
		taskService.unbindIIdentifiedRunnableFactory(this);
	}

	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables() {
		List<IIdentifiedRunnable> ret = new ArrayList<>();
		ret.add(new MediorderMailIdentifiedRunnable(coreModelService, contextService, textReplacementService,
				stockService, stickerService));
		return ret;
	}
	
}
