package ch.elexis.core.model.tasks;

public interface IIdentifiedRunnableFactory {
	
	/**
	 * 
	 * @param runnableWithContextId
	 * @return
	 */
	IIdentifiedRunnable createRunnableWithContext(String runnableWithContextId);
	
}
