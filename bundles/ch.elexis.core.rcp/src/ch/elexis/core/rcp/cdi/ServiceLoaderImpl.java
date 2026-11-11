package ch.elexis.core.rcp.cdi;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.LoggerFactory;

import ch.elexis.core.interfaces.IServiceLoader;
import ch.elexis.core.osgi.UnsatisfiedComponentUtil;
import ch.elexis.core.rcp.utils.OsgiServiceUtil;

@Component(immediate = true)
public class ServiceLoaderImpl implements IServiceLoader {

	private static HashMap<Object, ServiceReference<?>> serviceReferences = new HashMap<>();

	@Override
	public <T> Optional<T> getService(Class<T> clazz) {
		return getService(clazz, null);
	}

	@Override
	public <T> Optional<T> getService(Class<T> clazz, String filter) {
		return getServiceInternal(clazz, filter);
	}

	public synchronized <T extends Object> Optional<T> getServiceWait(Class<T> clazz, long timeout) {
		Bundle bundle = FrameworkUtil.getBundle(clazz);
		// fallback to our context ...
		if (bundle == null || bundle.getBundleContext() == null) {
			bundle = FrameworkUtil.getBundle(OsgiServiceUtil.class);
		}
		ServiceTracker<T, T> serviceTracker = new ServiceTracker<>(bundle.getBundleContext(), clazz, null);
		serviceTracker.open();
		try {
			T service = serviceTracker.waitForService(timeout);
			if (service != null) {
				ServiceReference<?> serviceReferenceTracker = serviceTracker.getServiceReference();
				serviceReferences.put(service, serviceReferenceTracker);
				return Optional.of(service);
			} else {
				ServiceComponentRuntime scr = getService(ServiceComponentRuntime.class).get();
				String unsatisfiedComponents = UnsatisfiedComponentUtil.listUnsatisfiedComponents(scr, bundle);
				LoggerFactory.getLogger(getClass()).warn("ERR getServiceWait [{}]: {}", clazz.getName(),
						unsatisfiedComponents);
				ungetService(scr);
			}
		} catch (InterruptedException e) {
			LoggerFactory.getLogger(getClass()).error("Could not get service", e);
		} finally {
			serviceTracker.close();
		}
		return Optional.empty();
	}

	public synchronized void ungetService(Object service) {
		if (service instanceof Optional) {
			throw new IllegalStateException("Optional is not a service");
		}
		ServiceReference<?> reference = serviceReferences.get(service);
		if (reference != null) {
			Bundle bundle = FrameworkUtil.getBundle(service.getClass());
			// fallback to our context ...
			if (bundle == null || bundle.getBundleContext() == null) {
				bundle = FrameworkUtil.getBundle(OsgiServiceUtil.class);
			}
			if (bundle.getBundleContext().ungetService(reference)) {
				serviceReferences.remove(service);
				LoggerFactory.getLogger(getClass()).info("Release active service [" + service + "] from "
						+ serviceReferences.size() + " active references");
			} else {
				serviceReferences.remove(service);
				LoggerFactory.getLogger(getClass()).info("Release not active service [" + service + "] from "
						+ serviceReferences.size() + " active references");
			}
			return;
		}
		LoggerFactory.getLogger(getClass()).warn(
				"Could not release service [" + service + "] from " + serviceReferences.size() + " active references");
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
