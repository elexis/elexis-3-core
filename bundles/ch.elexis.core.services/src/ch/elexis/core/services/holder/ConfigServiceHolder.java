package ch.elexis.core.services.holder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
	
	public static boolean isPresent(){
		return configService != null;
	}
	
	// global access methods
	
	public static String getGlobal(String key, String defaultValue){
		return configService.get(key, defaultValue);
	}
	
	public static String getGlobalCached(String key, String defaultValue){
		return configService.get(key, defaultValue, false);
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
	
	public static List<String> getSubNodes(String key){
		return configService.getSubNodes(key);
	}
	
	// active user access methods
	
	public static String getUser(String key, String defaultValue){
		return configService.getActiveUserContact(key, defaultValue);
	}
	
	public static String getUserCached(String key, String defaultValue){
		return configService.getActiveUserContact(key, defaultValue, false);
	}
	
	public static boolean getUser(String key, boolean defaultValue){
		return configService.getActiveUserContact(key, defaultValue);
	}
	
	public static Integer getUser(String key, int defaultValue){
		return configService.getActiveUserContact(key, defaultValue);
	}
	
	public static boolean setUser(String key, String value){
		return configService.setActiveUserContact(key, value);
	}
	
	public static boolean setUser(String key, boolean value){
		return configService.setActiveUserContact(key, value);
	}
	
	public static boolean setUser(String key, int value){
		return configService.setActiveUserContact(key, value);
	}
	
	public static List<String> getUserAsList(String key){
		String string = getUser(key, (String) null);
		if (string != null) {
			String[] split = string.split(",");
			if (split != null && split.length > 0) {
				return Arrays.asList(split);
			}
		}
		return Collections.emptyList();
	}
	
	public static void setUserAsList(String key, List<String> values){
		Optional<String> value =
			values.stream().map(o -> o.toString()).reduce((u, t) -> u + "," + t);
		if (value.isPresent()) {
			configService.setActiveUserContact(key, value.get());
		} else {
			configService.setActiveUserContact(key, null);
		}
	}
	
	public static void setUserFromMap(Map<Object, Object> map){
		configService.setActiveUserContact(map);
	}
	
	public static Map<Object, Object> getUserAsMap(){
		return configService.getActiveUserContactAsMap();
	}
	
	// active mandator access methods
	
	public static String getMandator(String key, String defaultValue){
		return configService.getActiveMandator(key, defaultValue);
	}
	
	public static String getMandatorCached(String key, String defaultValue){
		return configService.getActiveMandator(key, defaultValue, false);
	}
	
	public static boolean getMandator(String key, boolean defaultValue){
		return configService.getActiveMandator(key, defaultValue);
	}
	
	public static int getMandator(String key, int defaultValue){
		return configService.getActiveMandator(key, defaultValue);
	}
	
	public static void setMandator(String key, String value){
		configService.setActiveMandator(key, value);
	}
	
	public static void setMandator(String key, boolean value){
		configService.setActiveMandator(key, value);
	}
	
	public static List<String> getMandatorAsList(String key){
		String string = getMandator(key, (String) null);
		if (string != null) {
			String[] split = string.split(",");
			if (split != null && split.length > 0) {
				return Arrays.asList(split);
			}
		}
		return Collections.emptyList();
	}
	
	// local access methods
	
	public static boolean getLocal(String key, boolean defaultValue){
		return configService.getLocal(key, defaultValue);
	}
	
	public static String getLocal(String key, String defaultValue){
		return configService.getLocal(key, defaultValue);
	}
}
