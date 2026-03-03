package ch.elexis.core.rcp.cdi;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import ch.elexis.core.interfaces.IServiceLoader;
import ch.elexis.core.rcp.utils.OsgiServiceUtil;

@Component(immediate = true)
public class ServiceLoaderImpl implements IServiceLoader {

	private Map<String, Object> clazzCacheMap = Collections.synchronizedMap(new HashMap<>());

	@Override
	public <T> Optional<T> getService(Class<T> clazz) {
		return getService(clazz, null);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getService(Class<T> clazz, String filter) {
		String key = clazz.hashCode() + (filter != null ? "" + filter.hashCode() : "");
		if (!clazzCacheMap.containsKey(key)) {
			T service = OsgiServiceUtil.getService(clazz, filter).orElse(null);
			if (service != null) {
				clazzCacheMap.put(key, service);
			}
		}
		return Optional.ofNullable((T) clazzCacheMap.get(key));
	}

	@Deactivate
	public void deactivate() {
		clazzCacheMap.forEach((key, value) -> OsgiServiceUtil.ungetService(value));
	}

}
