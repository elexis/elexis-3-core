package ch.elexis.core.jpa.model.adapter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.Metamodel;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.persistence.config.HintValues;
import org.eclipse.persistence.config.QueryHints;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEvent;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.exceptions.AccessControlException;
import ch.elexis.core.jpa.entities.DBLog;
import ch.elexis.core.jpa.entities.EntityWithDeleted;
import ch.elexis.core.jpa.entities.EntityWithId;
import ch.elexis.core.jpa.model.service.holder.ContextServiceHolder;
import ch.elexis.core.jpa.model.service.holder.StoreToStringServiceHolder;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.INativeQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.net.NetTool;

public abstract class AbstractModelService implements IModelService {

	private IAccessControlService accessControlService;

	protected AbstractModelAdapterFactory adapterFactory;

	protected abstract EntityManager getEntityManager(boolean managed);

	protected abstract void closeEntityManager(EntityManager entityManager);

	protected abstract EventAdmin getEventAdmin();

	protected ExecutorService executor = Executors.newCachedThreadPool();

	private List<String> blockEventTopics;

	/**
	 * Get the core model service to perform delete of XID. Can return null if model
	 * does not use XID.
	 * 
	 * @return
	 */
	protected abstract IModelService getCoreModelService();

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> load(String id, Class<T> clazz, boolean includeDeleted, boolean refreshCache) {
		if (evaluateRightNoException(clazz, Right.READ)) {
			if (StringUtils.isNotEmpty(id)) {
				EntityManager em = getEntityManager(true);
				Class<? extends EntityWithId> dbObjectClass = adapterFactory.getEntityClass(clazz);

				HashMap<String, Object> queryHints = new HashMap<>();
				if (refreshCache) {
					queryHints.put(QueryHints.REFRESH, HintValues.TRUE);
				}
				EntityWithId dbObject = em.find(dbObjectClass, id, queryHints);
				if (dbObject != null) {
					// check for deleted
					if (!includeDeleted && (dbObject instanceof EntityWithDeleted)) {
						if (((EntityWithDeleted) dbObject).isDeleted()) {
							return Optional.empty();
						}
					}
					Optional<Identifiable> modelObject = adapterFactory.getModelAdapter(dbObject, clazz, true);
					if (modelObject.isPresent() && clazz.isAssignableFrom(modelObject.get().getClass())) {
						if (evaluateRightNoException(Collections.singletonList(modelObject.get()), Right.READ)) {
							return (Optional<T>) modelObject;
						}
					}
				}
			}
		}
		return Optional.empty();
	}

	@Override
	public <T> List<T> findAll(Class<T> clazz) {
		IQuery<T> query = getQuery(clazz);
		return query.execute();
	}

