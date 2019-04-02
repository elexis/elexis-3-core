package ch.elexis.core.jpa.model.adapter;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.beanutils.BeanUtils;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.jpa.entities.EntityWithDeleted;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.INativeQuery;

public abstract class AbstractModelService implements IModelService {
	
	protected AbstractModelAdapterFactory adapterFactory;
	
	protected abstract EntityManager getEntityManager(boolean managed);
	
	protected abstract void closeEntityManager(EntityManager entityManager);
	
	protected abstract EventAdmin getEventAdmin();
	
	protected ExecutorService executor = Executors.newCachedThreadPool();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> load(String id, Class<T> clazz, boolean includeDeleted){
		EntityManager em = getEntityManager(true);
		Class<? extends EntityWithId> dbObjectClass = adapterFactory.getEntityClass(clazz);
		HashMap<String, Object> queryHints = new HashMap<>();
		queryHints.put(QueryHints.REFRESH, HintValues.TRUE);
		EntityWithId dbObject = em.find(dbObjectClass, id, queryHints);
		if (dbObject != null) {
			// check for deleted
			if (!includeDeleted && (dbObject instanceof EntityWithDeleted)) {
				if (((EntityWithDeleted) dbObject).isDeleted()) {
					return Optional.empty();
				}
			}
			Optional<Identifiable> modelObject =
				adapterFactory.getModelAdapter(dbObject, clazz, true);
			if (modelObject.isPresent() && clazz.isAssignableFrom(modelObject.get().getClass())) {
				return (Optional<T>) modelObject;
			}
		}
		return Optional.empty();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> adapt(Object jpaEntity, Class<T> clazz){
		if (jpaEntity instanceof EntityWithId) {
			return (Optional<T>) adapterFactory.getModelAdapter((EntityWithId) jpaEntity, clazz,
				false);
		}
		return Optional.empty();
	}
	
	@Override
	public void refresh(Identifiable identifiable){
		EntityManager em = getEntityManager(true);
		EntityWithId dbObject = getDbObject(identifiable).orElse(null);
		HashMap<String, Object> queryHints = new HashMap<>();
		// TODO check why references are not refreshed from L2 cache without this flag
		queryHints.put(QueryHints.REFRESH, HintValues.TRUE);
		EntityWithId reloadedDbObject = em.find(dbObject.getClass(), dbObject.getId(), queryHints);
		if (reloadedDbObject != null) {
			setDbObject(identifiable, reloadedDbObject);
		}
	}
	
	@Override
	public Object getEntityProperty(String propertyName, Identifiable identifiable){
		EntityWithId dbObject = getDbObject(identifiable).orElse(null);
		if (dbObject != null) {
			try {
				return BeanUtils.getProperty(dbObject, propertyName);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				LoggerFactory.getLogger(getClass()).error(
					"Could not get property [" + propertyName + "] of entity [" + dbObject + "]",
					e);
			}
		}
		return null;
	}
	
	@Override
	public void setEntityProperty(String propertyName, Object value, Identifiable identifiable){
		EntityWithId dbObject = getDbObject(identifiable).orElse(null);
		if (dbObject != null) {
			try {
				BeanUtils.setProperty(dbObject, propertyName, value);
			} catch (IllegalAccessException | InvocationTargetException e) {
				LoggerFactory.getLogger(getClass()).error(
					"Could not set property [" + propertyName + "] of entity [" + dbObject + "]",
					e);
			}
		}
	}
	
	@Override
	public boolean save(Identifiable identifiable){
		Optional<EntityWithId> dbObject = getDbObject(identifiable);
		if (dbObject.isPresent()) {
			boolean newlyCreatedObject = (dbObject.get().getLastupdate() == null);
			EntityManager em = getEntityManager(false);
			try {
				em.getTransaction().begin();
				EntityWithId merged = em.merge(dbObject.get());
				setDbObject(identifiable, merged);
				em.getTransaction().commit();
				if (newlyCreatedObject) {
					postElexisEvent(getCreateEvent(identifiable));
					postEvent(ElexisEventTopics.EVENT_CREATE, identifiable);
				}
				return true;
			} finally {
				closeEntityManager(em);
			}
		}
		LoggerFactory.getLogger(getClass()).error("Could not save [{}]", identifiable);
		return false;
	}
	
	@Override
	public boolean save(List<Identifiable> identifiables){
		Map<Identifiable, EntityWithId> dbObjects = identifiables.parallelStream()
			.collect(Collectors.toMap(Function.identity(), i -> getDbObject(i).orElse(null)));
		if (!dbObjects.isEmpty()) {
			EntityManager em = getEntityManager(false);
			try {
				List<ElexisEvent> createdEvents = new ArrayList<>();
				List<Identifiable> createdIdentifiables = new ArrayList<>();
				em.getTransaction().begin();
				for (Identifiable identifiable : dbObjects.keySet()) {
					EntityWithId dbObject = dbObjects.get(identifiable);
					if (dbObject != null) {
						boolean newlyCreatedObject = (dbObject.getLastupdate() == null);
						EntityWithId merged = em.merge(dbObject);
						setDbObject(identifiable, merged);
						if (newlyCreatedObject) {
							createdEvents.add(getCreateEvent(identifiable));
							createdIdentifiables.add(identifiable);
						}
					}
				}
				em.getTransaction().commit();
				createdEvents.stream().forEach(e -> postElexisEvent(e));
				createdIdentifiables.stream()
					.forEach(i -> postEvent(ElexisEventTopics.EVENT_CREATE, i));
				return true;
			} finally {
				closeEntityManager(em);
			}
		}
		LoggerFactory.getLogger(getClass()).error("Could not save list [{}]", identifiables);
		return false;
	}
	
	@Override
	public boolean remove(Identifiable identifiable){
		Optional<EntityWithId> dbObject = getDbObject(identifiable);
		if (dbObject.isPresent()) {
			EntityManager em = getEntityManager(false);
			try {
				em.getTransaction().begin();
				EntityWithId object = em.merge(dbObject.get());
				em.remove(object);
				em.getTransaction().commit();
				postEvent(ElexisEventTopics.EVENT_DELETE, identifiable);
				return true;
			} finally {
				closeEntityManager(em);
			}
		}
		LoggerFactory.getLogger(getClass()).error("Could not remove [{}]", identifiable);
		return false;
	}
	
	/**
	 * Get an {@link ElexisEvent} representation of {@link Identifiable} creation. Called by the
	 * save methods, to send creation events. As the creation event currently uses storeToString,
	 * this method has to be implemented in sub classes.
	 * 
	 * @param identifiable
	 * @return
	 */
	protected abstract ElexisEvent getCreateEvent(Identifiable identifiable);
	
	public void postElexisEvent(ElexisEvent elexisEvent){
		if (elexisEvent == null || elexisEvent.getTopic() == null) {
			return;
		}
		String topic = elexisEvent.getTopic();
		if (!topic.startsWith(ElexisEventTopics.BASE)) {
			topic = ElexisEventTopics.BASE + topic;
		}
		Event event = new Event(topic, elexisEvent.getProperties());
		if (getEventAdmin() != null) {
			getEventAdmin().sendEvent(event);
		} else {
			throw new IllegalStateException("No EventAdmin available");
		}
	}
	
	protected Optional<EntityWithId> getDbObject(Object adapter){
		if (adapter instanceof AbstractIdModelAdapter<?>) {
			return Optional.ofNullable(((AbstractIdModelAdapter<?>) adapter).getEntity());
		}
		return Optional.empty();
	}
	
	protected void setDbObject(Object adapter, EntityWithId merged){
		if (adapter instanceof AbstractIdModelAdapter<?>) {
			((AbstractIdModelAdapter<?>) adapter).setEntity(merged);
		}
		sendEntityChangeEvent(merged);
	}
	
	private void sendEntityChangeEvent(EntityWithId merged){
		if (getEventAdmin() != null) {
			Map<String, Object> properites = new HashMap<>();
			properites.put(EntityWithId.class.getName(), merged);
			Event event = new Event(ElexisEventTopics.PERSISTENCE_EVENT_ENTITYCHANGED, properites);
			getEventAdmin().sendEvent(event);
		} else {
			throw new IllegalStateException("No EventAdmin available");
		}
	}
	
	@Override
	public void delete(Deleteable deletable){
		deletable.setDeleted(true);
		save((Identifiable) deletable);
		postEvent(ElexisEventTopics.EVENT_DELETE, deletable);
	}
	
	@Override
	public void postEvent(String topic, Object object){
		if (getEventAdmin() != null) {
			Map<String, Object> properites = new HashMap<>();
			properites.put("org.eclipse.e4.data", object);
			Event event = new Event(topic, properites);
			getEventAdmin().postEvent(event);
		} else {
			throw new IllegalStateException("No EventAdmin available");
		}
	}
	
	@Override
	public <T> T create(Class<T> clazz){
		return adapterFactory.createAdapter(clazz);
	}
	
	@Override
	public Stream<?> executeNativeQuery(String sql){
		Query query = getEntityManager(true).createNativeQuery(sql);
		return query.getResultStream();
	}
	
	@Override
	public int executeNativeUpdate(String sql){
		EntityManager em = getEntityManager(false);
		try {
			em.getTransaction().begin();
			int affected = em.createNativeQuery(sql).executeUpdate();
			em.getTransaction().commit();
			return affected;
		} finally {
			closeEntityManager(em);
			clearCache();
		}
	}
	
	protected String getNamedQueryName(Class<?> clazz, String... properties){
		Class<? extends EntityWithId> entityClazz = adapterFactory.getEntityClass(clazz);
		StringJoiner queryName = new StringJoiner(".");
		queryName.add(entityClazz.getSimpleName());
		for (String string : properties) {
			queryName.add(string);
		}
		return queryName.toString();
	}
	
	@Override
	public INativeQuery getNativeQuery(String sql){
		Query query = getEntityManager(true).createNativeQuery(sql);
		return new NativeQuery(query);
	}
	
	@Override
	public <R, T> INamedQuery<R> getNamedQuery(Class<R> returnValueclazz, Class<T> definitionClazz,
		boolean refreshCache, String... properties){
		return new NamedQuery<>(returnValueclazz, definitionClazz, refreshCache, adapterFactory,
			(EntityManager) getEntityManager(true), getNamedQueryName(definitionClazz, properties));
	}
	
	@Override
	public <R, T> INamedQuery<R> getNamedQueryByName(Class<R> returnValueclazz,
		Class<T> definitionClazz, boolean refreshCache, String queryName){
		return new NamedQuery<>(returnValueclazz, definitionClazz, refreshCache, adapterFactory,
			(EntityManager) getEntityManager(true), queryName);
	}
}
