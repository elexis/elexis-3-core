package ch.elexis.core.services;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.exceptions.AccessControlException;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;

/**
 * Service interface for accessing the data model. Implementations should
 * provide {@link IModelService#SERVICEMODELNAME} as service property. Using the
 * property clients can get service for a specific model.
 *
 * @author thomas
 *
 */
public interface IModelService {

	public final String SERVICEMODELNAME = "service.model.name";

	public final String EANNOTATION_ENTITY_ATTRIBUTE_MAPPING = "http://elexis.info/jpa/entity/attribute/mapping";

	public final Object EANNOTATION_ENTITY_ATTRIBUTE_MAPPING_NAME = "attributeName";

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
	 * Try to cast the {@link Identifiable} to the given class. Use this with
	 * caution, this method should be only used for very limited edge-cases.
	 * 
	 * @param <T>
	 * @param identifiable
	 * @param clazz
	 * @return
	 * @since 3.13
	 */
	public <T> Optional<T> cast(Identifiable identifiable, Class<T> clazz);

	/**
	 * Load all model objects of type clazz. Deleted entries are not loaded.
	 *
	 * @param clazz
	 * @return
	 */
	public <T> List<T> findAll(Class<T> clazz)  throws AccessControlException;

	/**
	 * Load all model objects of type clazz by a set of ids. Deleted entries are not
	 * loaded.
	 *
	 * @param clazz
	 * @return
	 */
	public <T> List<T> findAllById(Collection<String> ids, Class<T> clazz)  throws AccessControlException;

	/**
	 * Adapt a jpa loaded entity to clazz. This can be used to "convert" already
	 * loaded JPA entities to the model. <br />
	 * <br />
	 * <b>This method should only be called by other {@link IModelService}
	 * implementations.</b>
	 *
	 * @param jpaEntity
	 * @param clazz
	 * @return
	 */
	public <T> Optional<T> adapt(Object jpaEntity, Class<T> clazz);

	/**
	 * Get the JPA entity class for a model class. An {@link IModelService} can do
	 * this for all classes of its model, but not for classes of other
	 * {@link IModelService} instances. <br />
	 * <br />
	 * <b>This method should only be called by {@link IQuery} instances. For example
	 * when creating a {@link ISubQuery} to get the entity class for the model class
	 * of the {@link ISubQuery}, as the sub query can be on a separate entity.</b>
	 *
	 * @param clazz
	 * @return
	 */
	public Class<?> getEntityClass(Class<?> clazz);

	/**
	 * Load a model object of type clazz by the id. If Deleted entries should be
	 * loaded can be specified with the includeDeleted parameter.
	 *
	 * @param id
	 * @param clazz
	 * @param includeDeleted
	 * @return
	 */
	public default <T> Optional<T> load(String id, Class<T> clazz, boolean includeDeleted) throws AccessControlException {
		return load(id, clazz, includeDeleted, true);
	}

	/**
	 * Load a model object of type clazz by the id. If Deleted entries should be
	 * loaded can be specified with the includeDeleted parameter. If the entity
	 * should be refreshed from the db can be specified with the refreshCache
	 * parameter.
	 *
	 * @param <T>
	 * @param id
	 * @param clazz
	 * @param includeDeleted
	 * @param refreshCache
	 * @return
	 */
	public <T> Optional<T> load(String id, Class<T> clazz, boolean includeDeleted, boolean refreshCache)  throws AccessControlException;

	/**
	 * Save the model object.
	 *
	 * @param object
	 * @throws IllegalStateException
	 */
	public void save(Identifiable identifiable) throws AccessControlException;

	/**
	 * Update {@link Identifiable#getLastupdate()} to current
	 * 
	 * @param identifiable
	 */
	public void touch(Identifiable identifiable);

	/**
	 * Save the model objects.
	 *
	 * @param objects
	 * @throws IllegalStateException
	 */
	public void save(List<? extends Identifiable> identifiables) throws AccessControlException;

