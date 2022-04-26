package ch.elexis.core.findings.fhir.model.service;

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
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractModelService;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IStoreToStringContribution;

@Component(property = IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model")
public class FindingsModelService extends AbstractModelService implements IModelService, IStoreToStringContribution {

	@Reference(cardinality = ReferenceCardinality.MANDATORY, target = "(id=default)")
	private IElexisEntityManager entityManager;

	@Override
	protected EntityManager getEntityManager(boolean managed) {
		return (EntityManager) entityManager.getEntityManager(managed);
	}

	@Override
	protected void closeEntityManager(EntityManager entityManager) {
		this.entityManager.closeEntityManager(entityManager);
	}

	@Reference
	private EventAdmin eventAdmin;

	@Override
	protected EventAdmin getEventAdmin() {
		return eventAdmin;
	}

	@Activate
	public void activate() {
		adapterFactory = FindingsModelAdapterFactory.getInstance();
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

		if (storeToString.startsWith("ch.elexis.core.findings.fhir")) {
			String[] split = splitIntoTypeAndId(storeToString);

			// map string to classname
			String className = split[0];
			String id = split[1];
			Class<? extends EntityWithId> clazz = ElexisTypeMap.get(className);
			if (clazz != null) {
				EntityManager em = (EntityManager) entityManager.getEntityManager();
				EntityWithId dbObject = em.find(clazz, id);
				return Optional.ofNullable(adapterFactory.getModelAdapter(dbObject, null, false).orElse(null));
			}
		}
		return Optional.empty();
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
		return new FindingsQuery<>(clazz, refreshCache, (EntityManager) entityManager.getEntityManager(),
				includeDeleted);
	}

	/**
	 * Wrap the entity in a new ModelAdapter matching the provided type clazz. If
	 * entity is null, null is returned.
	 *
	 * @param entity
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getAdapter(EntityWithId entity, Class<T> clazz) {
		if (entity != null) {
			Optional<Identifiable> adapter = FindingsModelAdapterFactory.getInstance().getModelAdapter(entity, clazz,
					true);
			return (T) adapter.orElse(null);
		}
		return null;
	}

	/**
	 * Get the entity Object from a ModelAdapter instance.
	 *
	 * @param adapter
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends EntityWithId> T getDBObject(Object adapter, Class<T> clazz) {
		return ((AbstractIdModelAdapter<T>) adapter).getEntity();
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
				return getTypeForEntity(entityClass.newInstance());
			} catch (InstantiationException | IllegalAccessException e) {
				LoggerFactory.getLogger(getClass()).error("Error getting type for model [" + interfaze + "]", e);
			}
		}
		return null;
	}
}
