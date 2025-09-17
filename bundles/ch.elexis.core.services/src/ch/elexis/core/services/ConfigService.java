package ch.elexis.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.jpa.entities.Config;
import ch.elexis.core.jpa.entities.Userconfig;
import ch.elexis.core.jpa.entities.UserconfigId;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.net.NetTool;
import jakarta.persistence.EntityManager;

@Component
public class ConfigService implements IConfigService {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	@Reference
	private IContextService contextService;

	@Reference
	private IAccessControlService accessControlService;

	public static final String LIST_SEPARATOR = ",";

	private Map<Object, LocalLock> managedLocks;

	// lazy initialized
	private ITraceService traceService;

	@Activate
	public void activate() {
		accessControlService.doPrivileged(() -> {

			validateConfiguredDatabaseLocale();

			managedLocks = new HashMap<>();

		});
	}

	@Deactivate
	public void deactivate() {
		if (traceService != null) {
			OsgiServiceUtil.ungetService(traceService);
		}
	}

	/**
	 * Every station has to run using the same locale to not mix any configuration
	 * strings (e.g. access rights).
	 */
	private void validateConfiguredDatabaseLocale() {
		Locale locale = Locale.getDefault();
		String dbStoredLocale = get(Preferences.CFG_LOCALE, null);
		if (dbStoredLocale == null || !locale.toString().equals(dbStoredLocale)) {
			LoggerFactory.getLogger(getClass()).error("System locale [{}] does not match database locale [{}].",
					locale.toString(), dbStoredLocale);
		}
	}

	@Override
	public boolean set(String key, String value) {
		return set(key, value, true);
	}

	@Override
	public boolean set(String key, String value, boolean addTraceEntry) {
		Optional<IConfig> entry = modelService.load(key, IConfig.class);

		if (value != null) {
			// never update an existing key, may differ due to mysql ignoring case on load,
			// JPA would create update and throw exception on update of primary key
			if (entry.isPresent()) {
				IConfig existing = entry.get();
				key = existing.getKey();
				if (Objects.equals(existing.getValue(), value)) {
					return false;
				}
				existing.setValue(value);
				if (addTraceEntry) {
					addTraceEntry("W globalCfg key [" + key + "] => value [" + value + "]");
				}
				modelService.save(existing);
				return true;
			} else {
				IConfig newEntry = modelService.create(IConfig.class);
				newEntry.setKey(key);
				newEntry.setValue(value);
				if (addTraceEntry) {
					addTraceEntry("W globalCfg key [" + key + "] => value [" + value + "]");
				}
				modelService.save(newEntry);
				return true;
			}
		} else {
			if (entry.isPresent()) {
				if (addTraceEntry) {
					addTraceEntry("W globalCfg key [" + key + "] => removed");
				}
				modelService.remove(entry.get());
				return true;
			}
		}
		return false;
	}

	private void addTraceEntry(String action) {
		if (traceService == null) {
			traceService = OsgiServiceUtil.getService(ITraceService.class).get();
		}
		String userId = contextService.getActiveUser().map(IUser::getId).orElse("unknown");
		traceService.addTraceEntry(userId, NetTool.hostname, action);
	}

	@Override
	public boolean set(String key, boolean value) {
		return set(key, (value) ? "1" : "0");
	}

	@Override
	public boolean set(String key, int value) {
		return set(key, Integer.toString(value));
	}

	@Override
	public boolean set(IContact contact, String key, int value) {
		return set(contact, key, Integer.toString(value));
	}

	@Override
	public boolean setFromList(String key, List<String> values) {
		String flattenedValue = values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(key, flattenedValue);
	}

	@Override
	public String get(String key, String defaultValue, boolean refreshCache) {
		Optional<IConfig> configEntry = modelService.load(key, IConfig.class, false, refreshCache);
		return configEntry.map(IConfig::getValue).orElse(defaultValue);
	}

