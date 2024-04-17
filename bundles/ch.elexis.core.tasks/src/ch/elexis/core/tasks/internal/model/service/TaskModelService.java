package ch.elexis.core.tasks.internal.model.service;

import java.util.Optional;

import javax.persistence.EntityManager;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.EventAdmin;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.jpa.model.adapter.AbstractModelService;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IStoreToStringContribution;

@Component(property = IModelService.SERVICEMODELNAME + "=ch.elexis.core.tasks.model")
public class TaskModelService extends AbstractModelService implements IModelService, IStoreToStringContribution {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService coreModelService;

	@Reference
	private IElexisEntityManager entityManager;

	@Reference
	private EventAdmin eventAdmin;

	@Activate
	public void activate() {
		adapterFactory = TaskModelAdapterFactory.getInstance();
	}

	@Override
	public <T> IQuery<T> getQuery(Class<T> clazz, boolean refreshCache, boolean includeDeleted) {
		return new TaskModelQuery<>(clazz, refreshCache, (EntityManager) entityManager.getEntityManager(),
				includeDeleted);
	}

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

	@Override
	protected ElexisEvent getCreateEvent(Identifiable identifiable) {
		return null;
	}

	@Override
	public void clearCache() {
		this.entityManager.clearCache();
	}

	@Override
	public Optional<String> storeToString(Identifiable identifiable) {
		for (Class<?> clazz : identifiable.getClass().getInterfaces()) {
			if (clazz.getName().startsWith("ch.elexis.core.tasks.model")) {
				return Optional.of(clazz.getName() + StringConstants.DOUBLECOLON + identifiable.getId());
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

		if (storeToString.startsWith("ch.elexis.core.tasks.model")) {
			try {
				String[] split = splitIntoTypeAndId(storeToString);

				// map string to classname
				String className = split[0];
				String id = split[1];
				@SuppressWarnings("unchecked")
				Class<Identifiable> clazz = (Class<Identifiable>) TaskModelService.class.getClassLoader()
						.loadClass(className);
				if (clazz != null) {
					return load(id, clazz);
				}
			} catch (ClassNotFoundException e) {
				LoggerFactory.getLogger(getClass()).warn("Could not load class of [" + storeToString + "]");
			}
		}
		return Optional.empty();
	}

	@Override
	public Class<?> getEntityForType(String type) {
		if (type.startsWith("ch.elexis.core.tasks.model")) {
			try {
				@SuppressWarnings("unchecked")
				Class<Identifiable> clazz = (Class<Identifiable>) TaskModelService.class.getClassLoader()
						.loadClass(type);
				if (clazz != null) {
					return adapterFactory.getEntityClass(clazz);
				}
			} catch (ClassNotFoundException e) {
				LoggerFactory.getLogger(getClass()).warn("Could not load class for type [" + type + "]");
			}
		}
		return null;
	}

	@Override
	public String getTypeForEntity(Object entityInstance) {
		return null;
	}

	@Override
	public String getTypeForModel(Class<?> interfaze) {
		return null;
	}

	@Override
	protected IModelService getCoreModelService() {
		return coreModelService;
	}
}
