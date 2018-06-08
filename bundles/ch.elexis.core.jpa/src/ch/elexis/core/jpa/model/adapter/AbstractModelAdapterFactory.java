package ch.elexis.core.jpa.model.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.AbstractDBObjectIdDeleted;
import ch.elexis.core.model.Identifiable;

public abstract class AbstractModelAdapterFactory {
	
	public AbstractModelAdapterFactory(){
		initializeMappings();
	}
	
	protected abstract void initializeMappings();
	
	/**
	 * Get the {@link MappingEntry} for the model interface clazz.
	 * 
	 * @param clazz
	 * @return
	 */
	protected abstract MappingEntry getMappingForInterface(Class<?> clazz);
	
	/**
	 * Get the {@link MappingEntry} for the {@link AbstractIdDeleteModelAdapter}
	 * 
	 * @param adapter
	 * @return
	 */
	protected abstract MappingEntry getMappingForAdapter(
		Class<? extends AbstractIdDeleteModelAdapter<?>> adapter);
	
	/**
	 * Get the {@link MappingEntry} for the entity clazz. The interfaceClass parameter is optional,
	 * and can be used if entity maps to many adapters.
	 * 
	 * @param entity
	 * @param interfaceClass
	 * @return
	 */
	protected abstract MappingEntry getMappingEntity(
		Class<? extends AbstractDBObjectIdDeleted> entity, Class<?> interfaceClass);
	
	/**
	 * Get the {@link Constructor} for creating an instance of the
	 * {@link AbstractIdDeleteModelAdapter} with a model object as parameter.
	 * 
	 * @param adapter
	 * @return
	 */
	protected abstract Constructor<?> getAdapterConstructor(
		Class<? extends AbstractIdDeleteModelAdapter<?>> adapter);
	
	/**
	 * Get a {@link Identifiable} model adapter instance. The interfaceClass parameter is optional,
	 * and is used if the entity class maps to many adapter classes.
	 * 
	 * @param entity
	 * @param interfaceClass
	 * @return
	 */
	public Optional<Identifiable> getModelAdapter(AbstractDBObjectIdDeleted entity,
		Class<?> interfaceClass, boolean testPrecondition){
		MappingEntry mapping = getMappingEntity(entity.getClass(), interfaceClass);
		if (mapping != null) {
			Identifiable adapter = getAdapterInstance(mapping.getAdapterClass(), entity);
			if (testPrecondition) {
				if (mapping.testAdapterPrecondition((AbstractIdDeleteModelAdapter<?>) adapter)) {
					return Optional.of(adapter);
				} else {
					LoggerFactory.getLogger(getClass()).error("Adapter precondition failed for ["
						+ adapter + "] with id [" + adapter.getId() + "]");
				}
			} else {
				return Optional.of(adapter);
			}
		}
		return Optional.empty();
	}
	
	/**
	 * Get the {@link AbstractDBObjectIdDeleted} class registered for the
	 * {@link AbstractIdDeleteModelAdapter} class or the model Interface.
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends AbstractDBObjectIdDeleted> getEntityClass(Class<?> clazz){
		// test if the provided clazz is a known interface
		MappingEntry mapping = getMappingForInterface(clazz);
		if (mapping != null) {
			return mapping.getEntityClass();
		}
		// test if the provided clazz is a known adapter class
		mapping = getMappingForAdapter((Class<? extends AbstractIdDeleteModelAdapter<?>>) clazz);
		return mapping != null ? mapping.getEntityClass() : null;
	}
	
	private Identifiable getAdapterInstance(
		Class<? extends AbstractIdDeleteModelAdapter<?>> adapterClass,
		AbstractDBObjectIdDeleted dbObject){
		Constructor<?> constructor = getAdapterConstructor(adapterClass);
		if (constructor != null) {
			try {
				return (Identifiable) constructor.newInstance(dbObject);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new IllegalStateException("Error creating new AbstractModelAdapter instance",
					e);
			}
		} else {
			throw new IllegalStateException("No contructor for " + adapterClass + " found");
		}
	}
	
	/**
	 * Helper method for sub classes to get the {@link Constructor} of a adapter class.
	 * 
	 * @param adapterClass
	 * @param modelClass
	 * @return
	 */
	protected Constructor<?> getAdapterConstructor(
		Class<? extends AbstractIdDeleteModelAdapter<?>> adapterClass,
		Class<? extends AbstractDBObjectIdDeleted> modelClass){
		try {
			return adapterClass.getConstructor(new Class[] {
				modelClass
			});
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException(
				"Could not find AbstractModelAdapter constructor for " + adapterClass, e);
		}
	}
	
	/**
	 * Create a new {@link AbstractModelAdapter} instance of the provided clazz. Also creates a new
	 * transient entity instance.
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T createAdapter(Class<T> clazz){
		try {
			MappingEntry mapping = getMappingForInterface(clazz);
			if (mapping != null) {
				AbstractDBObjectIdDeleted dbObject = mapping.getEntityClass().newInstance();
				Optional<Identifiable> ret = getModelAdapter(dbObject, clazz, false);
				if (ret.isPresent() && clazz.isAssignableFrom(ret.get().getClass())) {
					return (T) ret.get();
				} else {
					throw new IllegalStateException(
						"Created model " + ret.orElse(null) + " is no instance of class " + clazz);
				}
			} else {
				throw new IllegalStateException("No model class found for " + clazz);
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Error creating adapter", e);
		}
	}
}
