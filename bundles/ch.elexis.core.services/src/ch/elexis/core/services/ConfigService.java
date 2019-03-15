package ch.elexis.core.services;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.equinox.internal.app.CommandLineArgs;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.Desk;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.utils.CoreUtil;
import ch.rgw.io.Settings;
import ch.rgw.io.SysSettings;

@Component
public class ConfigService implements IConfigService {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	public static final String LIST_SEPARATOR = ",";
	
	private Settings localConfig;
	
	private Map<Object, LocalLock> managedLocks;
	
	@Activate
	public void activate(){
		validateConfiguredDatabaseLocale();
		
		SysSettings cfg = new SysSettings(SysSettings.USER_SETTINGS, Desk.class);
		cfg.read_xml(CoreUtil.getWritableUserDir() + File.separator + getLocalConfigFileName());
		localConfig = cfg;
		
		managedLocks = new HashMap<>();
	}
	
	@Deactivate
	public void deactivate(){
		SysSettings localCfg = (SysSettings) localConfig;
		localCfg
			.write_xml(CoreUtil.getWritableUserDir() + File.separator + getLocalConfigFileName());
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
		Optional<IConfig> entry = modelService.load(key, IConfig.class);
		if (value != null) {
			IConfig _entry = entry.orElse(modelService.create(IConfig.class));
			_entry.setKey(key);
			_entry.setValue(value);
			return modelService.save(_entry);
		} else {
			if (entry.isPresent()) {
				return modelService.remove(entry.get());
			}
		}
		return false;
	}
	
	@Override
	public boolean set(String key, boolean value){
		return set(key, (value) ? "1" : "0");
	}
	
	@Override
	public boolean setFromList(String key, List<String> values){
		String flattenedValue =
			values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(key, flattenedValue);
	}
	
	@Override
	public String get(String key, String defaultValue){
		Optional<IConfig> configEntry = modelService.load(key, IConfig.class);
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
				return CoreModelServiceHolder.get().save(config);
			} else {
				if (loaded.isPresent()) {
					return CoreModelServiceHolder.get().remove(loaded.get());
				}
			}
		}
		return false;
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
	public String get(IContact contact, String key, String defaultValue){
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
			return Boolean.parseBoolean(value);
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
	
	@Override
	public boolean setLocal(String key, String value){
		return localConfig.set(key, value);
	}
	
	@Override
	public boolean setLocal(String key, boolean value){
		localConfig.set(key, value);
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
