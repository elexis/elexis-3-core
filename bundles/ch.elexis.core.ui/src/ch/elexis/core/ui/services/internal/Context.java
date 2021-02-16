package ch.elexis.core.ui.services.internal;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.eclipse.e4.core.contexts.IEclipseContext;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;

public class Context implements IContext {
	
	private ConcurrentHashMap<String, Object> context;
	
	private Context parent;
	
	private IEclipseContext eclipseContext;
	
	public Context(){
		this(null, "root");
	}
	
	public Context(Context parent, String name){
		context = new ConcurrentHashMap<>();
		this.parent = parent;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getTyped(Class<T> clazz){
		Optional<T> ret = Optional.ofNullable((T) context.get(clazz.getName()));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getTyped(clazz);
		}
		return ret;
	}
	
	@Override
	public void setTyped(Object object){
		if (object != null) {
			if (object instanceof IUser) {
				// also set active user contact
				IContact userContact = ((IUser) object).getAssignedContact();
				setNamed(ACTIVE_USERCONTACT, userContact);
			}
			Optional<Class<?>> modelInterface = getModelInterface(object);
			if (modelInterface.isPresent()) {
				if (object.equals(context.get(modelInterface.get().getName()))) {
					// object is already in the context do nothing otherwise loop happens
					return;
				}
				context.put(modelInterface.get().getName(), object);
			} else {
				context.put(object.getClass().getName(), object);
			}
			if (eclipseContext != null) {
				if (modelInterface.isPresent()) {
					eclipseContext.set(modelInterface.get().getName(), object);
				} else {
					eclipseContext.set(object.getClass().getName(), object);
				}
			}
		} else {
			throw new IllegalArgumentException("object must not be null, use #removeTyped");
		}
	}
	
	private Optional<Class<?>> getModelInterface(Object object){
		Class<?>[] interfaces = object.getClass().getInterfaces();
		for (Class<?> interfaze : interfaces) {
			if (interfaze.getName().startsWith("ch.elexis.core.model") && !interfaze.getName().contains("Identifiable")) {
				return Optional.of(interfaze);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public void removeTyped(Class<?> clazz){
		context.remove(clazz.getName());
		if (eclipseContext != null) {
			eclipseContext.remove(clazz.getName());
		}
	}
	
	@Override
	public Optional<?> getNamed(String name){
		Optional<?> ret = Optional.ofNullable(context.get(name));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getNamed(name);
		}
		if (ret.isPresent() && ret.get() instanceof Supplier) {
			return Optional.ofNullable(((Supplier<?>) ret.get()).get());
		}
		return ret;
	}
	
	@Override
	public void setNamed(String name, Object object){
		if (object == null) {
			context.remove(name);
		} else if (object.equals(context.get(name))) {
			// object is already in the context do nothing otherwise loop happens
			return;
		} else {
			context.put(name, object);
		}
		if (eclipseContext != null) {
			eclipseContext.set(name, object);
		}
	}
	
	public void setParent(Context parent){
		this.parent = parent;
	}
	
	public void setEclipseContext(IEclipseContext applicationContext){
		this.eclipseContext = applicationContext;
		// update with not present context
		context.forEach((k, v) -> {
			if (k instanceof String && v != null) {
				eclipseContext.set(k, v);
			}
		});
	}
	
	@Override
	public String getStationIdentifier(){
		return getNamed(STATION_IDENTIFIER).get().toString();
	}
}
