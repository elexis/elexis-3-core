package ch.elexis.core.services;

import java.util.Optional;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;

public interface IContextService {
	
	/**
	 * Get the root context of the service.
	 * 
	 * @return
	 */
	public IContext getRootContext();
	
	/**
	 * Get the active {@link IUser} from the root context.
	 * 
	 * @return
	 */
	default public Optional<IUser> getActiveUser(){
		return getRootContext().getTyped(IUser.class);
	}
	
	/**
	 * Set the active {@link IUser} of the root context.
	 * 
	 * @param user
	 *            or <code>null</code> to unset
	 */
	default public void setActiveUser(IUser user){
		if (user != null) {
			getRootContext().setTyped(user);
		} else {
			getRootContext().removeTyped(IUser.class);
		}
	}
	
	/**
	 * Get the active {@link IContact} of the {@link IUser} from the root context.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	default public Optional<IContact> getActiveUserContact(){
		return (Optional<IContact>) getRootContext().getNamed(IContext.ACTIVE_USERCONTACT);
	}
	
	/**
	 * Get the active {@link IPatient} of the root context.
	 * 
	 * @return
	 */
	default public Optional<IPatient> getActivePatient(){
		return getRootContext().getTyped(IPatient.class);
	}
	
	/**
	 * Set the active {@link IPatient} of the root context.
	 * 
	 * @param patient
	 *            or <code>null</code> to unset
	 */
	default public void setActivePatient(IPatient patient){
		if (patient != null) {
			getRootContext().setTyped(patient);
		} else {
			getRootContext().removeTyped(IPatient.class);
		}
	}
	
	/**
	 * Get the active {@link IMandator} of the root context.
	 * 
	 * @return
	 */
	default public Optional<IMandator> getActiveMandator(){
		return getRootContext().getTyped(IMandator.class);
	}
	
	/**
	 * Set the active {@link IMandator} of the root context.
	 * 
	 * @param mandator
	 *            or <code>null</code> to unset
	 */
	default public void setActiveMandator(IMandator mandator){
		if (mandator != null) {
			getRootContext().setTyped(mandator);
		} else {
			getRootContext().removeTyped(IMandator.class);
		}
	}
	
	/**
	 * Get the active {@link ICoverage} of the root context.
	 * 
	 * @return
	 */
	default public Optional<ICoverage> getActiveCoverage(){
		return getRootContext().getTyped(ICoverage.class);
	}
	
	/**
	 * Set the active {@link ICoverage} of the root context.
	 * 
	 * @param coverage
	 *            or <code>null</code> to unset
	 */
	default public void setActiveCoverage(ICoverage coverage){
		if (coverage != null) {
			getRootContext().setTyped(coverage);
		} else {
			getRootContext().removeTyped(ICoverage.class);
		}
	}
	
	/**
	 * Get the station identifier.
	 * 
	 * @return
	 * @see IContext#getStationIdentifier()
	 */
	default public String getStationIdentifier(){
		return getRootContext().getStationIdentifier();
	}
	
	/**
	 * Get an {@link Object} identified by its type from the root context.
	 * 
	 * @param clazz
	 * @return
	 */
	default public <T> Optional<T> getTyped(Class<T> clazz){
		return getRootContext().getTyped(clazz);
	}
	
	/**
	 * Get an {@link Object} identified by the name from the root context.
	 * 
	 * @param name
	 * @return
	 */
	default public Optional<?> getNamed(String name){
		return getRootContext().getNamed(name);
	}
	
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
	
	/**
	 * Send an Event using a topic (see {@link ElexisEventTopics}), and an optional object.
	 * 
	 * @param eventTopic
	 * @param object
	 */
	public void sendEvent(String eventTopic, Object object);
}
