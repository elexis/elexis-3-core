package ch.elexis.core.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.equinox.internal.app.CommandLineArgs;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.LoggerFactory;

import ch.elexis.Desk;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
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
import ch.elexis.core.utils.CoreUtil;
import ch.rgw.io.Settings;
import ch.rgw.io.SysSettings;
import ch.rgw.tools.net.NetTool;

@Component
public class ConfigService implements IConfigService {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	@Reference(cardinality = ReferenceCardinality.OPTIONAL, policyOption = ReferencePolicyOption.GREEDY)
	private IContextService contextService;
	
	public static final String LIST_SEPARATOR = ",";
	
	private Settings localConfig;
	
	private Map<Object, LocalLock> managedLocks;
	
	private ExecutorService traceExecutor;
	
	@Activate
	public void activate(){
		validateConfiguredDatabaseLocale();
		
		SysSettings cfg = SysSettings.getOrCreate(SysSettings.USER_SETTINGS, Desk.class);
		cfg.read_xml(CoreUtil.getWritableUserDir() + File.separator + getLocalConfigFileName());
		localConfig = cfg;
		
		managedLocks = new HashMap<>();
		
		traceExecutor = Executors.newSingleThreadExecutor();
	}
	
	@Deactivate
	public void deactivate(){
		SysSettings localCfg = (SysSettings) localConfig;
		localCfg
			.write_xml(CoreUtil.getWritableUserDir() + File.separator + getLocalConfigFileName());
		
		traceExecutor.shutdown();
	}
	
	private String getLocalConfigFileName(){
		String[] args = CommandLineArgs.getApplicationArgs();
		String config = "default"; //$NON-NLS-1$
		for (String s : args) {
			if (s.startsWith("--use-config=")) { //$NON-NLS-1$
				String[] c = s.split("="); //$NON-NLS-1$
				config = c[1];
			}
		}
		if (ElexisSystemPropertyConstants.RUN_MODE_FROM_SCRATCH
			.equals(System.getProperty(ElexisSystemPropertyConstants.RUN_MODE))) {
			config = UUID.randomUUID().toString();
		}
		return "localCfg_" + config + ".xml";
	}
	
	/**
	 * Every station has to run using the same locale to not mix any configuration strings (e.g.
	 * access rights).
	 */
	private void validateConfiguredDatabaseLocale(){
		Locale locale = Locale.getDefault();
		String dbStoredLocale = get(Preferences.CFG_LOCALE, null);
		if (dbStoredLocale == null || !locale.toString().equals(dbStoredLocale)) {
			LoggerFactory.getLogger(getClass()).error(
				"System locale [{}] does not match database locale [{}].", locale.toString(),
				dbStoredLocale);
		}
	}
	
	@Override
	public boolean set(String key, String value){
		return set(key, value, true);
	}
	
	@Override
	public boolean set(String key, String value, boolean addTraceEntry){
		Optional<IConfig> entry = modelService.load(key, IConfig.class);
		if (value != null) {
			IConfig _entry = entry.orElse(modelService.create(IConfig.class));
			_entry.setKey(key);
			_entry.setValue(value);
			if (addTraceEntry) {
				addTraceEntry("W globalCfg key [" + key + "] => value [" + value + "]");
			}
			
			return modelService.save(_entry);
		} else {
			if (entry.isPresent()) {
				if (addTraceEntry) {
					addTraceEntry("W globalCfg key [" + key + "] => removed");
				}
				return modelService.remove(entry.get());
			}
		}
		return false;
	}
	
	private void addTraceEntry(String action){
		traceExecutor.execute(()-> {
			String username = "unknown";
			if (ContextServiceHolder.isAvailable()) {
				IUser user = ContextServiceHolder.get().getActiveUser().orElse(null);
				if (user != null) {
					username = StringUtils.abbreviate(user.getId(), 30);
				}
			}
			
			String workstation = "unknown";
			if (StringUtils.isEmpty(workstation)) {
				workstation = StringUtils.abbreviate(NetTool.hostname, 40);
			}
			String _action = (StringUtils.isEmpty(action)) ? "" : action;
			
			String insertStatement =
				"INSERT INTO TRACES (logtime, workstation, username, action) VALUES("
					+ System.currentTimeMillis() + ", '" + workstation + "', '" + username + "', '"
					+ _action + "')";
			
			modelService.executeNativeUpdate(insertStatement, false);
		});
	}
	
	@Override
	public boolean set(String key, boolean value){
		return set(key, (value) ? "1" : "0");
	}
	
	@Override
	public boolean set(String key, int value){
		return set(key, Integer.toString(value));
	}
	
	@Override
	public boolean set(IContact contact, String key, int value){
		return set(contact, key, Integer.toString(value));
	}
	
