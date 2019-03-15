package ch.elexis.core.tasks.internal.service;

import static ch.elexis.core.tasks.RunnableWithContextIdConstants.RUNNABLE_ID_DELETEFILE;
import static ch.elexis.core.tasks.RunnableWithContextIdConstants.RUNNABLE_ID_LOGRESULTCONTEXT;

import org.osgi.service.component.annotations.Component;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnableFactory;
import ch.elexis.core.tasks.internal.runnables.DeleteFileIdentifiedRunnable;
import ch.elexis.core.tasks.internal.runnables.LogResultContextIdentifiedRunnable;

@Component
public class RunnableWithContextFactoryImpl implements IIdentifiedRunnableFactory {
	
	@Override
	public IIdentifiedRunnable createRunnableWithContext(String runnableWithContextId){
		if (runnableWithContextId != null) {
			switch (runnableWithContextId) {
			case RUNNABLE_ID_LOGRESULTCONTEXT:
				return new LogResultContextIdentifiedRunnable();
			case RUNNABLE_ID_DELETEFILE:
				return new DeleteFileIdentifiedRunnable();
			default:
				break;
			}
		}
		return null;
	}
	
}
