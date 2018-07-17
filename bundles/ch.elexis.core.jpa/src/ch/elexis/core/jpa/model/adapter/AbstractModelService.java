package ch.elexis.core.jpa.model.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.jpa.entities.AbstractDBObjectId;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;

public abstract class AbstractModelService implements IModelService {
	
	protected AbstractModelAdapterFactory adapterFactory;
	
	protected abstract EntityManager getEntityManager();
	
	protected abstract EventAdmin getEventAdmin();
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> load(String id, Class<T> clazz){
		EntityManager em = getEntityManager();
		try {
			Class<? extends AbstractDBObjectId> dbObjectClass =
				adapterFactory.getEntityClass(clazz);
			AbstractDBObjectId dbObject = em.find(dbObjectClass, id);
			if (dbObject != null) {
				Optional<Identifiable> modelObject =
					adapterFactory.getModelAdapter(dbObject, clazz, true);
				if (modelObject.isPresent()
					&& clazz.isAssignableFrom(modelObject.get().getClass())) {
					return (Optional<T>) modelObject;
				}
			}
		} finally {
			em.close();
		}
		return Optional.empty();
	}
	
	@Override
	public boolean save(Identifiable identifiable){
		Optional<AbstractDBObjectId> dbObject = getDbObject(identifiable);
		if (dbObject.isPresent()) {
			EntityManager em = getEntityManager();
			try {
				em.getTransaction().begin();
				boolean newlyCreatedObject = (dbObject.get().getLastupdate() == null);
				setDbObject(identifiable, em.merge(dbObject.get()));
				em.getTransaction().commit();
				if (newlyCreatedObject) {
					postElexisEvent(getCreateEvent(identifiable));
				}
				return true;
			} finally {
				em.close();
			}
		}
		return false;
	}
	
	@Override
	public boolean save(List<Identifiable> identifiables){
		Map<Identifiable, AbstractDBObjectId> dbObjects = identifiables.parallelStream()
			.collect(Collectors.toMap(Function.identity(), i -> getDbObject(i).orElse(null)));
		if (!dbObjects.isEmpty()) {
			EntityManager em = getEntityManager();
			try {
				List<ElexisEvent> createdEvents = new ArrayList<>();
				em.getTransaction().begin();
				for (Identifiable identifiable : dbObjects.keySet()) {
					AbstractDBObjectId dbObject = dbObjects.get(identifiable);
					if (dbObject != null) {
						boolean newlyCreatedObject = (dbObject.getLastupdate() == null);
						em.merge(dbObject);
						if (newlyCreatedObject) {
							createdEvents.add(getCreateEvent(identifiable));
						}
					}
				}
				em.getTransaction().commit();
				createdEvents.stream().forEach(e -> postElexisEvent(e));
				return true;
			} finally {
				em.close();
			}
		}
		return false;
	}
	
	@Override
	public boolean remove(Identifiable identifiable){
		Optional<AbstractDBObjectId> dbObject = getDbObject(identifiable);
		if (dbObject.isPresent()) {
			EntityManager em = getEntityManager();
			try {
				em.getTransaction().begin();
				AbstractDBObjectId object = em.merge(dbObject.get());
				em.remove(object);
				em.getTransaction().commit();
				return true;
			} finally {
				em.close();
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
	
	protected Optional<AbstractDBObjectId> getDbObject(Identifiable identifiable){
		if (identifiable instanceof AbstractIdModelAdapter<?>) {
			return Optional.ofNullable(((AbstractIdModelAdapter<?>) identifiable).getEntity());
		}
		return Optional.empty();
	}
	
	protected void setDbObject(Identifiable identifiable, AbstractDBObjectId entity){
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
		Query query = getEntityManager().createNativeQuery(sql);
		return query.getResultStream();
	}
}
