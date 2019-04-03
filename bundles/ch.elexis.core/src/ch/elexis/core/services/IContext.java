package ch.elexis.core.services;

import java.util.Optional;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;

public interface IContext {
	
	String ACTIVE_USER = "ch.elexis.core.services.icontext.active.user";
	String ACTIVE_USERCONTACT = "ch.elexis.core.services.icontext.active.usercontact";
	String ACTIVE_PATIENT = "ch.elexis.core.services.icontext.active.patient";
	String ACTIVE_MANDATOR = "ch.elexis.core.services.icontext.active.mandator";
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
	 * Get the active {@link IUser} from the context.
	 * 
	 * @return
	 */
	public Optional<IUser> getActiveUser();
	
	/**
	 * Set the active {@link IUser} of the context.
	 * 
	 * @param user
	 */
	public void setActiveUser(IUser user);
	
	/**
	 * Get the active {@link IContact} of the {@link IUser} from the context.
	 * 
	 * @return
	 */
	public Optional<IContact> getActiveUserContact();
	
	/**
	 * Set the active {@link IContact} of the {@link IUser} of the context.
	 * 
	 * @param user
	 */
	public void setActiveUserContact(IContact user);
	
	/**
	 * Get the active {@link IPatient} of the context.
	 * 
	 * @return
	 */
	public Optional<IPatient> getActivePatient();
	
	/**
	 * Set the active {@link IPatient} of the context.
	 * 
	 * @param patient
	 */
	public void setActivePatient(IPatient patient);
	
	/**
	 * Get the active {@link IMandator} of the context.
	 * 
	 * @return
	 */
	public Optional<IMandator> getActiveMandator();
	
	/**
	 * Set the active {@link IMandator} of the context.
	 * 
	 * @param mandator
	 */
	public void setActiveMandator(IMandator mandator);
	
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
