package ch.elexis.core.jpa.model.adapter;

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

import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.jpa.entities.EntityWithDeleted;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;

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
		//		EntityWithId dbObject = em.find(dbObjectClass, id);
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
				}
				return true;
			} finally {
				closeEntityManager(em);
			}
		}
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
				em.getTransaction().begin();
				for (Identifiable identifiable : dbObjects.keySet()) {
					EntityWithId dbObject = dbObjects.get(identifiable);
					if (dbObject != null) {
						boolean newlyCreatedObject = (dbObject.getLastupdate() == null);
						em.merge(dbObject);
						setDbObject(identifiable, dbObject);
						if (newlyCreatedObject) {
							createdEvents.add(getCreateEvent(identifiable));
						}
					}
				}
				em.getTransaction().commit();
				createdEvents.stream().forEach(e -> postElexisEvent(e));
				return true;
			} finally {
				closeEntityManager(em);
			}
		}
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
				return true;
			} finally {
				closeEntityManager(em);
			}
		}
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
	
	protected Optional<EntityWithId> getDbObject(Identifiable identifiable){
		if (identifiable instanceof AbstractIdModelAdapter<?>) {
			return Optional.ofNullable(((AbstractIdModelAdapter<?>) identifiable).getEntity());
		}
		return Optional.empty();
	}
	
	protected void setDbObject(Identifiable identifiable, EntityWithId entity){
		if (identifiable instanceof AbstractIdModelAdapter<?>) {
			((AbstractIdModelAdapter<?>) identifiable).setEntity(entity);
		}
	}
	
	@Override
	public void delete(Deleteable deletable){
		deletable.setDeleted(true);
		save((Identifiable) deletable);
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
	
	@Override
	public Map<String, Object> getParameterMap(Object... parameters){
		HashMap<String, Object> ret = new HashMap<>();
		for (int i = 0; i < parameters.length; i += 2) {
			ret.put((String) parameters[i], parameters[i + 1]);
		}
		return ret;
	}
}