	@Override
	public String getOrInsert(IContact contact, String key, Supplier<String> insertValue) {
		IElexisEntityManager elexisEntityManager = OsgiServiceUtil.getService(IElexisEntityManager.class, null).get();
		EntityManager entityManager = (EntityManager) elexisEntityManager.getEntityManager(false);
		String ret;
		try {
			entityManager.getTransaction().begin();

			if (contact == null) {
				Config config = entityManager.find(Config.class, key);
				if (config == null) {
					config = new Config();
					config.setParam(key);
					config.setWert(insertValue.get());
					entityManager.persist(config);
				}
				ret = config.getWert();
			} else {
				Userconfig userconfig = entityManager.find(Userconfig.class, new UserconfigId(contact.getId(), key));
				if (userconfig == null) {
					userconfig = new Userconfig();
					userconfig.setOwnerId(contact.getId());
					userconfig.setParam(key);
					userconfig.setValue(insertValue.get());
					entityManager.persist(userconfig);
				}
				ret = userconfig.getValue();
			}

			entityManager.getTransaction().commit();
			return ret;
		} finally {
			elexisEntityManager.closeEntityManager(entityManager);
		}

	}

	@Override
	public boolean get(String key, boolean defaultValue) {
		Optional<IConfig> configEntry = modelService.load(key, IConfig.class);
		return configEntry.map(IConfig::getValue).map(v -> "1".equals(v) || "true".equals(v)).orElse(defaultValue);
	}

	@Override
	public int get(String key, int defaultValue) {
		return Integer.parseInt(get(key, Integer.toString(defaultValue)));
	}

	@Override
	public List<String> getAsList(String key, List<String> defaultValue) {
		String val = get(key, null);
		if (val == null) {
			return defaultValue;
		}
		String[] split = val.split(LIST_SEPARATOR);
		return Stream.of(split).toList();
	}

	@Override
	public boolean set(IContact contact, String key, String value) {
		Optional<IUserConfig> loaded = Optional.empty();
		if (contact != null) {
			INamedQuery<IUserConfig> configQuery = modelService.getNamedQuery(IUserConfig.class, true, "ownerid",
					"param");
			List<IUserConfig> configs = configQuery
					.executeWithParameters(configQuery.getParameterMap("ownerid", contact.getId(), "param", key));
			if (!configs.isEmpty()) {
				if (configs.size() > 1) {
					LoggerFactory.getLogger(ConfigService.class)
							.warn("Multiple user config entries for [{}] using first.", key);
				}
				loaded = Optional.of(configs.get(0));
			}

			if (loaded.isPresent()) {
				if (value == null) {
					addTraceEntry("W userCfg [" + contact.getId() + "] key [" + key + "] => removed");
					modelService.remove(loaded.get());
					return true;
				}

				String _value = loaded.get().getValue();
				if (Objects.equals(value, _value)) {
					return true;
				}

				loaded.get().setValue(value);
				addTraceEntry("W userCfg [" + contact.getId() + "] key [" + key + "] => value [" + value + "]");
				modelService.save(loaded.get());
				return true;
			} else {
				if (value == null) {
					return false;
				}

				IUserConfig ret = modelService.create(IUserConfig.class);
				ret.setOwner(contact);
				ret.setKey(key);
				ret.setValue(value);
				addTraceEntry("W userCfg [" + contact.getId() + "] key [" + key + "] *> value [" + value + "]");
				modelService.save(ret);
				return true;
			}

		}
		return false;
	}

