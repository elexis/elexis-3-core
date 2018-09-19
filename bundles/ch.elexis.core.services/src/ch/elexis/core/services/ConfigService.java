package ch.elexis.core.services;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

import ch.elexis.core.model.IConfig;

@Component
public class ConfigService implements IConfigService {
	
	@Reference(cardinality = ReferenceCardinality.MANDATORY)
	private IModelService modelService;
	
	public static final String LIST_SEPARATOR = ",";
	
	@Override
	public boolean set(String key, String value){
		IConfig entry =
			modelService.load(key, IConfig.class).orElse(modelService.create(IConfig.class));
		entry.setKey(key);
		entry.setValue(value);
		return modelService.save(entry);
	}
	
	@Override
	public boolean setFromSet(String key, Set<String> values){
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
	public Set<String> getAsSet(String key, Set<String> defaultValue){
		String val = get(key, null);
		if (val != null) {
			String[] split = val.split(LIST_SEPARATOR);
			return Arrays.asList(split).stream().collect(Collectors.toSet());
		}
		return defaultValue;
	}
	
}
