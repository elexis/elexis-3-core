package ch.elexis.core.model.tasks;

import java.util.List;

public interface IIdentifiedRunnableFactory {
	
	/**
	 * Before calling {@link #getProvidedRunnables()} the first time, this method will be called. Do
	 * not introduce a dependency to ITaskService on instantiating this class, it would be circular.
	 * Use this method instead.
	 * 
	 * If an exception is thrown within this method, the factory will not be added.
	 * 
	 * @param taskService
	 *            castable to ITaskService
	 */
	default void initialize(Object taskService){};
	
	/**
	 * @return the {@link IIdentifiedRunnable}s provided by this factory, use
	 *         {@link IIdentifiedRunnable#getId()}. Instantiate new objects on every call, task
	 *         service will care for caching if applicable.
	 */
	List<IIdentifiedRunnable> getProvidedRunnables();
	
}