	@Override
	public boolean setActiveUserContact(String key, String value) {
		Optional<IContact> activeUser = contextService.getActiveUserContact();
		if (activeUser.isPresent()) {
			return set(activeUser.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active user available");
		}
		return false;
	}

	@Override
	public boolean setActiveUserContact(String key, boolean value) {
		Optional<IContact> activeUser = contextService.getActiveUserContact();
		if (activeUser.isPresent()) {
			return set(activeUser.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active user available");
		}
		return false;
	}

	@Override
	public boolean setActiveUserContact(String key, int value) {
		Optional<IContact> activeUser = contextService.getActiveUserContact();
		if (activeUser.isPresent()) {
			return set(activeUser.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active user available");
		}
		return false;
	}

	@Override
	public void setActiveUserContact(Map<Object, Object> map) {
		Optional<IContact> activeUser = contextService.getActiveUserContact();
		if (activeUser.isPresent()) {
			setFromMap(activeUser.get(), map);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active user available");
		}
	}

	@Override
	public boolean set(IContact contact, String key, boolean value) {
		return set(contact, key, (value) ? "1" : "0");
	}

	@Override
	public boolean setFromList(IContact contact, String key, List<String> values) {
		String flattenedValue = values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(contact, key, flattenedValue);
	}

	@Override
	public String get(IContact contact, String key, String defaultValue, boolean refreshCache) {
		if (contact != null) {

			INamedQuery<IUserConfig> configQuery = CoreModelServiceHolder.get().getNamedQuery(IUserConfig.class,
					refreshCache, "ownerid", "param");
			Optional<IUserConfig> config = configQuery.executeWithParametersSingleResult(
					configQuery.getParameterMap("ownerid", contact.getId(), "param", key));
			if (config.isPresent()) {
				return config.get().getValue();
			}
		}
		return defaultValue;
	}

	@Override
	public boolean get(IContact contact, String key, boolean defaultValue) {
		String value = get(contact, key, null);
		if (value != null) {
			return (value.equals("1") || value.equalsIgnoreCase(Boolean.TRUE.toString()));
		}
		return defaultValue;
	}

	@Override
	public int get(IContact contact, String key, int defaultValue) {
		return Integer.parseInt(get(contact, key, Integer.toString(defaultValue)));
	}

	@Override
	public List<String> getAsList(IContact contact, String key, List<String> defaultValue) {
		String val = get(contact, key, null);
		if (val == null) {
			return defaultValue;
		}
		String[] split = val.split(LIST_SEPARATOR);
		return Stream.of(split).toList();
	}

	@Override
	public List<String> getSubNodes(String key, boolean refreshCache) {
		Set<String> ret = new HashSet<>();
		IQuery<IConfig> query = CoreModelServiceHolder.get().getQuery(IConfig.class, refreshCache, false);
		query.and("param", COMPARATOR.LIKE, key + "/%");
		List<IConfig> found = query.execute();
		for (IConfig iConfig : found) {
			String subNode = iConfig.getKey().substring(key.length() + 1);
			if (StringUtils.isNotBlank(subNode)) {
				// add only nodes not values
				if (subNode.indexOf('/') != -1) {
					ret.add(subNode.substring(0, subNode.indexOf('/')));
				}
			}
		}
		return new ArrayList<>(ret);
	};

	@Override
	public Map<Object, Object> getActiveUserContactAsMap() {
		if (contextService != null) {
			Optional<IContact> activeUser = contextService.getActiveUserContact();
			if (activeUser.isPresent()) {
				return getAsMap(activeUser.get());
			}
		} else {
			LoggerFactory.getLogger(getClass())
					.warn("(getActiveUserContactAsMap) IContextService not available, returning null");
		}
		return null;
	}

	@Override
	public Map<Object, Object> getAsMap(IContact contact) {
		if (contact == null) {
			throw new IllegalArgumentException();
		}
		IQuery<IUserConfig> query = CoreModelServiceHolder.get().getQuery(IUserConfig.class);
		query.and("ownerid", COMPARATOR.EQUALS, contact.getId());
		List<IUserConfig> entries = query.execute();
		Map<Object, Object> ret = buildMap(entries);
		return ret;
	}

	private Map<Object, Object> buildMap(List<? extends IConfig> entries) {
		Hashtable<Object, Object> ret = new Hashtable<>();
		for (IConfig entry : entries) {
			buildMap(entry.getKey(), entry.getValue(), ret);
		}
		return ret;
	}

	/**
	 * Build a hierarchy of maps for the key, with values as leafs. As long as
	 * ch.elexis.data is in use {@link Hashtable} must be used as {@link Map}
	 * instances.
	 *
	 * @param key
	 * @param value
	 * @param map
	 */
	private void buildMap(String key, Object value, Map<Object, Object> map) {
		if (key.indexOf("/") != -1) {
			String currentKey = key.substring(0, key.indexOf("/"));
			@SuppressWarnings("unchecked")
			Hashtable<Object, Object> subMap = (Hashtable<Object, Object>) map.get(currentKey);
			if (subMap == null) {
				subMap = new Hashtable<>();
				map.put(currentKey, subMap);
			}
			buildMap(key.substring(key.indexOf("/") + 1), value, subMap);
		} else {
			map.put(key, value);
		}
	}

	@Override
	public void setFromMap(IContact contact, Map<Object, Object> map) {
		map = flattenMap(map);
		map.forEach((k, v) -> {
			set(contact, (String) k, (String) v);
		});
	}

	/**
	 * Flatten the map to keys of type String matching the param column in the db.
	 *
	 * @param map
	 * @return
	 */
	private Map<Object, Object> flattenMap(Map<Object, Object> map) {
		Map<Object, Object> ret = new HashMap<>();
		flattenMap(map, ret, StringUtils.EMPTY);
		return ret;
	}

	@SuppressWarnings("unchecked")
	private void flattenMap(Map<Object, Object> src, Map<Object, Object> dest, String parentkey) {
		for (Object key : src.keySet()) {
			String currentKey = StringUtils.isEmpty(parentkey) ? (String) key : parentkey + "/" + key;
			if (src.get(key) instanceof Map) {
				flattenMap((Map<Object, Object>) src.get(key), dest, currentKey);
			} else {
				dest.put(currentKey, src.get(key));
			}
		}
	}

	@Override
	public String getActiveMandator(String key, String defaultValue, boolean refreshCache) {
		if (contextService != null) {
			Optional<IMandator> activeMandator = contextService.getActiveMandator();
			if (activeMandator.isPresent()) {
				return get(activeMandator.get(), key, defaultValue, refreshCache);
			}
		} else {
			LoggerFactory.getLogger(getClass())
					.warn("(getActiveMandator) IContextService not available, returning defaultValue for [{}]", key);
		}
		return defaultValue;
	}

	@Override
	public void setActiveMandator(String key, String value) {
		Optional<IMandator> activeMandator = contextService.getActiveMandator();
		if (activeMandator.isPresent()) {
			set(activeMandator.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active mandator available");
		}
	}

	@Override
	public void setActiveMandator(String key, boolean value) {
		Optional<IMandator> activeMandator = contextService.getActiveMandator();
		if (activeMandator.isPresent()) {
			set(activeMandator.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active mandator available");
		}
	}

	@Override
	public String getActiveUserContact(String key, String defaultValue, boolean refreshCache) {
		if (contextService != null) {
			Optional<IContact> activeUser = contextService.getActiveUserContact();
			if (activeUser.isPresent()) {
				return get(activeUser.get(), key, defaultValue, refreshCache);
			}
		} else {
			LoggerFactory.getLogger(getClass())
					.warn("(getActiveUserContact) IContextService not available, returning defaultValue for [{}]", key);
		}
		return defaultValue;
	}

	@Override
	public boolean getActiveMandator(String key, boolean defaultValue) {
		String defaultValueString = Boolean.toString(defaultValue);
		String result = getActiveMandator(key, defaultValueString);
		return (result.equals("1") || result.equalsIgnoreCase(Boolean.TRUE.toString()));
	}

	@Override
	public boolean getActiveUserContact(String key, boolean defaultValue) {
		String defaultValueString = Boolean.toString(defaultValue);
		String result = getActiveUserContact(key, defaultValueString);
		return (result.equals("1") || result.equalsIgnoreCase(Boolean.TRUE.toString()));
	}

	@Override
	public int getActiveUserContact(String key, int defaultValue) {
		if (contextService != null) {
			Optional<IContact> activeUser = contextService.getActiveUserContact();
			if (activeUser.isPresent()) {
				return get(activeUser.get(), key, defaultValue);
			}
		} else {
			LoggerFactory.getLogger(getClass())
					.warn("(getActiveUserContact) IContextService not available, returning defaultValue for [{}]", key);
		}
		return defaultValue;
	}

	@Override
	public int getActiveMandator(String key, int defaultValue) {
		if (contextService != null) {
			Optional<IMandator> activeMandator = contextService.getActiveMandator();
			if (activeMandator.isPresent()) {
				return get(activeMandator.get(), key, defaultValue);
			}
		} else {
			LoggerFactory.getLogger(getClass())
					.warn("(getActiveMandator) IContextService not available, returning defaultValue for [{}]", key);
		}
		return defaultValue;
	}

	@Override
	public ILocalLock getLocalLock(Object object) {
		return new LocalLock(object);
	}

	@Override
	public Optional<ILocalLock> getManagedLock(Object object) {
		return Optional.ofNullable(managedLocks.get(object));
	}

	private class LocalLock implements ILocalLock {

		private String lockString;
		private Object lockObject;

		public LocalLock(Object object) {
			this.lockObject = object;
			if (object instanceof String) {
				lockString = "local_" + (String) object + "_lock";
			} else if (object instanceof Identifiable) {
				String storeToString = StoreToStringServiceHolder.getStoreToString(object);
				lockString = "local_" + storeToString + "_lock"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				throw new IllegalStateException("Unknown object type [" + object + "]");
			}
		}

		@Override
		public String getLockMessage() {
			Optional<IConfig> configEntry = modelService.load(lockString, IConfig.class);
			if (configEntry.isPresent() && configEntry.get().getValue() != null) {
				String[] parts = configEntry.get().getValue().split("@");
				if (parts.length > 0) {
					return parts[0];
				}
			}
			return "?"; //$NON-NLS-1$
		}

		@Override
		public long getLockCurrentMillis() {
			Optional<IConfig> configEntry = modelService.load(lockString, IConfig.class);
			if (configEntry.isPresent() && configEntry.get().getValue() != null) {
				String[] parts = configEntry.get().getValue().split("@");
				if (parts.length > 1) {
					return Long.parseLong(parts[1]);
				}
			}
			return -1; // $NON-NLS-1$
		}

		@Override
		public void unlock() {
			synchronized (LocalLock.class) {
				Optional<IConfig> configEntry = modelService.load(lockString, IConfig.class);
				if (configEntry.isPresent()) {
					modelService.remove(configEntry.get());
				}
			}
		}

		@Override
		public boolean hasLock(String userName) {
			synchronized (LocalLock.class) {
				Optional<IConfig> configEntry = modelService.load(lockString, IConfig.class);
				if (configEntry.isPresent()) {
					return configEntry.get().getValue().startsWith("[" + userName + "]@");
				}
				return false;
			}
		}

		@Override
		public boolean tryLock() {
			synchronized (LocalLock.class) {
				Optional<IConfig> configEntry = modelService.load(lockString, IConfig.class);
				if (configEntry.isPresent()) {
					return false;
				} else {
					String user = "system";
					Optional<IContact> activeUserContact = ContextServiceHolder.get().getActiveUserContact();
					if (activeUserContact.isPresent()) {
						user = activeUserContact.get().getLabel();
					}
					IConfig _entry = modelService.create(IConfig.class);
					_entry.setKey(lockString);
					_entry.setValue("[" + user + "]@" + System.currentTimeMillis());
					modelService.save(_entry);
					managedLocks.put(lockObject, this);
					return true;
				}
			}
		}
	}
}
