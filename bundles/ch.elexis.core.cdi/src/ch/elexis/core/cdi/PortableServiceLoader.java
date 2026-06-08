package ch.elexis.core.cdi;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.interfaces.IServiceLoader;
import ch.elexis.core.services.IModelService;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Provide a portable service loader, that allows to statically load services
 * both in Eclipse OSGi and Quarkus Arc.
 */
@Singleton
@Component(immediate = true)
public class PortableServiceLoader {

	private static IServiceLoader serviceLoader;

	@Inject
	@Reference
	public void setServiceLoader(IServiceLoader serviceLoader) {
		LoggerFactory.getLogger(getClass()).debug("setServiceLoader %s", serviceLoader);
		PortableServiceLoader.serviceLoader = serviceLoader;
	}

	public static IServiceLoader getServiceLoader() {
		return PortableServiceLoader.serviceLoader;
	}

	public synchronized static <T extends Object> Optional<T> getOptional(Class<T> clazz) {
		return PortableServiceLoader.serviceLoader.getService(clazz);
	}

	public synchronized static <T extends Object> T get(Class<T> clazz) {
		return PortableServiceLoader.serviceLoader.getService(clazz).get();
	}

	public synchronized static <T extends Object> Optional<T> getService(Class<T> clazz, String filter) {
		return PortableServiceLoader.serviceLoader.getService(clazz, filter);
	}

	public synchronized static IModelService getCoreModelService() {
		return PortableServiceLoader.serviceLoader
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
	}

}
