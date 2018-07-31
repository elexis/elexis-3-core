package ch.elexis.core.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;

/**
 * Service interface for accessing the data model. Implementations should provide
 * {@link IModelService#SERVICEMODELNAME} as service property. Using the property clients can get
 * service for a specific model.
 * 
 * @author thomas
 *
 */
public interface IModelService {
	
	public final String SERVICEMODELNAME = "service.model.name";
	
	public final String DOUBLECOLON = "::";
	
	public final String EANNOTATION_ENTITY_ATTRIBUTE_MAPPING =
		"http://elexis.info/jpa/entity/attribute/mapping";
	
	public final Object EANNOTATION_ENTITY_ATTRIBUTE_MAPPING_NAME = "attributeName";
	
	/**
	 * Get a reference string that can be used to load the {@link Identifiable} using
	 * {@link #loadFromString(String)}.
	 * 
	 * @param identifiable
	 * @return
	 */
	public Optional<String> storeToString(Identifiable identifiable);
	
	/**
	 * Load an {@link Identifiable} using a string created using
	 * {@link #storeToString(Identifiable)}.
	 * 
	 * @param string
	 * @return
	 */
	public Optional<Identifiable> loadFromString(String storeToString);
	
	/**
	 * Split a storeToString into an array containing the type and the id. Expected separator string
	 * is {@link #DOUBLECOLON}.
	 * 
	 * @param storeToString
	 * @return a size 2 array with type [0] and id [1] or <code>null</code> in either [0] or [1]
	 */
	public default String[] splitIntoTypeAndId(String storeToString){
		String[] split = storeToString.split(DOUBLECOLON);
		return Arrays.copyOf(split, 2);
	}
	
	/**
	 * Create a new transient model instance of type clazz.
	 * 
	 * @param clazz
	 * @return
	 */
	public <T> T create(Class<T> clazz);
	
	/**
	 * Load a model object of type clazz by the id. Deleted entries are not loaded.
	 * 
	 * @param id
	 * @param clazz
	 * @return
	 */
	public default <T> Optional<T> load(String id, Class<T> clazz){
		return load(id, clazz, false);
	}
	
	/**
	 * Load a model object of type clazz by the id. If Deleted entries should be loaded can be
	 * specified with the includeDeleted parameter.
	 * 
	 * @param id
	 * @param clazz
	 * @param includeDeleted
	 * @return
	 */
	public <T> Optional<T> load(String id, Class<T> clazz, boolean includeDeleted);
	
	/**
	 * Save the model object.
	 * 
	 * @param object
	 * @return
	 */
	public boolean save(Identifiable identifiable);
	
	/**
	 * Save the model objects.
	 * 
	 * @param objects
	 * @return
	 */
	public boolean save(List<Identifiable> identifiables);
	
	/**
	 * Remove the {@link Identifiable} from the database.
	 * 
	 * @param identifiable
	 * @return
	 */
	public boolean remove(Identifiable identifiable);
	
	/**
	 * Get a Query for objects of type clazz. If the clazz implements {@link Deleteable} no deleted
	 * entities are included in the result. The Query is closed after the {@link IQuery#execute()}
	 * method is called.
	 * 
	 * @param clazz
	 * @param context
	 * @return
	 */
	public default <T> IQuery<T> getQuery(Class<T> clazz){
		return getQuery(clazz, false);
	}
	
	/**
	 * Get a Query for objects of type clazz. If the clazz implements {@link Deleteable}
	 * includeDeleted determines if deleted entities are included in the result. The Query is closed
	 * after the {@link IQuery#execute()} method is called.
	 * 
	 * @param clazz
	 * @param includeDeleted
	 * @return
	 */
	public <T> IQuery<T> getQuery(Class<T> clazz, boolean includeDeleted);
	
	/**
	 * Get a named query for the clazz with the provided properties. The named query has to be
	 * defined on the entity mapped to the class. The name must match
	 * <i>className.propert[0]property[1]...</i>.
	 * 
	 * @param clazz
	 * @param properties
	 * @return
	 */
	public <T> INamedQuery<T> getNamedQuery(Class<T> clazz, String... properties);
	
	/**
	 * Convenience method setting deleted property and save the {@link Deleteable}.
	 * 
	 * @param deletable
	 */
	public void delete(Deleteable deletable);
	
	/**
	 * Post an asynchronous event using the OSGi event admin. The event including the object is also
	 * available to the e4 IEventBroker in the UI.
	 * 
	 * @param topic
	 * @param object
	 */
	public void postEvent(String topic, Object object);
	
	/**
	 * Execute the native query and return the result list.
	 * 
	 * @param sql
	 * @return
	 */
	public Stream<?> executeNativeQuery(String sql);
	
	/**
	 * Helper Method for creating a map to execute a {@link INamedQuery}.
	 * 
	 * @param parameters
	 * @return
	 */
	public Map<String, Object> getParameterMap(Object... parameters);
}
