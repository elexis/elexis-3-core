package ch.elexis.core.services;

import java.util.List;
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
	
	public final String EANNOTATION_ENTITY_ATTRIBUTE_MAPPING =
		"http://elexis.info/jpa/entity/attribute/mapping";
	
	public final Object EANNOTATION_ENTITY_ATTRIBUTE_MAPPING_NAME = "attributeName";
	
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
	 * entities are included in the result.
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
	 * includeDeleted determines if deleted entities are included in the result.
	 * 
	 * @param clazz
	 * @param includeDeleted
	 * @return
	 */
	public default <T> IQuery<T> getQuery(Class<T> clazz, boolean includeDeleted){
		return getQuery(clazz, false, includeDeleted);
	}
	
	/**
	 * Get a Query for objects of type clazz. If the clazz implements {@link Deleteable}
	 * includeDeleted determines if deleted entities are included in the result. With the
	 * refreshCache parameter updating the cache with the results of the query can be triggered, it
	 * has performance implications.
	 * 
	 * @param clazz
	 * @param refreshCache
	 * @param includeDeleted
	 * @return
	 */
	public <T> IQuery<T> getQuery(Class<T> clazz, boolean refreshCache, boolean includeDeleted);
	
	/**
	 * Get a named query for the clazz with the provided properties. The named query has to be
	 * defined on the entity mapped to the class. The name must match
	 * <i>className.propert[0]property[1]...</i>.
	 * 
	 * @param clazz
	 * @param properties
	 * @return
	 */
	public default <R, T> INamedQuery<R> getNamedQuery(Class<R> clazz, String... properties){
		return getNamedQuery(clazz, false, properties);
	}
	
	/**
	 * Get a named query for the clazz with the provided properties. The named query has to be
	 * defined on the entity mapped to the class. The name must match
	 * <i>className.propert[0]property[1]...</i>. With the refreshCache parameter updating the cache
	 * with the results of the query can be triggered, it has performance implications.
	 * 
	 * @param clazz
	 * @param refreshCache
	 * @param properties
	 * @return
	 */
	public default <R, T> INamedQuery<R> getNamedQuery(Class<R> clazz, boolean refreshCache,
		String... properties){
		return getNamedQuery(clazz, clazz, refreshCache, properties);
	}
	
	/**
	 * 
	 * @param returnValueclazz
	 * @param clazz
	 * @param refreshCache
	 * @param properties
	 * @return
	 */
	public <R, T> INamedQuery<R> getNamedQuery(Class<R> returnValueclazz, Class<T> clazz,
		boolean refreshCache, String... properties);
	
	/**
	 * 
	 * @param returnValueclazz
	 * @param definitionClazz
	 * @param queryName
	 * @return
	 */
	public default <R, T> INamedQuery<R> getNamedQueryByName(Class<R> returnValueclazz,
		Class<T> definitionClazz, String queryName){
		return getNamedQueryByName(returnValueclazz, definitionClazz, false, queryName);
	}
	
	/**
	 * 
	 * @param returnValueclazz
	 * @param definitionClazz
	 * @param refreshCache
	 * @param queryName
	 * @return
	 */
	public <R, T> INamedQuery<R> getNamedQueryByName(Class<R> returnValueclazz,
		Class<T> definitionClazz, boolean refreshCache, String queryName);
	
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
	 * Get a native query object
	 * @param sql
	 * @return
	 */
	public INativeQuery getNativeQuery(String sql);
	
	/**
	 * Execute the native query and return the result list.
	 * 
	 * @param sql
	 * @return
	 */
	public Stream<?> executeNativeQuery(String sql);
	
	/**
	 * Execute the native update and return the number of affected rows.
	 * 
	 * @param sql
	 * @return
	 */
	public int executeNativeUpdate(String sql);
	
	/**
	 * Refresh the entity of the {@link Identifiable} with data from the L2 cache or if not loaded
	 * the database.
	 * 
	 * @param Identifiable
	 */
	public void refresh(Identifiable identifiable);
	
	/**
	 * Get the value from a property of the entity of the {@link Identifiable}. </br>
	 * <b>IMPORTANT</b> this method exists for compatibility with older Elexis code, therefore it
	 * <b>should only be used in such cases</b>.
	 * 
	 * @param propertyName
	 * @param identifiable
	 * @return
	 */
	public Object getEntityProperty(String propertyName, Identifiable identifiable);
	
	/**
	 * Set the value of a property of the entity of the {@link Identifiable}. </br>
	 * <b>IMPORTANT</b> this method exists for compatibility with older Elexis code, therefore it
	 * <b>should only be used in such cases</b>.
	 * 
	 * @param propertyName
	 * @param identifiable
	 * @return
	 */
	public void setEntityProperty(String propertyName, Object value, Identifiable identifiable);
}