	@Override
	public boolean setFromList(String key, List<String> values){
		String flattenedValue =
			values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(key, flattenedValue);
	}
	
	@Override
	public String get(String key, String defaultValue, boolean refreshCache){
		Optional<IConfig> configEntry = modelService.load(key, IConfig.class, false, refreshCache);
		return configEntry.map(IConfig::getValue).orElse(defaultValue);
	}
	
	@Override
	public boolean get(String key, boolean defaultValue){
		Optional<IConfig> configEntry = modelService.load(key, IConfig.class);
		return configEntry.map(IConfig::getValue).map(v -> "1".equals(v) || "true".equals(v)).orElse(defaultValue);
	}
	
	@Override
	public int get(String key, int defaultValue){
		return Integer.parseInt(get(key, Integer.toString(defaultValue)));
	}
	
	@Override
	public List<String> getAsList(String key, List<String> defaultValue){
		String val = get(key, null);
		if (val != null) {
			String[] split = val.split(LIST_SEPARATOR);
			return Arrays.asList(split).stream().collect(Collectors.toList());
		}
		return defaultValue;
	}
	
	@Override
	public boolean set(IContact contact, String key, String value){
		Optional<IUserConfig> loaded = Optional.empty();
		if (contact != null) {
			INamedQuery<IUserConfig> configQuery = CoreModelServiceHolder.get()
				.getNamedQuery(IUserConfig.class, true, "ownerid", "param");
			List<IUserConfig> configs = configQuery.executeWithParameters(
				configQuery.getParameterMap("ownerid", contact.getId(), "param", key));
			if (!configs.isEmpty()) {
				if (configs.size() > 1) {
					LoggerFactory.getLogger(ConfigService.class)
						.warn("Multiple user config entries for [" + key + "] using first.");
				}
				loaded = Optional.of(configs.get(0));
			}
			if (value != null) {
				IUserConfig config = loaded.orElseGet(() -> {
					IUserConfig ret = CoreModelServiceHolder.get().create(IUserConfig.class);
					ret.setOwner(contact);
					ret.setKey(key);
					return ret;
				});
				config.setValue(value);
				addTraceEntry("W userCfg [" + contact.getId() + "] key [" + key + "] => value ["
					+ value + "]");
				return CoreModelServiceHolder.get().save(config);
			} else {
				if (loaded.isPresent()) {
					addTraceEntry(
						"W userCfg [" + contact.getId() + "] key [" + key + "] => removed");
					return CoreModelServiceHolder.get().remove(loaded.get());
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean setActiveUserContact(String key, String value){
		Optional<IContact> activeUser = contextService.getActiveUserContact();
		if (activeUser.isPresent()) {
			return set(activeUser.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active user available");
		}
		return false;
	}
	
	@Override
	public boolean setActiveUserContact(String key, boolean value){
		Optional<IContact> activeUser = contextService.getActiveUserContact();
		if (activeUser.isPresent()) {
			return set(activeUser.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active user available");
		}
		return false;
	}
	
	@Override
	public boolean setActiveUserContact(String key, int value){
		Optional<IContact> activeUser = contextService.getActiveUserContact();
		if (activeUser.isPresent()) {
			return set(activeUser.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active user available");
		}
		return false;
	}
	
	@Override
	public void setActiveUserContact(Map<Object, Object> map){
		Optional<IContact> activeUser = contextService.getActiveUserContact();
		if (activeUser.isPresent()) {
			setFromMap(activeUser.get(), map);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active user available");
		}
	}
	
	@Override
	public boolean set(IContact contact, String key, boolean value){
		return set(contact, key, (value) ? "1" : "0");
	}
	
	@Override
	public boolean setFromList(IContact contact, String key, List<String> values){
		String flattenedValue =
			values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(contact, key, flattenedValue);
	}
	
	@Override
	public String get(IContact contact, String key, String defaultValue, boolean refreshCache){
		if (contact != null) {
			INamedQuery<IUserConfig> configQuery = CoreModelServiceHolder.get()
				.getNamedQuery(IUserConfig.class, refreshCache, "ownerid", "param");
			List<IUserConfig> configs = configQuery.executeWithParameters(
				configQuery.getParameterMap("ownerid", contact.getId(), "param", key));
			if (!configs.isEmpty()) {
				if (configs.size() > 1) {
					LoggerFactory.getLogger(ConfigService.class)
						.warn("Multiple user config entries for [" + key + "] using first.");
				}
				String value = configs.get(0).getValue();
				return value != null ? value : defaultValue;
			}
		}
		return defaultValue;
	}
	
	@Override
	public boolean get(IContact contact, String key, boolean defaultValue){
		String value = get(contact, key, null);
		if (value != null) {
			return (value.equals("1") || value.equalsIgnoreCase(Boolean.TRUE.toString()));
		}
		return defaultValue;
	}
	
	@Override
	public int get(IContact contact, String key, int defaultValue){
		return Integer.parseInt(get(contact, key, Integer.toString(defaultValue)));
	}
	
	@Override
	public List<String> getAsList(IContact contact, String key, List<String> defaultValue){
		String val = get(contact, key, null);
		if (val != null) {
			String[] split = val.split(LIST_SEPARATOR);
			return Arrays.asList(split).stream().collect(Collectors.toList());
		}
		return defaultValue;
	}
	
	public List<String> getSubNodes(String key){
		Set<String> ret = new HashSet<>();
		IQuery<IConfig> query = CoreModelServiceHolder.get().getQuery(IConfig.class);
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
	public Map<Object, Object> getActiveUserContactAsMap(){
		if (contextService != null) {
			Optional<IContact> activeUser = contextService.getActiveUserContact();
			if (activeUser.isPresent()) {
				return getAsMap(activeUser.get());
			}
		} else {
			LoggerFactory.getLogger(getClass())
				.warn("IContextService not available, returning defaultValue");
		}
		return null;
	}
	
	@Override
	public Map<Object, Object> getAsMap(IContact contact){
		IQuery<IUserConfig> query = CoreModelServiceHolder.get().getQuery(IUserConfig.class);
		query.and("ownerid", COMPARATOR.EQUALS, contact.getId());
		List<IUserConfig> entries = query.execute();
		Map<Object, Object> ret = buildMap(entries);
		return ret;
	}
	
	private Map<Object, Object> buildMap(List<IUserConfig> entries){
		Hashtable<Object, Object> ret = new Hashtable<Object, Object>();
		for (IUserConfig iUserConfig : entries) {
			buildMap(iUserConfig.getKey(), iUserConfig.getValue(), ret);
		}
		return ret;
	}
	
	/**
	 * Build a hierarchy of maps for the key, with values as leafs. As long as ch.elexis.data is in
	 * use {@link Hashtable} must be used as {@link Map} instances.
	 * 
	 * @param key
	 * @param value
	 * @param map
	 */
	private void buildMap(String key, Object value, Map<Object, Object> map){
		if (key.indexOf("/") != -1) {
			String currentKey = key.substring(0, key.indexOf("/"));
			@SuppressWarnings("unchecked")
			Hashtable<Object, Object> subMap = (Hashtable<Object, Object>) map.get(currentKey);
			if (subMap == null) {
				subMap = new Hashtable<Object, Object>();
				map.put(currentKey, subMap);
			}
			buildMap(key.substring(key.indexOf("/") + 1), value, subMap);
		} else {
			map.put(key, value);
		}
	}
	
	@Override
	public void setFromMap(IContact contact, Map<Object, Object> map){
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
	private Map<Object, Object> flattenMap(Map<Object, Object> map){
		Map<Object, Object> ret = new HashMap<>();
		flattenMap(map, ret, "");
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	private void flattenMap(Map<Object, Object> src, Map<Object, Object> dest, String parentkey){
		for (Object key : src.keySet()) {
			String currentKey =
				StringUtils.isEmpty(parentkey) ? (String) key : parentkey + "/" + key;
			if (src.get(key) instanceof Map) {
				flattenMap((Map<Object, Object>) src.get(key), dest, currentKey);
			} else {
				dest.put(currentKey, src.get(key));
			}
		}
	}
	
	@Override
	public boolean setLocal(String key, String value){
		boolean result = localConfig.set(key, value);
		localConfig.flush();
		return result;
	}
	
	@Override
	public String getActiveMandator(String key, String defaultValue, boolean refreshCache){
		if (contextService != null) {
			Optional<IMandator> activeMandator = contextService.getActiveMandator();
			if (activeMandator.isPresent()) {
				return get(activeMandator.get(), key, defaultValue, refreshCache);
			}
		} else {
			LoggerFactory.getLogger(getClass()).warn("IContextService not available, returning defaultValue");
		}
		return defaultValue;
	}
	
	@Override
	public void setActiveMandator(String key, String value){
		Optional<IMandator> activeMandator = contextService.getActiveMandator();
		if (activeMandator.isPresent()) {
			set(activeMandator.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass())
				.warn("No active mandator available");
		}
	}
	
	@Override
	public void setActiveMandator(String key, boolean value){
		Optional<IMandator> activeMandator = contextService.getActiveMandator();
		if (activeMandator.isPresent()) {
			set(activeMandator.get(), key, value);
		} else {
			LoggerFactory.getLogger(getClass()).warn("No active mandator available");
		}
	}
	
	@Override
	public String getActiveUserContact(String key, String defaultValue, boolean refreshCache){
		if (contextService != null) {
			Optional<IContact> activeUser = contextService.getActiveUserContact();
			if (activeUser.isPresent()) {
				return get(activeUser.get(), key, defaultValue, refreshCache);
			}
		} else {
			LoggerFactory.getLogger(getClass()).warn("IContextService not available, returning defaultValue");
		}
		return defaultValue;
	}
	
	@Override
	public boolean setLocal(String key, boolean value){
		boolean result =  localConfig.set(key, value);
		localConfig.flush();
		return result;
	}
	
	@Override
	public boolean setLocal(String key, int value){
		localConfig.set(key, value);
		localConfig.flush();
		return true;
	}
	
	@Override
	public String getLocal(String key, String defaultValue){
		return localConfig.get(key, defaultValue);
	}
	
	@Override
	public boolean getLocal(String key, boolean defaultValue){
		return localConfig.get(key, defaultValue);
	}
	
	@Override
	public int getLocal(String key, int defaultValue){
		return localConfig.get(key, defaultValue);
	}
	
	public boolean getActiveMandator(String key, boolean defaultValue) {
		String defaultValueString = Boolean.toString(defaultValue);
		String result = getActiveMandator(key, defaultValueString);
		return (result.equals("1") || result.equalsIgnoreCase(Boolean.TRUE.toString()));
	}
	
	@Override
	public boolean getActiveUserContact(String key, boolean defaultValue){
		String defaultValueString = Boolean.toString(defaultValue);
		String result = getActiveUserContact(key, defaultValueString);
		return (result.equals("1") || result.equalsIgnoreCase(Boolean.TRUE.toString()));
	}
	
	@Override
	public int getActiveUserContact(String key, int defaultValue){
		if (contextService != null) {
			Optional<IContact> activeUser = contextService.getActiveUserContact();
			if (activeUser.isPresent()) {
				return get(activeUser.get(), key, defaultValue);
			}
		} else {
			LoggerFactory.getLogger(getClass())
				.warn("IContextService not available, returning defaultValue");
		}
		return defaultValue;
	}
	
	@Override
	public int getActiveMandator(String key, int defaultValue){
		if (contextService != null) {
			Optional<IMandator> activeMandator = contextService.getActiveMandator();
			if (activeMandator.isPresent()) {
				return get(activeMandator.get(), key, defaultValue);
			}
		} else {
			LoggerFactory.getLogger(getClass())
				.warn("IContextService not available, returning defaultValue");
		}
		return defaultValue;
	}
	
	@Override
	public ILocalLock getLocalLock(Object object){
		return new LocalLock(object);
	}
	
	@Override
	public Optional<ILocalLock> getManagedLock(Object object){
		return Optional.ofNullable(managedLocks.get(object));
	}
	
	private class LocalLock implements ILocalLock {
		
		private String lockString;
		private Object lockObject;
		
		public LocalLock(Object object){
			this.lockObject = object;
			if (object instanceof String) {
				lockString = "local_" + (String) object + "_lock";
			} else if (object instanceof Identifiable) {
				String storeToString =
					StoreToStringServiceHolder.getStoreToString((Identifiable) object);
				lockString = "local_" + storeToString + "_lock"; //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				throw new IllegalStateException("Unknown object type [" + object + "]");
			}
		}
		
		@Override
		public String getLockMessage(){
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
		public long getLockCurrentMillis(){
			Optional<IConfig> configEntry = modelService.load(lockString, IConfig.class);
			if (configEntry.isPresent() && configEntry.get().getValue() != null) {
				String[] parts = configEntry.get().getValue().split("@");
				if (parts.length > 1) {
					return Long.parseLong(parts[1]);
				}
			}
			return -1; //$NON-NLS-1$
		}
		
		@Override
		public void unlock(){
			synchronized (LocalLock.class) {
				Optional<IConfig> configEntry = modelService.load(lockString, IConfig.class);
				if (configEntry.isPresent()) {
					modelService.remove(configEntry.get());
				}
			}
		}
		
		@Override
		public boolean hasLock(String userName){
			synchronized (LocalLock.class) {
				Optional<IConfig> configEntry = modelService.load(lockString, IConfig.class);
				if (configEntry.isPresent()) {
					return configEntry.get().getValue().startsWith("[" + userName + "]@");
				}
				return false;
			}
		}
		
		@Override
		public boolean tryLock(){
			synchronized (LocalLock.class) {
				Optional<IConfig> configEntry = modelService.load(lockString, IConfig.class);
				if(configEntry.isPresent()) {
					return false;
				} else {
					String user = "system";
					Optional<IContact> activeUserContact = ContextServiceHolder.get().getActiveUserContact();
					if(activeUserContact.isPresent()) {
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
