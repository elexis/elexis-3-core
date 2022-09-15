package ch.elexis.core.tasks.test.runnable;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;

@Component(immediate = true)
public class TestExecutionContextIdentifiedRunnableFactory implements IIdentifiedRunnableFactory {

	@Reference
	private ITaskService taskService;

	@Reference
	private IContextService contextService;

	@Activate
	public void activate() {

		Optional<ITaskDescriptor> findTaskDescriptorByIdOrReferenceId = ((ITaskService) taskService)
				.findTaskDescriptorByIdOrReferenceId("thisIsJustAnAccessTest");
		assertFalse(findTaskDescriptorByIdOrReferenceId.isPresent());
		taskService.bindIIdentifiedRunnableFactory(this);
	}

	@Deactivate
	public void deactivate() {
		taskService.unbindIIdentifiedRunnableFactory(this);
	}

	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables() {
		return Arrays.asList(new TestExecutionContextRunnable(contextService));
	}

}
