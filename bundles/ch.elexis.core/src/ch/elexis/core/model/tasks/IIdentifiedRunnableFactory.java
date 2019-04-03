package ch.elexis.core.model.tasks;

import java.util.Map;

public interface IIdentifiedRunnableFactory {
	
	/**
	 * 
	 * @return a {@link Map} with the {@link IIdentifiedRunnable}s provided by this factory as key, and their
	 *         localized description as value
	 */
	Map<String, String> getProvidedRunnables();
	
	/**
	 * 
	 * @param runnableWithContextId
	 * @return
	 */
	IIdentifiedRunnable createRunnableWithContext(String runnableWithContextId);
	
}
