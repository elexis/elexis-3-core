package ch.elexis.core.tasks.test.runnable;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.TaskException;
import ch.elexis.core.services.IContextService;

/**
 * Returns the runContext as result
 */
public class TestExecutionContextRunnable implements IIdentifiedRunnable {
	
	public static final String ID = "testExecutionContextRunnable";
	
	private IContextService contextService;
	
	public TestExecutionContextRunnable(IContextService contextService){
		this.contextService = contextService;
	}
	
	@Override
	public String getId(){
		return ID;
	}
	
	@Override
	public String getLocalizedDescription(){
		return "test execution context";
	}
	
	@Override
	public Map<String, Serializable> getDefaultRunContext(){
		return Collections.emptyMap();
	}
	
	@Override
	public Map<String, Serializable> run(Map<String, Serializable> runContext,
		IProgressMonitor progressMonitor, Logger logger) throws TaskException{
		
		IUser user = contextService.getActiveUser()
			.orElseThrow(() -> new TaskException(TaskException.EXECUTION_ERROR, "No active user"));
		assertEquals("testUser", user.getId());
		
		IMandator mandator = contextService.getActiveMandator().orElseThrow(
			() -> new TaskException(TaskException.EXECUTION_ERROR, "No active mandator"));
		assertEquals(user.getAssignedContact().getId(), mandator.getId());
		
		return runContext;
	}
	
}
