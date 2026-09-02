package ch.elexis.core.fhir.model.adapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ch.elexis.core.fhir.model.interfaces.IFhirBased;

/**
 * Factory for creating lazy-loading proxies for IFhirBased objects. The proxy
 * ensures that load() is called before any accessor method when the object is
 * subsetted.
 */
public class LazyFhirProxy {

	// Methods that should NOT trigger loading (safe to call on subsetted objects)
	// TODO needs adaptation
	private static final Set<String> SAFE_METHODS = new HashSet<>(Arrays.asList("isSubsetted", "load", "getId",
			"getLabel", "getNarrativeLabel", "getNarrativeTags", "getLastupdate", "getClass", "hashCode", "toString",
			"equals", "notify", "notifyAll", "wait", "getMap", "getExtInfo", "setExtInfo", "getXid", "addXid"));

	/**
	 * Wraps an IFhirBased instance with a lazy-loading proxy.
	 * 
	 * @param instance the instance to wrap
	 * @return a proxy that will call load() before any accessor if subsetted
	 */
	@SuppressWarnings("unchecked")
	public static <T extends IFhirBased> T createLazy(T instance) {
		if (instance == null) {
			return null;
		}

		// If not subsetted, no need for proxy
		if (!instance.isSubsetted()) {
			return instance;
		}

		// Recursively collect ALL interfaces from the class hierarchy
		Class<?>[] allInterfaces = getAllInterfaces(instance.getClass()).toArray(new Class<?>[0]);

		InvocationHandler handler = new LazyLoadingHandler(instance);

		return (T) Proxy.newProxyInstance(instance.getClass().getClassLoader(), allInterfaces, handler);
	}

	/**
	 * Recursively collects all interfaces implemented by a class, including those
	 * from superclasses and superinterfaces.
	 */
	private static Set<Class<?>> getAllInterfaces(Class<?> clazz) {
		Set<Class<?>> interfaces = new HashSet<>();

		// Add all interfaces directly implemented by this class
		for (Class<?> iface : clazz.getInterfaces()) {
			interfaces.add(iface);
			// Recursively add superinterfaces
			interfaces.addAll(getAllInterfaces(iface));
		}

		// Recursively add interfaces from superclass
		Class<?> superClass = clazz.getSuperclass();
		if (superClass != null) {
			interfaces.addAll(getAllInterfaces(superClass));
		}

		return interfaces;
	}

	private static class LazyLoadingHandler implements InvocationHandler {
		private final IFhirBased target;

		public LazyLoadingHandler(IFhirBased target) {
			this.target = target;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String methodName = method.getName();

			// Check if we need to load before calling this method
			if (!SAFE_METHODS.contains(methodName) && target.isSubsetted()) {
				target.load();
			}

			try {
				return method.invoke(target, args);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			}
		}
	}
}