	/**
	 * Remove the {@link Identifiable} from the database.
	 *
	 * @param identifiable
	 */
	public void remove(Identifiable identifiable) throws AccessControlException;

	/**
	 * Remove a list of {@link Identifiable} from the database in a single
	 * transaction.
	 *
	 * @param identifiables
	 */
	public void remove(List<? extends Identifiable> identifiables) throws AccessControlException;

	/**
	 * Get a Query for objects of type clazz. If the clazz implements
	 * {@link Deleteable} no deleted entities are included in the result.
	 *
	 * @param clazz
	 * @param context
	 * @return
	 */
	public default <T> IQuery<T> getQuery(Class<T> clazz) {
		return getQuery(clazz, false);
	}

	/**
	 * Get a Query for objects of type clazz. If the clazz implements
	 * {@link Deleteable} includeDeleted determines if deleted entities are included
	 * in the result.
	 *
	 * @param clazz
	 * @param includeDeleted
	 * @return
	 */
	public default <T> IQuery<T> getQuery(Class<T> clazz, boolean includeDeleted) {
		return getQuery(clazz, false, includeDeleted);
	}

	/**
	 * Get a Query for objects of type clazz. If the clazz implements
	 * {@link Deleteable} includeDeleted determines if deleted entities are included
	 * in the result. With the refreshCache parameter updating the cache with the
	 * results of the query can be triggered, it has performance implications.
	 *
	 * @param clazz
	 * @param refreshCache
	 * @param includeDeleted
	 * @return
	 */
	public <T> IQuery<T> getQuery(Class<T> clazz, boolean refreshCache, boolean includeDeleted);

	/**
	 * Get a named query for the clazz with the provided properties. The named query
	 * has to be defined on the entity mapped to the class. The name must match
	 * <i>className.propert[0]property[1]...</i>.
	 *
	 * @param clazz
	 * @param properties
	 * @return
	 */
	public default <R, T> INamedQuery<R> getNamedQuery(Class<R> clazz, String... properties) {
		return getNamedQuery(clazz, false, properties);
	}

