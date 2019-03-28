package ch.elexis.core.tasks.internal.runnables;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.tasks.RunnableWithContextIdConstants;

public class LogResultContextIdentifiedRunnable implements IIdentifiedRunnable {
	
	@Override
	public String getId(){
		return RunnableWithContextIdConstants.RUNNABLE_ID_LOGRESULTCONTEXT;
	}
	
	@Override
	public Map<String, Serializable> run(Map<String, Serializable> context,
		IProgressMonitor progressMonitor, Logger logger){
		
		context.entrySet().stream().forEach(c -> logger.info("{}: {}", c.getKey(), c.getValue()));
		
		return Collections.emptyMap();
	}

	@Override
	public String getLocalizedDescription(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, Serializable> getDefaultRunContext(){
		return Collections.emptyMap();
	}
	
}
