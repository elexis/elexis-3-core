package ch.elexis.core.services;

import java.util.Optional;

public interface IContext {
	
	String ACTIVE_USERCONTACT = "ch.elexis.core.services.icontext.active.usercontact";
	
	String STATION_IDENTIFIER = "ch.elexis.core.services.icontext.stationidentifier";

	/**
	 * Get a string identifying this station (do not use the $ sign within the id).<br>
	 * If multiple instances are run on a station, the stationId has to append the instance number
	 * with <code>$</code>.<br>
	 * For example: first instance <code>stationId</code>, second instance <code>stationId$2</code>
	 * 
	 * @return
	 */
	public String getStationIdentifier();
	
	/**
	 * Get an {@link Object} identified by its type from the context.
	 * 
	 * @param clazz
	 * @return
	 */
	public <T> Optional<T> getTyped(Class<T> clazz);
	
	/**
	 * Set an {@link Object} identified by its type in the context.
	 * 
	 * @param object
	 */
	public void setTyped(Object object);
	
	/**
	 * Remove the object set for the type clazz from the context.
	 * 
	 * @param clazz
	 */
	void removeTyped(Class<?> clazz);
	
	/**
	 * Get an {@link Object} identified by the name from the context.
	 * 
	 * @param name
	 * @return
	 */
	public Optional<?> getNamed(String name);
	
	/**
	 * Set an {@link Object} identified by the name from the context.
	 * 
	 * @param name
	 * @param object
	 */
	public void setNamed(String name, Object object);
}
