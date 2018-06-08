package ch.elexis.core.model.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.jpa.entities.AbstractDBObjectIdDeleted;
import ch.elexis.core.jpa.entitymanager.ElexisEntityManger;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractModelAdapterFactory;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;

@Component(property = IModelService.SERVICEMODELNAME + "=ch.elexis.core.model")
public class CoreModelService implements IModelService {
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private ElexisEntityManger entityManager;
	
	@Reference
	private EventAdmin eventAdmin;
	
	private AbstractModelAdapterFactory adapterFactory;
	
	@Activate
	public void activate(){
		adapterFactory = new CoreModelAdapterFactory();
	}
	
	@Override
	public Optional<String> storeToString(Identifiable identifiable){
		String classKey = null;
		Optional<AbstractDBObjectIdDeleted> dbObject = getDbObject(identifiable);
		if (dbObject.isPresent()) {
			classKey = ElexisTypeMap.getKeyForObject(dbObject.get());
			if (classKey == null) {
				LoggerFactory.getLogger(getClass()).warn(
					"Could not resolve [{}] to storeToString name",
					(identifiable != null) ? identifiable.getClass() : "null");
			} else {
				return Optional.of(classKey + StringConstants.DOUBLECOLON + identifiable.getId());
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<Identifiable> loadFromString(String storeToString){
		if (storeToString == null) {
			LoggerFactory.getLogger(getClass()).warn("StoreToString is null");
			return Optional.empty();
		}
		
		String[] split = splitIntoTypeAndId(storeToString);
		
		// map string to classname
		String className = split[0];
		String id = split[1];
		Class<? extends AbstractDBObjectIdDeleted> clazz = ElexisTypeMap.get(className);
		if (clazz == null) {
			LoggerFactory.getLogger(getClass()).warn("Could not resolve class [{}] from [{}]",
				className, storeToString);
			return Optional.empty();
		}
		
		EntityManager em = entityManager.getEntityManager();
		try {
			AbstractDBObjectIdDeleted dbObject = em.find(clazz, id);
			return Optional
				.ofNullable(adapterFactory.getModelAdapter(dbObject, null, false).orElse(null));
		} finally {
			em.close();
		}
	}
	
	@Override
	public <T> T create(Class<T> clazz){
		return adapterFactory.createAdapter(clazz);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> load(String id, Class<T> clazz){
		EntityManager em = entityManager.getEntityManager();
		try {
			Class<? extends AbstractDBObjectIdDeleted> dbObjectClass =
				adapterFactory.getEntityClass(clazz);
			AbstractDBObjectIdDeleted dbObject = em.find(dbObjectClass, id);
			Optional<Identifiable> modelObject =
				adapterFactory.getModelAdapter(dbObject, clazz, true);
			if (modelObject.isPresent() && clazz.isAssignableFrom(modelObject.get().getClass())) {
				return (Optional<T>) modelObject;
			}
		} finally {
			em.close();
		}
		return Optional.empty();
	}
	
	@Override
	public boolean save(Identifiable identifiable){
		Optional<AbstractDBObjectIdDeleted> dbObject = getDbObject(identifiable);
		if (dbObject.isPresent()) {
			EntityManager em = entityManager.getEntityManager();
			try {
				em.getTransaction().begin();
				boolean newlyCreatedObject = (dbObject.get().getLastupdate() == null);
				em.merge(dbObject.get());
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
		Map<Identifiable, AbstractDBObjectIdDeleted> dbObjects = identifiables.parallelStream()
			.collect(Collectors.toMap(Function.identity(), i -> getDbObject(i).orElse(null)));
		if (!dbObjects.isEmpty()) {
			EntityManager em = entityManager.getEntityManager();
			try {
				List<ElexisEvent> createdEvents = new ArrayList<>();
				em.getTransaction().begin();
				for (Identifiable identifiable : dbObjects.keySet()) {
					AbstractDBObjectIdDeleted dbObject = dbObjects.get(identifiable);
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
	
	private ElexisEvent getCreateEvent(Identifiable identifiable){
		ElexisEvent ee = new ElexisEvent();
		ee.setTopic(ElexisEventTopics.PERSISTENCE_EVENT_CREATE);
		if (identifiable instanceof AbstractIdDeleteModelAdapter<?>) {
			AbstractDBObjectIdDeleted dbObject =
				((AbstractIdDeleteModelAdapter<?>) identifiable).getEntity();
			ee.getProperties().put(ElexisEventTopics.PROPKEY_ID, dbObject.getId());
			ee.getProperties().put(ElexisEventTopics.PROPKEY_CLASS,
				ElexisTypeMap.getKeyForObject(dbObject));
		}
		return ee;
	}
	
	public void postElexisEvent(ElexisEvent elexisEvent){
		if (elexisEvent == null || elexisEvent.getTopic() == null) {
			return;
		}
		String topic = elexisEvent.getTopic();
		if (!topic.startsWith(ElexisEventTopics.BASE)) {
			topic = ElexisEventTopics.BASE + topic;
		}
		Event event = new Event(topic, elexisEvent.getProperties());
		if (eventAdmin != null) {
			eventAdmin.sendEvent(event);
		} else {
			throw new IllegalStateException("No EventAdmin available");
		}
	}
	
	private Optional<AbstractDBObjectIdDeleted> getDbObject(Identifiable identifiable){
		if (identifiable instanceof AbstractIdDeleteModelAdapter<?>) {
			return Optional.ofNullable(((AbstractIdDeleteModelAdapter<?>) identifiable).getEntity());
		}
		return Optional.empty();
	}
}
