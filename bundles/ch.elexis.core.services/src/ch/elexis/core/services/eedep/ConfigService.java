package ch.elexis.core.services.eedep;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IContact;
import ch.elexis.core.services.ConfigServiceActivator;
import ch.elexis.core.services.IConfigService;
import ch.elexis.core.services.IContextService;
import ch.myelexis.server.api.UserApi;
import ch.myelexis.server.client.ApiException;

@Component(name = ConfigServiceActivator.EEDEP, enabled = false)
public class ConfigService implements IConfigService {

	@Reference
	UserApi userApi;

	@Reference
	IContextService contextService;

	// FIXME set operation should invalidate cache

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String LIST_SEPARATOR = ",";

	@Override
	public String get(String key, String defaultValue, boolean refreshCache) {
		try {
			String _value = userApi.getGlobalConfigurationValueByKey(key);
			return Objects.toString(_value, defaultValue);
		} catch (ApiException e) {
			logger.error("Could not get key " + key, e);
		}
		return defaultValue;
	}

	@Override
	public boolean get(String key, boolean defaultValue) {
		String v = get(key, Boolean.toString(defaultValue), false);
		return "1".equals(v) || "true".equals(v);
	}

	@Override
	public int get(String key, int defaultValue) {
		String v = get(key, Integer.toString(defaultValue), false);
		return Integer.parseInt(v);
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
	public String getActiveUserContact(String key, String defaultValue, boolean refreshCache) {
		try {
			String _value = userApi.getUserContactConfigurationValueByKey(key, null);
			return Objects.toString(_value, defaultValue);
		} catch (ApiException e) {
			logger.error("Could not getActiveUserContact " + key, e);
		}
		return defaultValue;
	}

	@Override
	public boolean getActiveUserContact(String key, boolean defaultValue) {
		String v = getActiveUserContact(key, Boolean.toString(defaultValue), false);
		return "1".equals(v) || "true".equals(v);
	}

	@Override
	public int getActiveUserContact(String key, int defaultValue) {
		String v = getActiveUserContact(key, Integer.toString(defaultValue), false);
		return Integer.parseInt(v);
	}

	@Override
	public String get(IContact contact, String key, String defaultValue, boolean refreshCache) {
		if (Objects.equals(contact.getId(), contextService.getActiveUserContact().map(IContact::getId).orElse(null))) {
			return getActiveUserContact(key, defaultValue, refreshCache);
		}

		try {
			String _value = userApi.getUserContactConfigurationValueByKey(contact.getId(), null);
			return Objects.toString(_value, defaultValue);
		} catch (ApiException e) {
			logger.error("Could not get " + contact.getId() + " " + key, e);
		}

		throw new UnsupportedOperationException("requires ict-adminstrator if non self");
	}

	@Override
	public boolean get(IContact contact, String key, boolean defaultValue) {
		String v = get(contact, key, Boolean.toString(defaultValue), false);
		return "1".equals(v) || "true".equals(v);
	}

	@Override
	public int get(IContact contact, String key, int defaultValue) {
		String v = get(contact, key, Integer.toString(defaultValue), false);
		return Integer.parseInt(v);
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
		try {
			return userApi.findGlobalConfigurationDirectSubnodeKeysForGivenKey(key);
		} catch (ApiException e) {
			logger.error("Could not getSubNodes " + key, e);
		}
		return Collections.emptyList();
	}

	@Override
	public String getActiveMandator(String key, String defaultValue, boolean refreshCache) {
		return get(contextService.getActiveMandator().orElse(null), key, defaultValue, refreshCache);
	}

	@Override
	public boolean getActiveMandator(String key, boolean defaultValue) {
		String v = getActiveMandator(key, Boolean.toString(defaultValue), false);
		return "1".equals(v) || "true".equals(v);
	}

	@Override
	public int getActiveMandator(String key, int defaultValue) {
		String v = getActiveMandator(key, Integer.toString(defaultValue), false);
		return Integer.parseInt(v);
	}

	@Override
	public boolean set(String key, String value) {
		try {
			if (value == null) {
				userApi.deleteGlobalConfigurationValueByKey(key);
				contextService.sendEvent(ElexisEventTopics.BASE_CONFIG + "delete", "global/" + key);
			} else {
				userApi.setGlobalConfigurationValueByKey(key, value);
				contextService.sendEvent(ElexisEventTopics.BASE_CONFIG + "update", "global/" + key);
			}
			return true;
		} catch (ApiException e) {
			logger.error("Could not set/delete global key " + key, e);
		}
		return false;
	}

	@Override
	public boolean set(String key, String value, boolean addTraceEntry) {
		// addTraceEntry is determined by server
		return set(key, value);
	}

	@Override
	public boolean set(String key, boolean value) {
		return set(key, Boolean.toString(value), false);
	}

	@Override
	public boolean set(String key, int value) {
		return set(key, Integer.toString(value), false);
	}

	@Override
	public boolean set(IContact contact, String key, int value) {
		return set(contact, key, Integer.toString(value));
	}

	@Override
	public boolean set(IContact contact, String key, String value) {
		if (Objects.equals(contact.getId(), contextService.getActiveUserContact().map(IContact::getId).orElse(null))) {
			return setActiveUserContact(key, value);
		}
		if (value == null) {
			// FIXME
		} else {
			// FIXME
		}
		throw new UnsupportedOperationException("May only set values for oneself");
	}

	@Override
	public void setActiveMandator(String key, String value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void setActiveMandator(String key, boolean value) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setActiveUserContact(String key, String value) {
		try {
			if (value == null) {
				userApi.deleteUserContactConfigurationEntryByKey(key);
				contextService.sendEvent(ElexisEventTopics.BASE_CONFIG + "delete", "user/" + key);
			} else {
				userApi.setUserContactConfigurationValueByKey(key, value);
				contextService.sendEvent(ElexisEventTopics.BASE_CONFIG + "update", "user/" + key);
			}
		} catch (ApiException e) {
			logger.error("Could not setActiveUserContact " + key, e);
			return false;
		}
		return true;
	}

	@Override
	public boolean setActiveUserContact(String key, int value) {
		return setActiveUserContact(key, Integer.toString(value));
	}

	@Override
	public boolean setActiveUserContact(String key, boolean value) {
		return setActiveUserContact(key, Boolean.toString(value));
	}

	@Override
	public boolean set(IContact contact, String key, boolean value) {
		return set(contact, key, Boolean.valueOf(value));
	}

	@Override
	public boolean setFromList(String key, List<String> values) {
		String flattenedValue = values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(key, flattenedValue);
	}

	@Override
	public boolean setFromList(IContact contact, String key, List<String> values) {
		String flattenedValue = values.stream().map(o -> o.toString()).reduce((u, t) -> u + LIST_SEPARATOR + t).get();
		return set(contact, key, flattenedValue);
	}

	@Override
	public void setActiveUserContact(Map<Object, Object> map) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public String getOrInsert(IContact contact, String key, Supplier<String> insertValue) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Map<Object, Object> getAsMap(IContact contact) {
		throw new UnsupportedOperationException("requires ict-adminstrator if non self");
	}

	@Override
	public Map<Object, Object> getActiveUserContactAsMap() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public void setFromMap(IContact person, Map<Object, Object> map) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public ILocalLock getLocalLock(Object object) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	@Override
	public Optional<ILocalLock> getManagedLock(Object object) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

}
