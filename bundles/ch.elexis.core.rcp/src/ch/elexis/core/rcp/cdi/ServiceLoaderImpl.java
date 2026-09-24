package ch.elexis.core.rcp.cdi;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.interfaces.IServiceLoader;
import ch.elexis.core.rcp.utils.OsgiServiceUtil;

@Component(immediate = true)
public class ServiceLoaderImpl implements IServiceLoader {

	@Override
	public <T> Optional<T> getService(Class<T> clazz) {
		return getService(clazz, null);
	}

	@Override
	public <T> Optional<T> getService(Class<T> clazz, String filter) {
		return getServiceInternal(clazz, filter);
	}

	private synchronized <T extends Object> Optional<T> getServiceInternal(Class<T> clazz, String filter) {
		Bundle bundle = FrameworkUtil.getBundle(clazz);
		// fallback to our context ...
		if (bundle == null || bundle.getBundleContext() == null) {
			bundle = FrameworkUtil.getBundle(OsgiServiceUtil.class);
		}
		Collection<ServiceReference<T>> references = Collections.emptyList();
		try {
			references = bundle.getBundleContext().getServiceReferences(clazz, filter);
		} catch (InvalidSyntaxException e) {
			LoggerFactory.getLogger(getClass()).error("Invalid filter syntax", e);
		}
		if (!references.isEmpty() && references.size() == 1) {
			ServiceReference<T> ref = references.iterator().next();
			T service = bundle.getBundleContext().getService(ref);
			if (service != null) {
				return Optional.of(service);
			}
		}
		return Optional.empty();
	}

}
