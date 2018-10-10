package ch.elexis.core.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IConfig;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.services.holder.CoreModelServiceHolder;

@Component
public class ConfigService implements IConfigService {
	
	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;
	
	public static final String LIST_SEPARATOR = ",";
	
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
	public boolean setFromList(String key, List<String> values){
		String flattenedValue =
			values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(key, flattenedValue);
	}
	
	@Override
	public String get(String key, String defaultValue){
		Optional<IConfig> configEntry = modelService.load(key, IConfig.class);
		return configEntry.map(v -> v.getValue()).orElse(defaultValue);
	}
	
	@Override
	public boolean get(String key, boolean defaultValue){
		Optional<IConfig> configEntry = modelService.load(key, IConfig.class);
		return configEntry.map(v -> Boolean.parseBoolean(v.getValue())).orElse(defaultValue);
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
			List<IUserConfig> configs = configQuery.executeWithParameters(CoreModelServiceHolder
				.get().getParameterMap("ownerid", contact.getId(), "param", key));
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
			List<IUserConfig> configs = configQuery.executeWithParameters(CoreModelServiceHolder
				.get().getParameterMap("ownerid", contact.getId(), "param", key));
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
	
}
