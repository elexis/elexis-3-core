package ch.elexis.core.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsgiServiceUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(OsgiServiceUtil.class);
	
	private static HashMap<Object, ServiceReference<?>> serviceReferences = new HashMap<>();
	
	/**
	 * Get a service from the OSGi service registry. <b>Always</b> release the service using the
	 * {@link OsgiServiceUtil#ungetService(Object)} method after usage.
	 * 
	 * @param clazz
	 * @return
	 */
	public synchronized static <T extends Object> Optional<T> getService(Class<T> clazz){
		return getService(clazz, null);
	}
	
	/**
	 * Get a service from the OSGi service registry. <b>Always</b> release the service using the
	 * {@link OsgiServiceUtil#ungetService(Object)} method after usage.
	 * 
	 * @param clazz
	 * @param filter
	 *            provide a filter to select a specific service instance if multiple available
	 * @return
	 */
	public synchronized static <T extends Object> Optional<T> getService(Class<T> clazz,
		String filter){
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
	 * Release a service that was acquired using the {@link OsgiServiceUtil#getService(Class)}
	 * method.
	 * 
	 * @param service
	 */
	public synchronized static void ungetService(Object service){
		if (service instanceof Optional) {
			throw new IllegalStateException("Optional is not a service");
		}
		ServiceReference<?> reference = serviceReferences.get(service);
		if (reference != null) {
			Bundle bundle = FrameworkUtil.getBundle(service.getClass());
			// fallback to our context ...
			if (bundle.getBundleContext() == null) {
				bundle = FrameworkUtil.getBundle(OsgiServiceUtil.class);
			}
			if (bundle.getBundleContext().ungetService(reference)) {
				serviceReferences.remove(service);
				logger.info("Release active service [" + service + "] from "
					+ serviceReferences.size() + " active references");
			} else {
				serviceReferences.remove(service);
				logger.info("Release not active service [" + service + "] from "
					+ serviceReferences.size() + " active references");
			}
			return;
		}
		logger.warn("Could not release service [" + service + "] from " + serviceReferences.size()
			+ " active references");
	}
}
