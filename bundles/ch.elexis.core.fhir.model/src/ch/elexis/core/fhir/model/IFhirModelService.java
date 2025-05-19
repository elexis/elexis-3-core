package ch.elexis.core.fhir.model;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.exceptions.AccessControlException;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.IModelService;

public interface IFhirModelService {

	/**
	 * Get the connection Status to the FHIR Server, or empty if there is no server
	 * available.
	 * 
	 * @return
	 */
	public ConnectionStatus getConnectionStatus();

	/**
	 * Create a new transient model instance of type clazz.
	 *
	 * @param clazz
	 * @return
	 */
	public <T> T create(Class<T> clazz) throws AccessControlException;

	/**
	 * Load a model object of type clazz by the id. Deleted entries are not loaded.
	 *
	 * @param id
	 * @param clazz
	 * @return
	 */
	public default <T> Optional<T> load(String id, Class<T> clazz) {
		return load(id, clazz, false);
	}

	/**
	 * Adapt a loaded FHIR object to clazz. This can be used to "convert" already
	 * loaded FHIR objects to the model. <br />
	 * <br />
	 * <b>This method should only be called by other {@link IModelService}
	 * implementations.</b>
	 *
	 * @param fhirObject
	 * @param clazz
	 * @return
	 */
	public <T> Optional<T> adapt(Object fhirObject, Class<T> clazz);

	/**
	 * Load a model object of type clazz by the id. If Deleted entries should be
	 * loaded can be specified with the includeDeleted parameter.
	 *
	 * @param id
	 * @param clazz
	 * @param includeDeleted
	 * @return
	 */
	public <T> Optional<T> load(String id, Class<T> clazz, boolean includeDeleted) throws AccessControlException;

	/**
	 * Save the model object.
	 *
	 * @param object
	 * @throws IllegalStateException
	 */
	public void save(Identifiable identifiable) throws AccessControlException;

	/**
	 * Save the model objects.
	 *
	 * @param objects
	 * @throws IllegalStateException
	 */
	public void save(List<? extends Identifiable> identifiables) throws AccessControlException;

	/**
	 * Convenience method setting deleted property and save the
	 * {@link Identifiable}.
	 *
	 * @param identifiable
	 */
	public void delete(Identifiable identifiable) throws AccessControlException;

	/**
	 * @see #delete(Deleteable)
	 * @param identifiables
	 */
	public void delete(List<? extends Identifiable> identifiables) throws AccessControlException;

	/**
	 * Post an asynchronous event using the OSGi event admin. The event including
	 * the object is also available to the e4 IEventBroker in the UI.
	 *
	 * @param topic
	 * @param object
	 */
	public void postEvent(String topic, Object object);

	/**
	 * Get a HAPI FHIR {@link IQuery} from the FHIR client of this
	 * {@link IFhirModelService}. For search of the FHIR class matching the provided
	 * model interface.
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public <T> IQuery<IBaseBundle> getQuery(Class<T> clazz);

	/**
	 * Execute the HAPI FHIR {@link IQuery} and adapt the resulting
	 * {@link Resource}s to the provided core model interface.
	 * 
	 * @param <T>
	 * @param query
	 * @param clazz
	 * @return
	 */
	public <T> List<T> getQueryResults(IQuery<IBaseBundle> query, Class<T> clazz);

	/**
	 * Get a HAPI FHIR {@link IQuery} from the FHIR client of this
	 * {@link IFhirModelService} for search by the provided url.
	 * 
	 * @param byUrl
	 * @return
	 */
	public IQuery<IBaseBundle> getQuery(String byUrl);
}
