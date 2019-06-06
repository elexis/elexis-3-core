package ch.elexis.core.test.context;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;

public class TestContext implements IContext {
	
	private ConcurrentHashMap<String, Object> context;
	
	private TestContext parent;
	
	public TestContext(){
		this(null, "root");
	}
	
	public TestContext(TestContext parent, String name){
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
			if (object.equals(context.get(modelInterface.get().getName()))) {
				// object is already in the context do nothing otherwise loop happens
				return;
			}
			if (modelInterface.isPresent()) {
				context.put(modelInterface.get().getName(), object);
			} else {
				context.put(object.getClass().getName(), object);
			}
		}
	}
	
	private Optional<Class<?>> getModelInterface(Object object){
		Class<?>[] interfaces = object.getClass().getInterfaces();
		for (Class<?> interfaze : interfaces) {
			if (interfaze.getName().startsWith("ch.elexis.core.model")
				&& !interfaze.getName().contains("Identifiable")) {
				return Optional.of(interfaze);
			}
		}
		return Optional.empty();
	}
	
	@Override
	public void removeTyped(Class<?> clazz){
		context.remove(clazz.getName());
		
	}
	
	@Override
	public Optional<?> getNamed(String name){
		Optional<?> ret = Optional.ofNullable(context.get(name));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getNamed(name);
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
	}
	
	public void setParent(TestContext parent){
		this.parent = parent;
	}
	
	@Override
	public String getStationIdentifier(){
		// TODO Auto-generated method stub
		return null;
	}
}
