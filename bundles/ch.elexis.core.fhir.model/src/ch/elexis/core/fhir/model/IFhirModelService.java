package ch.elexis.core.fhir.model;

import java.util.List;
import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Resource;

import ca.uhn.fhir.rest.gclient.IQuery;
import ch.elexis.core.services.ICompositeModelService;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.services.IModelService;

public interface IFhirModelService extends ICompositeModelService {

	/**
	 * Get the connection Status to the FHIR Server, or empty if there is no server
	 * available.
	 * 
	 * @return
	 */
	public ConnectionStatus getConnectionStatus();

	/**
	 * Directly load the fhir resource
	 * 
	 * @param <U>
	 * @param id
	 * @param fhirClazz
	 * @return
	 */
	public <U> Optional<U> loadFhir(String id, Class<U> fhirClazz);

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
	 * Post an asynchronous event using the OSGi event admin. The event including
	 * the object is also available to the e4 IEventBroker in the UI.
	 *
	 * @param topic
	 * @param object
	 */
	public void postEvent(String topic, Object object);

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
	 * Return a FHIR based query
	 * 
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	<T> IQuery<IBaseBundle> getFhirQuery(Class<T> clazz);

	/**
	 * Get a HAPI FHIR {@link IQuery} from the FHIR client of this
	 * {@link IFhirModelService} for search by the provided url.
	 * 
	 * 
	 * @param byUrl
	 * @return
	 */
	public IQuery<IBaseBundle> getFhirQuery(String byUrl);
}
