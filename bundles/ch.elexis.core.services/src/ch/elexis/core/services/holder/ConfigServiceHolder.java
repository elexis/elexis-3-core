package ch.elexis.core.services.holder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IConfigService;

@Component
public class ConfigServiceHolder {
	
	private static IConfigService configService;
	
	@Reference
	public void setModelService(IConfigService modelService){
		ConfigServiceHolder.configService = modelService;
	}
	
	public static IConfigService get(){
		if (configService == null) {
			throw new IllegalStateException("No IConfigService available");
		}
		return configService;
	}
	
	// global access methods
	
	public static String getGlobal(String key, String defaultValue){
		return configService.get(key, defaultValue);
	}
	
	public static int getGlobal(String key, int defaultValue){
		return configService.get(key, defaultValue);
	}
	
	public static boolean getGlobal(String key, boolean defaultValue){
		return configService.get(key, defaultValue);
	}
	
	public static List<String> getGlobalAsList(String key){
		String string = getGlobal(key, (String) null);
		if (string != null) {
			String[] split = string.split(",");
			if (split != null && split.length > 0) {
				return Arrays.asList(split);
			}
		}
		return Collections.emptyList();
	}
	
	public static String[] getGlobalStringArray(String key){
		String raw = getGlobal(key, null);
		if (StringUtils.isBlank(raw)) {
			return null;
		}
		return raw.split(",");
	}
	
	public static boolean setGlobal(String key, String value){
		return configService.set(key, value);
	}
	
	public static boolean setGlobal(String key, boolean value){
		return configService.set(key, value);
	}
	
	public static void setGlobalAsList(String key, List<String> values){
		Optional<String> value =
			values.stream().map(o -> o.toString()).reduce((u, t) -> u + "," + t);
		if (value.isPresent()) {
			configService.set(key, value.get());
		} else {
			configService.set(key, null);
		}
	}
	
	public static boolean setGlobal(String key, int value){
		return configService.set(key, value);
	}
	
	// active user access methods
	
	public static String getActiveUser(String key, String defaultValue){
		return configService.getActiveUserContact(key, defaultValue);
	}
	
	// active mandator access methods
	
	public static String getActiveMandator(String key, String defaultValue){
		return configService.getActiveMandator(key, defaultValue);
	}
}
