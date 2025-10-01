package ch.elexis.core.services.eedep;

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

import ch.elexis.core.model.IContact;
import ch.elexis.core.services.ConfigServiceActivator;
import ch.elexis.core.services.IConfigService;
import ch.myelexis.server.api.UserApi;
import ch.myelexis.server.client.ApiException;

@Component(name = ConfigServiceActivator.EEDEP, enabled = false)
public class ConfigService implements IConfigService {

	@Reference
	UserApi userApi;

	private Logger logger = LoggerFactory.getLogger(getClass());

	public static final String LIST_SEPARATOR = ",";

	@Override
	public String get(String key, String defaultValue, boolean refreshCache) {
		try {
			String _value = userApi.getGlobalConfigurationValueByKey(key);
			return Objects.toString(_value, defaultValue);
		} catch (ApiException e) {
			logger.error("Could not fetch key " + key, e);
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
			String _value = userApi.getUserContactConfigurationValueByKey(key);
			return Objects.toString(_value, defaultValue);
		} catch (ApiException e) {
			logger.error("Could not fetch key " + key, e);
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
	public boolean set(String key, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean set(String key, String value, boolean addTraceEntry) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean set(String key, boolean value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean set(String key, int value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean set(IContact contact, String key, int value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean set(IContact contact, String key, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setActiveMandator(String key, String value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setActiveMandator(String key, boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setActiveUserContact(String key, String value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setActiveUserContact(String key, int value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setActiveUserContact(String key, boolean value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean set(IContact contact, String key, boolean value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setFromList(String key, List<String> values) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setFromList(IContact contact, String key, List<String> values) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setActiveUserContact(Map<Object, Object> map) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getOrInsert(IContact contact, String key, Supplier<String> insertValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getActiveMandator(String key, String defaultValue, boolean refreshCache) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getActiveMandator(String key, boolean defaultValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getActiveMandator(String key, int defaultValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String get(IContact contact, String key, String defaultValue, boolean refreshCache) {
		throw new UnsupportedOperationException("requires ict-adminstrator if non self");
	}

	@Override
	public boolean get(IContact contact, String key, boolean defaultValue) {
		throw new UnsupportedOperationException("requires ict-adminstrator if non self");
	}

	@Override
	public int get(IContact contact, String key, int defaultValue) {
		throw new UnsupportedOperationException("requires ict-adminstrator if non self");
	}

	@Override
	public List<String> getAsList(IContact contact, String key, List<String> defaultValue) {
		throw new UnsupportedOperationException("requires ict-adminstrator if non self");
	}

	@Override
	public Map<Object, Object> getAsMap(IContact contact) {
		throw new UnsupportedOperationException("requires ict-adminstrator if non self");
	}

	@Override
	public Map<Object, Object> getActiveUserContactAsMap() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFromMap(IContact person, Map<Object, Object> map) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<String> getSubNodes(String key, boolean refreshCache) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILocalLock getLocalLock(Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ILocalLock> getManagedLock(Object object) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

}
