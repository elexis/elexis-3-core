package ch.elexis.core.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.util.tracker.ServiceTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.osgi.UnsatisfiedComponentUtil;

public class OsgiServiceUtil {

	private static final Logger logger = LoggerFactory.getLogger(OsgiServiceUtil.class);

	private static HashMap<Object, ServiceReference<?>> serviceReferences = new HashMap<>();

	/**
	 * Get a service from the OSGi service registry. <b>Always</b> release the
	 * service using the {@link OsgiServiceUtil#ungetService(Object)} method after
	 * usage.
	 *
	 * @param clazz
	 * @return
	 */
	public synchronized static <T extends Object> Optional<T> getService(Class<T> clazz) {
		return getService(clazz, null);
	}

	/**
	 * Get a service from the OSGi service registry. <b>Always</b> release the
	 * service using the {@link OsgiServiceUtil#ungetService(Object)} method after
	 * usage.
	 *
	 * @param clazz
	 * @param filter provide a filter to select a specific service instance if
	 *               multiple available
	 * @return
	 */
	public synchronized static <T extends Object> Optional<T> getService(Class<T> clazz, String filter) {
		Bundle bundle = FrameworkUtil.getBundle(clazz);
		// fallback to our context ...
		if (bundle == null || bundle.getBundleContext() == null) {
			bundle = FrameworkUtil.getBundle(OsgiServiceUtil.class);
		}
		Collection<ServiceReference<T>> references = Collections.emptyList();
		try {
			references = bundle.getBundleContext().getServiceReferences(clazz, filter);
		} catch (InvalidSyntaxException e) {
			logger.error("Invalid filter syntax", e);
		}
		if (!references.isEmpty() && references.size() == 1) {
			ServiceReference<T> ref = references.iterator().next();
			T service = bundle.getBundleContext().getService(ref);
			if (service != null) {
				serviceReferences.put(service, ref);
				return Optional.of(service);
			}
		}
		return Optional.empty();
	}

	/**
	 * Release a service that was acquired using the
	 * {@link OsgiServiceUtil#getService(Class)} method.
	 *
	 * @param service
	 */
	public synchronized static void ungetService(Object service) {
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
				logger.info("Release active service [" + service + "] from " + serviceReferences.size()
						+ " active references");
			} else {
				serviceReferences.remove(service);
				logger.info("Release not active service [" + service + "] from " + serviceReferences.size()
						+ " active references");
			}
			return;
		}
		logger.warn(
				"Could not release service [" + service + "] from " + serviceReferences.size() + " active references");
	}

	/**
	 * Get a service from the OSGi service registry. Wait for it, it it's not
	 * available yet. <b>Always</b> release the service using the
	 * {@link OsgiServiceUtil#ungetService(Object)} method after usage.
	 *
	 * @param clazz
	 * @param timeout milliseconds to wait for the service to become available
	 * @return
	 * @since 3.10
	 */
	public synchronized static <T extends Object> Optional<T> getServiceWait(Class<T> clazz, long timeout) {
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
				logger.warn("ERR getServiceWait [{}]: {}", clazz.getName(), unsatisfiedComponents);
				ungetService(scr);
			}
		} catch (InterruptedException e) {
			logger.error("Could not get service", e);
		} finally {
			serviceTracker.close();
		}
		return Optional.empty();
	}
}
