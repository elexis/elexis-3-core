package ch.elexis.core.ui.services.internal;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.e4.core.contexts.IEclipseContext;

import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IContextService;

public class Context implements IContext {

	private ConcurrentHashMap<String, Object> context;

	private IContextService service;

	private Context parent;

	private IEclipseContext eclipseContext;

	private TypedModifier typedModifier;

	public Context(IContextService service) {
		this(null, "root", service); //$NON-NLS-1$
	}

	public Context(Context parent, String name, IContextService service) {
		context = new ConcurrentHashMap<>();
		this.parent = parent;
		this.service = service;
		this.typedModifier = new TypedModifier(this);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getTyped(Class<T> clazz) {
		Optional<T> ret = Optional.ofNullable((T) context.get(clazz.getName()));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getTyped(clazz);
		}
		return ret;
	}

	@Override
	public void setTyped(Object object) {
		setTyped(object, false);
	}

	protected void setTyped(Object object, boolean ignoreModifier) {
		if (object != null) {
			Object previous = null;
			Optional<Class<?>> modelInterface = getModelInterface(object);
			if (modelInterface.isPresent()) {
				if (object.equals(context.get(modelInterface.get().getName()))) {
					// object is already in the context do nothing otherwise loop happens
					return;
				}
				previous = context.get(modelInterface.get().getName());
				context.put(modelInterface.get().getName(), object);
			} else {
				previous = context.get(object.getClass().getName());
				context.put(object.getClass().getName(), object);
			}
			if (eclipseContext != null) {
				if (modelInterface.isPresent()) {
					eclipseContext.set(modelInterface.get().getName(), object);
				} else {
					eclipseContext.set(object.getClass().getName(), object);
				}
			}
			if (typedModifier != null) {
				typedModifier.releaseAndRefreshLock(previous);
				if (!ignoreModifier) {
					typedModifier.modifyFor(object);
				}
			}
		} else {
			throw new IllegalArgumentException("object must not be null, use #removeTyped"); //$NON-NLS-1$
		}
	}

	private Optional<Class<?>> getModelInterface(Object object) {
		Class<?>[] interfaces = object.getClass().getInterfaces();
		for (Class<?> interfaze : interfaces) {
			if (interfaze.getName().startsWith("ch.elexis.core.model") //$NON-NLS-1$
					&& !interfaze.getName().contains("Identifiable")) { //$NON-NLS-1$
				return Optional.of(interfaze);
			}
		}
		return Optional.empty();
	}

	@Override
	public void removeTyped(Class<?> clazz) {
		removeTyped(clazz, false);
	}

	protected void removeTyped(Class<?> clazz, boolean ignoreModifier) {
		Object previous = context.remove(clazz.getName());
		if (eclipseContext != null) {
			eclipseContext.remove(clazz.getName());
		}

		if (typedModifier != null) {
			if (!ignoreModifier) {
				typedModifier.releaseAndRefreshLock(previous);
			}
			typedModifier.modifyRemove(clazz);
		}
	}

	@Override
	public Optional<?> getNamed(String name) {
		Optional<?> ret = Optional.ofNullable(context.get(name));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getNamed(name);
		}
		if (ret.isPresent() && ret.get() instanceof Supplier) {
			return Optional.ofNullable(((Supplier<?>) ret.get()).get());
		}
		return ret;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void setNamed(String name, Object object) {
		if (object == null) {
			context.remove(name);
		} else if (object.equals(context.get(name))) {
			// object is already in the context do nothing otherwise loop happens
			return;
		} else {
			if (context.get(name) instanceof Consumer) {
				((Consumer) context.get(name)).accept(object);
			} else {
				context.put(name, object);
			}
		}
		if (eclipseContext != null) {
			eclipseContext.set(name, object);
		}
	}

	public void setParent(Context parent) {
		this.parent = parent;
	}

	public void setEclipseContext(IEclipseContext applicationContext) {
		this.eclipseContext = applicationContext;
		setTyped(applicationContext);
		// update with not present context
		context.forEach((k, v) -> {
			if (k instanceof String && v != null) {
				eclipseContext.set(k, v);
			}
		});
	}

	@Override
	public String getStationIdentifier() {
		return getNamed(STATION_IDENTIFIER).get().toString();
	}

	public IContextService getService() {
		return service;
	}
}
