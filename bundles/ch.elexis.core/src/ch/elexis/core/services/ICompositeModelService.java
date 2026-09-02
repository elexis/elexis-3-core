package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.exceptions.AccessControlException;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;

/**
 * Parts of the model service that are supported both in the JPA backed
 * implementation, and in the FHIR backed implementation.
 * 
 * @since 3.14
 */
public interface ICompositeModelService {

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

}