	@Override
	public <T> List<T> findAllById(Collection<String> ids, Class<T> clazz) {
		IQuery<T> query = getQuery(clazz);
		if (ids != null && !ids.isEmpty()) {
			query.and("id", COMPARATOR.IN, ids); //$NON-NLS-1$
			return query.execute();
		}
		return Collections.emptyList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> adapt(Object jpaEntity, Class<T> clazz) {
		if (jpaEntity instanceof EntityWithId) {
			return (Optional<T>) adapterFactory.getModelAdapter((EntityWithId) jpaEntity, clazz, false);
		}
		return Optional.empty();
	}

	@Override
	public Class<?> getEntityClass(Class<?> clazz) {
		return adapterFactory.getEntityClass(clazz);
	}

	@Override
	public void refresh(Identifiable identifiable, boolean refreshCache) {
		EntityManager em = getEntityManager(true);
		EntityWithId dbObject = getDbObject(identifiable).orElse(null);
		if (dbObject != null) {
			HashMap<String, Object> queryHints = new HashMap<>();
			if (refreshCache) {
				queryHints.put(QueryHints.REFRESH, HintValues.TRUE);
			}
			EntityWithId reloadedDbObject = em.find(dbObject.getClass(), dbObject.getId(), queryHints);
			if (reloadedDbObject != null) {
				setDbObject(identifiable, reloadedDbObject, false);
			}
		}
	}

	@Override
	public Object getEntityProperty(String propertyName, Identifiable identifiable) {
		EntityWithId dbObject = getDbObject(identifiable).orElse(null);
		if (dbObject != null) {
			try {
				return BeanUtils.getProperty(dbObject, propertyName);
			} catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				LoggerFactory.getLogger(getClass())
						.error("Could not get property [" + propertyName + "] of entity [" + dbObject + "]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		return null;
	}

	@Override
	public void setEntityProperty(String propertyName, Object value, Identifiable identifiable) {
		EntityWithId dbObject = getDbObject(identifiable).orElse(null);
		if (dbObject != null) {
			try {
				BeanUtils.setProperty(dbObject, propertyName, value);
			} catch (IllegalAccessException | InvocationTargetException e) {
				LoggerFactory.getLogger(getClass())
						.error("Could not set property [" + propertyName + "] of entity [" + dbObject + "]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addToEntityList(String getterName, Identifiable value, Identifiable identifiable) {
		EntityWithId dbObject = getDbObject(identifiable).orElse(null);
		if (dbObject != null) {
			EntityWithId dbValueObject = getDbObject(value).orElse(null);
			if (dbValueObject != null) {
				try {
					Method getterMethod = dbObject.getClass().getMethod(getterName, (Class[]) null);
					Object list = getterMethod.invoke(dbObject, (Object[]) null);
					if (list instanceof List<?>) {
						((List<Object>) list).add(dbValueObject);
					}
				} catch (SecurityException | IllegalArgumentException | IllegalAccessException
						| InvocationTargetException | NoSuchMethodException e) {
					LoggerFactory.getLogger(getClass())
							.error("Could not add to entity list [" + getterName + "] of entity [" + dbObject + "]", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				}
			}
		}
	}

	@Override
	public void save(Identifiable identifiable) {
		if (identifiable != null && evaluateRight(identifiable.getClass(), Right.UPDATE)) {
			if (identifiable.getChanged() != null) {
				save(Collections.singletonList(identifiable));
				return;
			}
			Optional<EntityWithId> dbObject = getDbObject(identifiable);
			if (dbObject.isPresent()) {
				boolean newlyCreatedObject = (dbObject.get().getLastupdate() == null);
				EntityManager em = getEntityManager(false);
				try {
					em.getTransaction().begin();
					EntityWithId merged = em.merge(dbObject.get());
					em.getTransaction().commit();
					// update model adapters and post events
					if (identifiable instanceof AbstractIdModelAdapter) {
						// clear dirty state before setting merged entity
						setDbObject(identifiable, merged, true);
					}
					if (identifiable.getRefresh() != null) {
						for (Identifiable toRefresh : identifiable.getRefresh()) {
							refresh(toRefresh, true);
						}
						identifiable.clearRefresh();
					}
					if (identifiable.getUpdated() != null) {
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, identifiable,
								Collections.singletonMap(ElexisEventTopics.PROPKEY_UPDATED, identifiable.getUpdated()));
						identifiable.clearUpdated();
					}
					if (newlyCreatedObject) {
						ElexisEvent createEvent = getCreateEvent(identifiable);
						if (createEvent != null) {
							if (ContextServiceHolder.isPresent()) {
								String userId = ContextServiceHolder.get().getActiveUser().map(Identifiable::getId)
										.orElse(null);
								if (userId != null) {
									createEvent.getProperties().put(ElexisEventTopics.PROPKEY_USER, userId);
								}
							}
							postElexisEvent(createEvent);
						}
						postEvent(ElexisEventTopics.EVENT_CREATE, identifiable);
					}
					return;
				} finally {
					closeEntityManager(em);
				}
			}
			String message = "Could not save [" + identifiable + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			LoggerFactory.getLogger(getClass()).error(message);
			throw new IllegalStateException(message);
		}
	}

	@Override
	public void save(List<? extends Identifiable> identifiables) {
		if (evaluateRight(identifiables, Right.UPDATE)) {
			if (identifiables == null || identifiables.isEmpty()) {
				return;
			}
			identifiables = addChanged(identifiables);
			Map<Identifiable, EntityWithId> dbObjects = new HashMap<>();
			for (Identifiable identifiable : identifiables) {
				dbObjects.put(identifiable, getDbObject(identifiable).orElse(null));
			}
			if (!dbObjects.isEmpty()) {
				EntityManager em = getEntityManager(false);
				try {
					// collect information for update of model adapters and events
					List<ElexisEvent> createdEvents = new ArrayList<>();
					List<Identifiable> createdIdentifiables = new ArrayList<>();
					Map<Identifiable, EntityWithId> mergedEntities = new HashMap<>();
					em.getTransaction().begin();
					for (Identifiable identifiable : identifiables) {
						EntityWithId dbObject = dbObjects.get(identifiable);
						if (dbObject != null) {
							boolean newlyCreatedObject = (dbObject.getLastupdate() == null);

							EntityWithId merged = em.merge(dbObject);
							mergedEntities.put(identifiable, merged);
							if (newlyCreatedObject) {
								ElexisEvent createEvent = getCreateEvent(identifiable);
								if (createEvent != null) {
									if (ContextServiceHolder.isPresent()) {
										String userId = ContextServiceHolder.get().getActiveUser()
												.map(Identifiable::getId).orElse(null);
										if (userId != null) {
											createEvent.getProperties().put(ElexisEventTopics.PROPKEY_USER, userId);
										}
									}
									createdEvents.add(createEvent);
								}
								createdIdentifiables.add(identifiable);
							}
						}
					}
					em.getTransaction().commit();
					// update model adapters and post events
					identifiables.stream().forEach(i -> {
						if (i instanceof AbstractIdModelAdapter) {
							setDbObject(i, mergedEntities.get(i), true);
							if (i.getRefresh() != null) {
								for (Identifiable toRefresh : i.getRefresh()) {
									refresh(toRefresh, true);
								}
								i.clearRefresh();
							}
							if (i.getUpdated() != null) {
								ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, i,
										Collections.singletonMap(ElexisEventTopics.PROPKEY_UPDATED, i.getUpdated()));
								i.clearUpdated();
							}
						}
					});
					createdEvents.stream().forEach(e -> postElexisEvent(e));
					createdIdentifiables.stream().forEach(i -> postEvent(ElexisEventTopics.EVENT_CREATE, i));
					return;
				} finally {
					closeEntityManager(em);
				}
			}
			String message = "Could not save list [" + identifiables + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			LoggerFactory.getLogger(getClass()).error(message);
			throw new IllegalStateException(message);
		}
	}

	@Override
	public void touch(Identifiable identifiable) {
		if (evaluateRight(identifiable.getClass(), Right.UPDATE)) {
			Optional<EntityWithId> dbObject = getDbObject(identifiable);
			if (dbObject.isPresent()) {
				EntityManager em = getEntityManager(false);
				try {
					em.getTransaction().begin();
					dbObject.get().setLastupdate(System.currentTimeMillis());
					em.merge(dbObject.get());
					em.getTransaction().commit();
				} finally {
					closeEntityManager(em);
				}
			}
		}
	}

	protected List<? extends Identifiable> addChanged(List<? extends Identifiable> identifiables) {
		List<Identifiable> ret = new ArrayList<>();
		ret.addAll(identifiables);
		identifiables.forEach(i -> {
			if (i.getChanged() != null) {
				for (Identifiable changed : i.getChanged()) {
					if (!ret.contains(changed)) {
						ret.add(changed);
					}
				}
				i.clearChanged();
			}
		});
		return ret;
	}

	@Override
	public void remove(Identifiable identifiable) {
		if (evaluateRight(identifiable.getClass(), Right.REMOVE)) {
			Optional<EntityWithId> dbObject = getDbObject(identifiable);
			if (dbObject.isPresent()) {
				EntityManager em = getEntityManager(false);
				try {
					em.getTransaction().begin();
					EntityWithId object = em.merge(dbObject.get());
					em.remove(object);
					em.getTransaction().commit();
					postEvent(ElexisEventTopics.EVENT_DELETE, identifiable);
					return;
				} finally {
					closeEntityManager(em);
				}
			}
		}
		String message = "Could not remove [" + identifiable + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		LoggerFactory.getLogger(getClass()).error(message);
		throw new IllegalStateException(message);
	}

	@Override
	public void remove(List<? extends Identifiable> identifiables) {

		if (identifiables != null) {
			List<Identifiable> listOfRemoved = new ArrayList<>();
			EntityManager em = getEntityManager(false);
			try {
				em.getTransaction().begin();
				for (Identifiable identifiable : identifiables) {
					Optional<EntityWithId> dbObject = getDbObject(identifiable);
					if (dbObject.isPresent()) {
						em.remove(em.merge(dbObject.get()));
						listOfRemoved.add(identifiable);
					}
				}
				em.getTransaction().commit();
				for (Identifiable removed : listOfRemoved) {
					postEvent(ElexisEventTopics.EVENT_DELETE, removed);
				}
			} finally {
				closeEntityManager(em);
			}

		}
	}

	/**
	 * Get an {@link ElexisEvent} representation of {@link Identifiable} creation.
	 * Called by the save methods, to send creation events. As the creation event
	 * currently uses storeToString, this method has to be implemented in sub
	 * classes.
	 *
	 * @param identifiable
	 * @return
	 */
	protected abstract ElexisEvent getCreateEvent(Identifiable identifiable);

	protected Optional<EntityWithId> getDbObject(Object adapter) {
		if (adapter instanceof AbstractIdModelAdapter<?>) {
			return Optional.ofNullable(((AbstractIdModelAdapter<?>) adapter).getEntity());
		}
		return Optional.empty();
	}

	/**
	 * Set the {@link EntityWithId} in all {@link AbstractIdModelAdapter} instances
	 * using {@link ElexisEventTopics#PERSISTENCE_EVENT_ENTITYCHANGED} event.
	 *
	 * @param adapter
	 * @param merged
	 */
	protected void setDbObject(Object adapter, EntityWithId entity, boolean resetDirty) {
		if (adapter instanceof AbstractIdModelAdapter<?>) {
			((AbstractIdModelAdapter<?>) adapter).setEntity(entity, resetDirty);
			// synchronous change event will set the entity in all entity model adapter
			// known to EntityChangeEventListener
			sendEntityChangeEvent(entity);
		}
	}

	private void sendEntityChangeEvent(EntityWithId entity) {
		if (getEventAdmin() != null) {
			Map<String, Object> properites = new HashMap<>();
			properites.put(EntityWithId.class.getName(), entity);
			Event event = new Event(ElexisEventTopics.PERSISTENCE_EVENT_ENTITYCHANGED, properites);
			getEventAdmin().sendEvent(event);
		} else {
			throw new IllegalStateException("No EventAdmin available"); //$NON-NLS-1$
		}
	}

	private void deleteXids(Identifiable identifiable) {
		if (getCoreModelService() != null) {
			INamedQuery<IXid> query = getCoreModelService().getNamedQuery(IXid.class, "objectid");
			query.executeWithParameters(query.getParameterMap("objectid", identifiable.getId())).forEach(xid -> {
				xid.setDeleted(true);
				getCoreModelService().save(xid);
			});
		}
	}

	@Override
	public void delete(Deleteable deletable) {
		if (evaluateRight(deletable.getClass(), Right.DELETE)) {
			deletable.setDeleted(true);
			deleteXids((Identifiable) deletable);
			save((Identifiable) deletable);
			createDBLog((Identifiable) deletable);
			postEvent(ElexisEventTopics.EVENT_DELETE, deletable);
		}
	}

	@Override
	public void delete(List<? extends Deleteable> deletables) {
		if (deletables != null) {
			if (evaluateRight(deletables.stream().map(d -> (Identifiable) d).collect(Collectors.toList()),
					Right.DELETE)) {
				List<Identifiable> identifiables = new ArrayList<>();
				deletables.forEach(item -> {
					item.setDeleted(true);
					deleteXids((Identifiable) item);
					identifiables.add((Identifiable) item);
				});
				save(identifiables);
				identifiables.forEach(item -> {
					createDBLog(item);
					postEvent(ElexisEventTopics.EVENT_DELETE, item);
				});
			}
		}
	}

	/**
	 * Creates a db log uses the active transaction if exists otherwise creates a
	 * new one
	 *
	 * @param identifiable
	 */
	private void createDBLog(Identifiable identifiable) {
		DBLog dbLog = new DBLog();
		dbLog.setUserId(ContextServiceHolder.getActiveUserContact().map(IContact::getId).orElse("?")); //$NON-NLS-1$
		dbLog.setOid(StoreToStringServiceHolder.getStoreToString(identifiable).orElse(identifiable.getId()));
		dbLog.setTyp(DBLog.Type.DELETE);
		dbLog.setDatum(LocalDate.now());
		dbLog.setStation(Optional.ofNullable(NetTool.hostname).orElse("?")); //$NON-NLS-1$

		EntityManager em = getEntityManager(true);
		if (!em.getTransaction().isActive()) {
			em.getTransaction().begin();
			em.merge(dbLog);
			em.getTransaction().commit();
		} else {
			// use active transaction
			em.merge(dbLog);
		}
	}

	@Override
	public void postEvent(String topic, Object object) {
		if (getEventAdmin() != null) {
			if (blockEventTopics != null && blockEventTopics.contains(topic)) {
				return;
			}
			Map<String, Object> properties = new HashMap<>();
			properties.put(ElexisEventTopics.ECLIPSE_E4_DATA, object);
			Event event = new Event(topic, properties);
			getEventAdmin().postEvent(event);
		} else {
			throw new IllegalStateException("No EventAdmin available"); //$NON-NLS-1$
		}
	}

	// TODO @deprecated?!
	public void postElexisEvent(ElexisEvent elexisEvent) {
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
			throw new IllegalStateException("No EventAdmin available"); //$NON-NLS-1$
		}
	}

	@Override
	public <T> T create(Class<T> clazz) {
		if (evaluateRight(clazz, Right.CREATE)) {
			return adapterFactory.createAdapter(clazz);
		}
		return null;
	}

	private IAccessControlService getAccessControlService() {
		if (accessControlService == null) {
			accessControlService = OsgiServiceUtil.getService(IAccessControlService.class).orElse(null);
		}
		return accessControlService;
	}

	protected boolean evaluateRightNoException(Class<?> clazz, Right right) {
		boolean ret = CoreUtil.isTestMode();
		if (getAccessControlService() != null) {
			ret = getAccessControlService().evaluate(EvACE.of(clazz, right));
			if (!ret) {
				String message = "(ACL " + System.currentTimeMillis() + ") User has no right [" + right
						+ "] for class [" + clazz.getName() + "]";
				if (ElexisSystemPropertyConstants.VERBOSE_ACL_NOTIFICATION) {
					fullNotify(message, new Throwable());
				} else {
					LoggerFactory.getLogger(getClass()).info(message);
				}
			}
		}
		return ret;
	}

	private void fullNotify(String message, Throwable throwable) {
		Map<String, Object> eventMap = new HashMap<>();
		eventMap.put(ElexisEventTopics.NOTIFICATION_PROPKEY_TITLE, "Access Denied");
		eventMap.put(ElexisEventTopics.NOTIFICATION_PROPKEY_MESSAGE, message);
		Event notificationEvent = new Event(ElexisEventTopics.BASE_NOTIFICATION + "warn", eventMap);
		getEventAdmin().postEvent(notificationEvent);
		LoggerFactory.getLogger(getClass()).info(message, new Throwable());

	}

	protected boolean evaluateRight(Class<?> clazz, Right right) throws AccessControlException {
		boolean ret = evaluateRightNoException(clazz, right);
		if (!ret) {
			AccessControlException accessControlException = new AccessControlException(clazz, right);
			if (ElexisSystemPropertyConstants.VERBOSE_ACL_NOTIFICATION) {
				fullNotify("(ACL " + System.currentTimeMillis() + ") " + accessControlException.getMessage(),
						accessControlException);
			}
			throw accessControlException;
		}
		return ret;
	}

	protected boolean evaluateRightNoException(List<? extends Identifiable> identifiables, Right right) {
		boolean ret = true;
		if (identifiables != null && !identifiables.isEmpty()) {
			// test class of first object
			if (!evaluateRightNoException(identifiables.get(0).getClass(), right)) {
				return false;
			}
			// test all objects if aobo
			if (getAccessControlService() != null) {
				for (Identifiable identifiable : identifiables) {
					String storeToString = StoreToStringServiceHolder.getStoreToString(identifiable).orElse(null);
					if (storeToString != null) {
						ObjectEvaluatableACE objAce = (ObjectEvaluatableACE) EvACE.of(identifiable.getClass(), right,
								storeToString);
						if (!getAccessControlService().evaluate(objAce)) {
							return false;
						}
					}
				}
			}
		}
		return ret;
	}

	protected boolean evaluateRight(List<? extends Identifiable> identifiables, Right right)
			throws AccessControlException {
		boolean ret = evaluateRightNoException(identifiables, right);
		if (!ret && identifiables != null && !identifiables.isEmpty()) {
			throw new AccessControlException(identifiables.get(0).getClass(), right);
		}
		return ret;
	}

	@Override
	public Stream<?> executeNativeQuery(String sql) {
		Query query = getEntityManager(true).createNativeQuery(sql);
		return query.getResultStream();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> Stream<T> executeNativeQuery(String sql, Class<T> interfaceClazz) {
		Class<? extends EntityWithId> entityClazz = adapterFactory.getEntityClass(interfaceClazz);
		Query query = getEntityManager(true).createNativeQuery(sql, entityClazz);
		return query.getResultStream()
				.map(e -> adapterFactory.getModelAdapter((EntityWithId) e, interfaceClazz, true).get());
	}

	@Override
	public int executeNativeUpdate(String sql, boolean invalidateCache) {
		EntityManager em = getEntityManager(false);
		try {
			em.getTransaction().begin();
			int affected = em.createNativeQuery(sql).executeUpdate();
			em.getTransaction().commit();
			return affected;
		} finally {
			closeEntityManager(em);
			if (invalidateCache) {
				clearCache();
			}
		}
	}

	protected String getNamedQueryName(Class<?> clazz, String... properties) {
		Class<? extends EntityWithId> entityClazz = adapterFactory.getEntityClass(clazz);
		StringJoiner queryName = new StringJoiner("."); //$NON-NLS-1$
		queryName.add(entityClazz.getSimpleName());
		for (String string : properties) {
			queryName.add(string);
		}
		if (accessControlService.isAoboOrSelf(EvACE.of(clazz, Right.READ)).isPresent()) {
			queryName.add("aobo");
		}
		return queryName.toString();
	}

	@Override
	public INativeQuery getNativeQuery(String sql) {
		Query query = getEntityManager(true).createNativeQuery(sql);
		return new NativeQuery(query);
	}

	@Override
	public <R, T> INamedQuery<R> getNamedQuery(Class<R> returnValueclazz, Class<T> definitionClazz,
			boolean refreshCache, String... properties) {
		if (evaluateRightNoException(definitionClazz, Right.READ)) {
			return new NamedQuery<>(returnValueclazz, definitionClazz, refreshCache, adapterFactory,
					getEntityManager(true), getNamedQueryName(definitionClazz, properties));
		}
		return new EmptyNamedQuery<>();
	}

	@Override
	public <R, T> INamedQuery<R> getNamedQueryByName(Class<R> returnValueclazz, Class<T> definitionClazz,
			boolean refreshCache, String queryName) {
		if (evaluateRightNoException(definitionClazz, Right.READ)) {
			return new NamedQuery<>(returnValueclazz, definitionClazz, refreshCache, adapterFactory,
					getEntityManager(true), queryName);
		}
		return new EmptyNamedQuery<>();
	}

	@Override
	public <T> long getHighestLastUpdate(Class<T> clazz) {
		INativeQuery nativeQuery = getNativeQuery("SELECT COALESCE(MAX(LASTUPDATE),0) FROM " //$NON-NLS-1$
				+ getTableName(getEntityManager(true), getEntityClass(clazz)));
		Optional<?> result = nativeQuery.executeWithParameters(Collections.emptyMap()).findFirst();
		if (result.isPresent()) {
			// Native queries can return different objects based on the database driver
			return ((Number) result.get()).longValue();
		}
		return 0;
	}

	private <T> String getTableName(EntityManager em, Class<T> entityClass) {
		/*
		 * Check if the specified class is present in the metamodel. Throws
		 * IllegalArgumentException if not.
		 */
		Metamodel meta = em.getMetamodel();
		EntityType<T> entityType = meta.entity(entityClass);

		// Check whether @Table annotation is present on the class.
		Table t = entityClass.getAnnotation(Table.class);

		String tableName = (t == null) ? entityType.getName().toUpperCase() : t.name();
		return tableName;
	}

	@Override
	public synchronized void setBlockEventTopics(List<String> blockEventTopics) {
		this.blockEventTopics = blockEventTopics;
	}
}
