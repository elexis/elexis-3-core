package ch.elexis.core.jpa.model.adapter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import org.slf4j.LoggerFactory;

import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.event.EntityChangeEventListenerHolder;
import ch.elexis.core.model.Identifiable;

public abstract class AbstractModelAdapterFactory {

	protected Map<Class<? extends AbstractIdModelAdapter<?>>, List<MappingEntry>> adapterToEntryMap;
	protected Map<Class<? extends EntityWithId>, List<MappingEntry>> entityToEntryMap;
	protected Map<Class<?>, List<MappingEntry>> interfaceToEntryMap;
	protected Map<Class<? extends AbstractIdModelAdapter<?>>, Constructor<?>> adapterConstructorMap;

	public AbstractModelAdapterFactory() {
		adapterToEntryMap = new HashMap<>();
		entityToEntryMap = new HashMap<>();
		interfaceToEntryMap = new HashMap<>();
		adapterConstructorMap = new HashMap<>();

		initializeMappings();
		initializeAdapterContructors();
	}

	private void initializeAdapterContructors() {
		for (Class<? extends AbstractIdModelAdapter<?>> adapterClass : adapterToEntryMap.keySet()) {
			List<MappingEntry> entries = adapterToEntryMap.get(adapterClass);
			for (MappingEntry interfaceAdapterEntityEntry : entries) {
				adapterConstructorMap.put(adapterClass,
						getAdapterConstructor(adapterClass, interfaceAdapterEntityEntry.getEntityClass()));
			}
		}
	}

	protected abstract void initializeMappings();

	/**
	 * Add the {@link MappingEntry} to the maps.
	 *
	 * @param entry
	 */
	protected void addMapping(MappingEntry entry) {
		List<MappingEntry> list = interfaceToEntryMap.get(entry.getInterfaceClass());
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(entry);
		interfaceToEntryMap.put(entry.getInterfaceClass(), list);

		list = adapterToEntryMap.get(entry.getAdapterClass());
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(entry);
		adapterToEntryMap.put(entry.getAdapterClass(), list);

		list = entityToEntryMap.get(entry.getEntityClass());
		if (list == null) {
			list = new ArrayList<>();
		}
		list.add(entry);
		entityToEntryMap.put(entry.getEntityClass(), list);
	}

	/**
	 * Get the {@link MappingEntry} for the model interface clazz.
	 *
	 * @param clazz
	 * @return
	 */
	protected MappingEntry getMappingForInterface(Class<?> clazz) {
		List<MappingEntry> entryList = interfaceToEntryMap.get(clazz);
		return getSingleEntry(entryList);
	}

	/**
	 * Get the {@link MappingEntry} for the {@link AbstractIdModelAdapter}
	 *
	 * @param adapter
	 * @return
	 */
	protected MappingEntry getMappingForAdapter(Class<? extends AbstractIdModelAdapter<?>> adapter) {
		List<MappingEntry> entryList = adapterToEntryMap.get(adapter);
		return getSingleEntry(entryList);
	}

	/**
	 * Get the {@link MappingEntry} for the entity clazz. The interfaceClass
	 * parameter is optional, and can be used if entity maps to many adapters.
	 *
	 * @param entity
	 * @param interfaceClass
	 * @return
	 */
	protected MappingEntry getMappingEntity(Class<? extends EntityWithId> entity, Class<?> interfaceClass) {
		List<MappingEntry> entryList = entityToEntryMap.get(entity);
		if (interfaceClass != null) {
			return getSingleEntry(entryList, e -> e.getInterfaceClass() == interfaceClass);
		} else {
			return getSingleEntry(entryList);
		}
	}

	/**
	 * Get the {@link Constructor} for creating an instance of the
	 * {@link AbstractIdModelAdapter} with a model object as parameter.
	 *
	 * @param adapter
	 * @return
	 */
	protected Constructor<?> getAdapterConstructor(Class<? extends AbstractIdModelAdapter<?>> adapter) {
		return adapterConstructorMap.get(adapter);
	}

	/**
	 * Get a {@link Identifiable} model adapter instance. The interfaceClass
	 * parameter is optional, and is used if the entity class maps to many adapter
	 * classes.
	 *
	 * @param entity
	 * @param interfaceClass
	 * @return
	 */
	public Optional<Identifiable> getModelAdapter(EntityWithId entity, Class<?> interfaceClass,
			boolean testPrecondition) {
		return getModelAdapter(entity, interfaceClass, testPrecondition, true);
	}

