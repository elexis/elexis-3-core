package ch.elexis.core.tasks.test.runnable;

import java.util.Arrays;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.services.IContextService;

@Component
public class TestExecutionContextIdentifiedRunnableFactory implements IIdentifiedRunnableFactory {
	
	@Reference
	private IContextService contextService;
	
	@Override
	public List<IIdentifiedRunnable> getProvidedRunnables(){
		return Arrays.asList(new TestExecutionContextRunnable(contextService));
	}
	
}