	/**
	 * Get a named query for the clazz with the provided properties. The named query
	 * has to be defined on the entity mapped to the class. The name must match
	 * <i>className.property[0]property[1]...</i>. With the refreshCache parameter
	 * updating the cache with the results of the query can be triggered, it has
	 * performance implications.
	 *
	 * @param clazz
	 * @param refreshCache
	 * @param properties
	 * @return
	 */
	public default <R, T> INamedQuery<R> getNamedQuery(Class<R> clazz, boolean refreshCache, String... properties) {
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
	public <R, T> INamedQuery<R> getNamedQuery(Class<R> returnValueclazz, Class<T> clazz, boolean refreshCache,
			String... properties);

	/**
	 *
	 * @param returnValueclazz
	 * @param definitionClazz
	 * @param queryName
	 * @return
	 */
	public default <R, T> INamedQuery<R> getNamedQueryByName(Class<R> returnValueclazz, Class<T> definitionClazz,
			String queryName) {
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
	public <R, T> INamedQuery<R> getNamedQueryByName(Class<R> returnValueclazz, Class<T> definitionClazz,
			boolean refreshCache, String queryName);

	/**
	 * Convenience method setting deleted property and save the {@link Deleteable}.
	 *
	 * @param deletable
	 */
	public void delete(Deleteable deletable) throws AccessControlException;

	/**
	 * @see #delete(Deleteable)
	 * @param deletables
	 */
	public void delete(List<? extends Deleteable> deletables) throws AccessControlException;

	/**
	 * Post an asynchronous event using the OSGi event admin. The event including
	 * the object is also available to the e4 IEventBroker in the UI.
	 *
	 * @param topic
	 * @param object
	 */
	public void postEvent(String topic, Object object);

	/**
	 * Get a native query object
	 *
	 * @param sql
	 * @return
	 */
	public INativeQuery getNativeQuery(String sql);

	/**
	 * Execute the native query and return the result stream.
	 *
	 * @param sql
	 * @return
	 */
	public Stream<?> executeNativeQuery(String sql);

	/**
	 * Execute the native query and return the result stream of type T.
	 *
	 * @param sql
	 * @return
	 */
	public <T> Stream<T> executeNativeQuery(String sql, Class<T> interfaceClazz);

	/**
	 * Execute the native update and return the number of affected rows. After the
	 * update is execute the cache of this {@link IModelService} is invalidated.
	 * <b>Performance implications</b>.
	 *
	 * @param sql
	 * @return
	 */
	default public int executeNativeUpdate(String sql) {
		return executeNativeUpdate(sql, true);
	}

	/**
	 * Execute the native update and return the number of affected rows. Via the
	 * invalidateCache parameter the clearing the cache of this
	 * {@link IModelService} can be skipped. <b>Performance implications if cache is
	 * cleared</b>.
	 *
	 * @param sql
	 * @param invalidateCache
	 * @return
	 */
	public int executeNativeUpdate(String sql, boolean invalidateCache);

	/**
	 * Refresh the entity of the {@link Identifiable} with data from the L2 cache or
	 * if not loaded the database. <b>If the {@link Identifiable} has unsaved
	 * changes, this will have no effect.</b> Reload the {@link Identifiable} to
	 * reset changes.
	 *
	 * @param Identifiable
	 */
	default public void refresh(Identifiable identifiable) {
		refresh(identifiable, false);
	}

	/**
	 * Refresh the entity of the {@link Identifiable} with data from the L2 cache or
	 * if not loaded the database. With the refreshCache parameter the data is
	 * always loaded from the database and the L2 cache is refreshed. <b>If the
	 * {@link Identifiable} has unsaved changes, this will have no effect.</b>
	 * Reload the {@link Identifiable} to reset changes.
	 *
	 * @param Identifiable
	 * @param refreshCache
	 */
	public void refresh(Identifiable identifiable, boolean refreshCache);

	/**
	 * Get the value from a property of the entity of the {@link Identifiable}.
	 * </br>
	 * <b>IMPORTANT</b> this method exists for compatibility with older Elexis code,
	 * therefore it <b>should only be used in such cases</b>.
	 *
	 * @param propertyName
	 * @param identifiable
	 * @return
	 */
	public Object getEntityProperty(String propertyName, Identifiable identifiable);

	/**
	 * Set the value of a property of the entity of the {@link Identifiable}. </br>
	 * <b>IMPORTANT</b> this method exists for compatibility with older Elexis code,
	 * therefore it <b>should only be used in such cases</b>.
	 *
	 * @param propertyName
	 * @param identifiable
	 * @return
	 */
	public void setEntityProperty(String propertyName, Object value, Identifiable identifiable);

	/**
	 * Add the Entity of {@link Identifiable} value to the list identified by
	 * listName of the Entity of the {@link Identifiable}. <b>IMPORTANT</b> this
	 * method exists for compatibility with older Elexis code, therefore it
	 * <b>should only be used in such cases</b>.
	 *
	 * @param listName
	 * @param value
	 * @param identifiable
	 */
	public void addToEntityList(String listName, Identifiable value, Identifiable identifiable);

	/**
	 * Clear the whole cache.
	 *
	 */
	public void clearCache();

	/**
	 * Get the highest last update from the table of type clazz. This always results
	 * in a db statement.
	 *
	 * @param <T>
	 * @param clazz
	 * @return
	 */
	public <T> long getHighestLastUpdate(Class<T> clazz);

	/**
	 * Set a list of {@link ElexisEventTopics} to be blocked. These events will not
	 * be posted or sent, until method is called with null as block event topics.
	 * This can be used for importers where events lead to unwanted side effects.
	 * <b>USE WITH CARE</b>
	 *
	 * @param blockEventTypes
	 */
	public void setBlockEventTopics(List<String> blockEventTopics);
}
