package ch.elexis.core.jpa.model.adapter.proxy;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import ch.elexis.core.jpa.model.service.holder.StoreToStringServiceHolder;
import ch.elexis.core.model.Identifiable;

/**
 * {@link InvocationHandler} can be used to make a loaded {@link Identifiable}
 * garbage collect able. It will be reloaded and held be a {@link WeakReference}
 * on invocation of a method.
 * 
 * @author thomas
 *
 */
public class ModelAdapterProxyHandler implements InvocationHandler {

	private String storeToString;

	private WeakReference<?> targetReference;

	private static Map<Class<?>, Map<String, Method>> classMethodMap;

	public ModelAdapterProxyHandler(Identifiable target) {
		this.storeToString = StoreToStringServiceHolder.get().storeToString(target).get();
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		Object target = targetReference != null ? targetReference.get() : null;
		if (target == null) {
			target = StoreToStringServiceHolder.get().loadFromString(storeToString).orElse(null);
			targetReference = new WeakReference<>(target);
		}
		if (target != null) {
			Map<String, Method> methods = classMethodMap != null ? classMethodMap.get(target.getClass()) : null;
			if (methods == null) {
				methods = addToClassMethodMap(target.getClass());
			}
			Object result = methods.get(method.getName()).invoke(target, args);
			return result;
		}
		throw new IllegalStateException("Could not load target for [" + storeToString + "]");
	}

	private Map<String, Method> addToClassMethodMap(Class<? extends Object> clazz) {
		if (classMethodMap == null) {
			classMethodMap = new HashMap<Class<?>, Map<String, Method>>();
		}
		Method[] methods = clazz.getMethods();
		Map<String, Method> methodMap = new HashMap<>();
		for (Method method : methods) {
			methodMap.put(method.getName(), method);
		}
		classMethodMap.put(clazz, methodMap);
		return methodMap;
	}
}