	public Optional<Identifiable> getModelAdapter(EntityWithId entity, Class<?> interfaceClass,
			boolean testPrecondition, boolean registerEntityChangeEvent) {
		if (entity != null) {
			MappingEntry mapping = getMappingEntity(entity.getClass(), interfaceClass);
			if (mapping != null) {
				Identifiable adapter = getAdapterInstance(mapping.getAdapterClass(), entity);
				if (testPrecondition) {
					if (!mapping.testAdapterPrecondition((AbstractIdModelAdapter<?>) adapter)) {
						LoggerFactory.getLogger(getClass()).error(
								"Adapter precondition failed for [" + adapter + "] with id [" + adapter.getId() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						return Optional.empty();
					}
				}
				if (registerEntityChangeEvent && EntityChangeEventListenerHolder.isAvailable()) {
					EntityChangeEventListenerHolder.get().add((AbstractIdModelAdapter<?>) adapter);
				}
				return Optional.of(adapter);
			}
		}
		return Optional.empty();
	}

	/**
	 * Get the {@link AbstractDBObjectId} class registered for the
	 * {@link AbstractIdModelAdapter} class or the model Interface.
	 *
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Class<? extends EntityWithId> getEntityClass(Class<?> clazz) {
		// test if the provided clazz is a known interface
		MappingEntry mapping = getMappingForInterface(clazz);
		if (mapping != null) {
			return mapping.getEntityClass();
		}
		// test if the provided clazz is a known adapter class
		mapping = getMappingForAdapter((Class<? extends AbstractIdModelAdapter<?>>) clazz);
		return mapping != null ? mapping.getEntityClass() : null;
	}

	private Identifiable getAdapterInstance(Class<? extends AbstractIdModelAdapter<?>> adapterClass,
			EntityWithId dbObject) {
		Constructor<?> constructor = getAdapterConstructor(adapterClass);
		if (constructor != null) {
			try {
				return (Identifiable) constructor.newInstance(dbObject);
			} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
					| InvocationTargetException e) {
				throw new IllegalStateException("Error creating new AbstractModelAdapter instance", e); //$NON-NLS-1$
			}
		} else {
			throw new IllegalStateException("No contructor for " + adapterClass + " found"); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/**
	 * Helper method for sub classes to get the {@link Constructor} of a adapter
	 * class.
	 *
	 * @param adapterClass
	 * @param modelClass
	 * @return
	 */
	protected Constructor<?> getAdapterConstructor(Class<? extends AbstractIdModelAdapter<?>> adapterClass,
			Class<? extends EntityWithId> modelClass) {
		try {
			return adapterClass.getConstructor(new Class[] { modelClass });
		} catch (NoSuchMethodException | SecurityException e) {
			throw new IllegalStateException("Could not find AbstractModelAdapter constructor for " + adapterClass, e); //$NON-NLS-1$
		}
	}

	/**
	 * Create a new {@link AbstractModelAdapter} instance of the provided clazz.
	 * Also creates a new transient entity instance.
	 *
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T createAdapter(Class<T> clazz) {
		try {
			MappingEntry mapping = getMappingForInterface(clazz);
			if (mapping != null) {
				EntityWithId dbObject = mapping.getEntityClass().newInstance();
				Optional<Identifiable> ret = getModelAdapter(dbObject, clazz, false);
				if (ret.isPresent() && clazz.isAssignableFrom(ret.get().getClass())) {
					mapping.initialize((AbstractIdModelAdapter<?>) ret.get());
					return (T) ret.get();
				} else {
					throw new IllegalStateException(
							"Created model " + ret.orElse(null) + " is no instance of class " + clazz); //$NON-NLS-1$ //$NON-NLS-2$
				}
			} else {
				throw new IllegalStateException("No model class found for " + clazz); //$NON-NLS-1$
			}
		} catch (InstantiationException | IllegalAccessException e) {
			throw new IllegalStateException("Error creating adapter", e); //$NON-NLS-1$
		}
	}

	private MappingEntry getSingleEntry(List<MappingEntry> entryList) {
		if (entryList != null && !entryList.isEmpty()) {
			return entryList.get(0);
		}
		return null;
	}

	private MappingEntry getSingleEntry(List<MappingEntry> entryList, Predicate<MappingEntry> matcher) {
		if (entryList != null && !entryList.isEmpty()) {
			for (MappingEntry mappingEntry : entryList) {
				if (matcher.test(mappingEntry)) {
					return mappingEntry;
				}
			}
			throw new IllegalStateException(
					"Ambiguous adapter mapping for [" + entryList.get(0).getAdapterClass() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return null;
	}
}
