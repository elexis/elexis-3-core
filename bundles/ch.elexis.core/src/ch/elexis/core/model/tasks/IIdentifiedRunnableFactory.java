package ch.elexis.core.model.tasks;

import java.util.List;

/**
 * Factory to register {@link IIdentifiedRunnable} to the TaskService
 * 
 * @since 3.10 removed initialize method
 */
public interface IIdentifiedRunnableFactory {

	/**
	 * @return the {@link IIdentifiedRunnable}s provided by this factory, use
	 *         {@link IIdentifiedRunnable#getId()}. Instantiate new objects on every
	 *         call, task service will care for caching if applicable.
	 */
	List<IIdentifiedRunnable> getProvidedRunnables();

}
