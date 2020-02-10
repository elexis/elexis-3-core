package ch.elexis.core.tasks.test.runnable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.ITaskService;

@Component
public class TestExecutionContextIdentifiedRunnableFactory implements IIdentifiedRunnableFactory {
	
	@Reference
	private IContextService contextService;
	
	@Override
	public void initialize(Object taskService){
		assertTrue(taskService instanceof ITaskService);
		
		Optional<ITaskDescriptor> findTaskDescriptorByIdOrReferenceId =
			((ITaskService) taskService).findTaskDescriptorByIdOrReferenceId("thisIsJustAnAccessTest");
		assertFalse(findTaskDescriptorByIdOrReferenceId.isPresent());
	}
	
	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables(){
		return Arrays.asList(new TestExecutionContextRunnable(contextService));
	}
	
}
