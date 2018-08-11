package ch.elexis.core.services;

import java.util.Optional;

import ch.elexis.core.common.ElexisEventTopics;

public interface IContextService {
	
	/**
	 * Get the root context of the service.
	 * 
	 * @return
	 */
	public IContext getRootContext();
	
	/**
	 * Get the {@link IContext} created with matching name.
	 * 
	 * @param name
	 * @return
	 */
	public Optional<IContext> getNamedContext(String name);
	
	/**
	 * Create a new {@link IContext} with the name.
	 * 
	 * @param name
	 * @return
	 */
	public IContext createNamedContext(String name);
	
	/**
	 * Release the context, and make it available for garbage collection. Root context can not be
	 * released.
	 * 
	 * @param context
	 * @return
	 */
	public void releaseContext(String name);
	
	/**
	 * Post an Event using a topic (see {@link ElexisEventTopics}), and an optional object.
	 * 
	 * @param eventTopic
	 * @param object
	 */
	public void postEvent(String eventTopic, Object object);
}
