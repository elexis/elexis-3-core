package ch.elexis.core.model.tasks;

import java.util.List;

public interface IIdentifiedRunnableFactory {
	
	/**
	 * @return the {@link IIdentifiedRunnable}s provided by this factory, use
	 *         {@link IIdentifiedRunnable#getId()}. Instantiate new objects on every call, task service
	 *         will care for caching if applicable.
	 */
	List<IIdentifiedRunnable> getProvidedRunnables();
	
}
