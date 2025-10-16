package ch.elexis.core.importer.div.tasks.internal;

import java.util.ArrayList;
import java.util.List;

import org.osgi.service.component.ComponentException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.importer.div.tasks.HL7KIKonsImporterTemplateTaskDescriptor;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.tasks.model.ITaskService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
@Component(immediate = true)
public class HL7KIKonsImporterIdentifiedRunnableFactory implements IIdentifiedRunnableFactory {

	@Inject
	@Reference
	private ITaskService taskService;

	@Inject
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Inject
	@Reference
	private IAccessControlService accessControlService;

	@PostConstruct
	@Activate
	void activate() {
		accessControlService.doPrivileged(() -> {
			try {
				HL7KIKonsImporterTemplateTaskDescriptor.assertTemplate(taskService);
			} catch (TaskException e) {
				LoggerFactory.getLogger(getClass()).error("initialize", e);
				throw new ComponentException(e);
			}
			taskService.bindIIdentifiedRunnableFactory(this);
		});
	}

	@PreDestroy
	@Deactivate
	void deactivate() {
		taskService.unbindIIdentifiedRunnableFactory(this);
	}

	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables() {
		List<IIdentifiedRunnable> ret = new ArrayList<>();
		ret.add(new HL7KIKonsImporterIIdentifiedRunnable(modelService));
		return ret;
	}
}
