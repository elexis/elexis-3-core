package ch.elexis.core.interfaces;

import java.util.Optional;

/**
 * Used by <code>ch.elexis.core.cdi.PortableServiceLocator</code>
 */
public interface IServiceLoader {

	public <T extends Object> Optional<T> getService(Class<T> clazz);

	public <T extends Object> Optional<T> getService(Class<T> clazz, String filter);

	public <T extends Object> Optional<T> getServiceWait(Class<T> clazz, long timeout);

	public void ungetService(Object object);

}
