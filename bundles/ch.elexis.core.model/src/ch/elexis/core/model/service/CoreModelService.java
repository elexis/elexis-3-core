package ch.elexis.core.model.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventAdmin;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractModelService;
import ch.elexis.core.jpa.model.adapter.EmptyQuery;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IStoreToStringContribution;

@Component(property = IModelService.SERVICEMODELNAME + "=ch.elexis.core.model")
public class CoreModelService extends AbstractModelService implements IModelService, IStoreToStringContribution {

	@Reference(target = "(id=default)")
	private IElexisEntityManager entityManager;

	@Reference
	private EventAdmin eventAdmin;

	@Override
	protected EntityManager getEntityManager(boolean managed) {
		return (EntityManager) entityManager.getEntityManager(managed);
	}

	@Override
	protected void closeEntityManager(EntityManager entityManager) {
		this.entityManager.closeEntityManager(entityManager);
	}

	@Override
	protected EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	@Activate
	public void activate() {
		adapterFactory = CoreModelAdapterFactory.getInstance();
	}

	@Override
	public Optional<String> storeToString(Identifiable identifiable) {
		String classKey = null;
		Optional<EntityWithId> dbObject = getDbObject(identifiable);
		if (dbObject.isPresent()) {
			classKey = ElexisTypeMap.getKeyForObject(dbObject.get());
			if (classKey != null) {
				return Optional.of(classKey + StringConstants.DOUBLECOLON + identifiable.getId());
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<Identifiable> loadFromString(String storeToString) {
		if (storeToString == null) {
			LoggerFactory.getLogger(getClass()).warn("StoreToString is null");
			return Optional.empty();
		}

		String[] split = splitIntoTypeAndId(storeToString);
		if (split != null && split.length == 2) {

			// map string to classname
			String className = split[0];
			String id = split[1];
			Class<? extends EntityWithId> clazz = ElexisTypeMap.get(className);
			Class<? extends Identifiable> interfaceClass = ElexisTypeMap.getInterfaceClass(className);
			if (clazz != null) {
				EntityManager em = (EntityManager) entityManager.getEntityManager();
				EntityWithId dbObject = em.find(clazz, id);
				if (dbObject != null) {
					return Optional
							.ofNullable(adapterFactory.getModelAdapter(dbObject, interfaceClass, false).orElse(null));
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public List<Identifiable> loadFromStringWithIdPart(String partialStoreToString) {
		if (partialStoreToString == null) {
			LoggerFactory.getLogger(getClass()).warn("StoreToString is null");
			return Collections.emptyList();
		}

		String[] split = splitIntoTypeAndId(partialStoreToString);
		if (split != null && split.length == 2) {

			// map string to classname
			String className = split[0];
			String id = split[1];
			Class<? extends EntityWithId> clazz = ElexisTypeMap.get(className);
			if (clazz != null) {
				EntityManager em = (EntityManager) entityManager.getEntityManager();
				TypedQuery<? extends EntityWithId> query = em.createQuery(
						"SELECT entity FROM " + clazz.getSimpleName() + " entity WHERE entity.id LIKE :idpart", clazz);
				query.setParameter("idpart", id + "%");
				List<? extends EntityWithId> found = query.getResultList();
				if (!found.isEmpty()) {
					return found.parallelStream().map(e -> adapterFactory.getModelAdapter(e, null, false).orElse(null))
							.collect(Collectors.toList());
				}
			}
		}
		return Collections.emptyList();
	}

	@Override
	protected ElexisEvent getCreateEvent(Identifiable identifiable) {
		ElexisEvent ee = new ElexisEvent();
		ee.setTopic(ElexisEventTopics.PERSISTENCE_EVENT_CREATE);
		if (identifiable instanceof AbstractIdModelAdapter<?>) {
			EntityWithId dbObject = ((AbstractIdModelAdapter<?>) identifiable).getEntity();
			ee.getProperties().put(ElexisEventTopics.PROPKEY_ID, dbObject.getId());
			ee.getProperties().put(ElexisEventTopics.PROPKEY_CLASS, ElexisTypeMap.getKeyForObject(dbObject));
		}
		return ee;
	}

	@Override
	public <T> IQuery<T> getQuery(Class<T> clazz, boolean refreshCache, boolean includeDeleted) {
		if (evaluateRightNoException(clazz, Right.READ)) {
			return new CoreQuery<>(clazz, refreshCache, (EntityManager) entityManager.getEntityManager(), includeDeleted);
		}
		return new EmptyQuery<>();
	}

	@Override
	public void clearCache() {
		entityManager.clearCache();
	}

	@Override
	public Class<?> getEntityForType(String type) {
		return ElexisTypeMap.get(type);
	}

	@Override
	public String getTypeForEntity(Object entityInstance) {
		return ElexisTypeMap.getKeyForObject((EntityWithId) entityInstance);
	}

	@Override
	public String getTypeForModel(Class<?> interfaze) {
		Class<? extends EntityWithId> entityClass = adapterFactory.getEntityClass(interfaze);
		if (entityClass != null) {
			try {
				// modify instance to reflect different types of same entity
				EntityWithId instance = entityClass.newInstance();
				if (instance instanceof Kontakt) {
					if (IPatient.class.isAssignableFrom(interfaze)) {
						((Kontakt) instance).setPatient(true);
					}
					if (IPerson.class.isAssignableFrom(interfaze)) {
						((Kontakt) instance).setPerson(true);
					}
					if (IOrganization.class.isAssignableFrom(interfaze)) {
						((Kontakt) instance).setOrganisation(true);
					}
					if (ILaboratory.class.isAssignableFrom(interfaze)) {
						((Kontakt) instance).setLaboratory(true);
					}
				}
				return getTypeForEntity(instance);
			} catch (InstantiationException | IllegalAccessException e) {
				LoggerFactory.getLogger(getClass()).error("Error getting type for model [" + interfaze + "]", e);
			}
		}
		return null;
	}

	@Override
	protected IModelService getCoreModelService() {
		return this;
	}
}
