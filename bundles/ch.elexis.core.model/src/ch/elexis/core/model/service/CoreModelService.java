package ch.elexis.core.model.service;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.event.EventAdmin;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.jpa.entities.AbstractDBObjectId;
import ch.elexis.core.jpa.entitymanager.ElexisEntityManger;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractModelService;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;

@Component(property = IModelService.SERVICEMODELNAME + "=ch.elexis.core.model")
public class CoreModelService extends AbstractModelService implements IModelService {
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private ElexisEntityManger entityManager;
	
	@Override
	protected EntityManager getEntityManager(){
		return entityManager.getEntityManager();
	}
	
	@Reference
	private EventAdmin eventAdmin;
	
	@Override
	protected EventAdmin getEventAdmin(){
		return eventAdmin;
	}
	
	@Activate
	public void activate(){
		adapterFactory = CoreModelAdapterFactory.getInstance();
	}
	
	@Override
	public Optional<String> storeToString(Identifiable identifiable){
		String classKey = null;
		Optional<AbstractDBObjectId> dbObject = getDbObject(identifiable);
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
		Class<? extends AbstractDBObjectId> clazz = ElexisTypeMap.get(className);
		if (clazz == null) {
			LoggerFactory.getLogger(getClass()).warn("Could not resolve class [{}] from [{}]",
				className, storeToString);
			return Optional.empty();
		}
		
		EntityManager em = entityManager.getEntityManager();
		try {
			AbstractDBObjectId dbObject = em.find(clazz, id);
			return Optional
				.ofNullable(adapterFactory.getModelAdapter(dbObject, null, false).orElse(null));
		} finally {
			em.close();
		}
	}
	
	@Override
	protected ElexisEvent getCreateEvent(Identifiable identifiable){
		ElexisEvent ee = new ElexisEvent();
		ee.setTopic(ElexisEventTopics.PERSISTENCE_EVENT_CREATE);
		if (identifiable instanceof AbstractIdModelAdapter<?>) {
			AbstractDBObjectId dbObject = ((AbstractIdModelAdapter<?>) identifiable).getEntity();
			ee.getProperties().put(ElexisEventTopics.PROPKEY_ID, dbObject.getId());
			ee.getProperties().put(ElexisEventTopics.PROPKEY_CLASS,
				ElexisTypeMap.getKeyForObject(dbObject));
		}
		return ee;
	}
	
	@Override
	public <T> IQuery<T> getQuery(Class<T> clazz, boolean includeDeleted){
		return new CoreQuery<>(clazz, entityManager.getEntityManager(), includeDeleted);
	}
}
