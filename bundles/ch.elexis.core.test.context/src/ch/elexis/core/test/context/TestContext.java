package ch.elexis.core.test.context;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
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
	
	@Override
	public Optional<IUser> getActiveUser(){
		Optional<IUser> ret = Optional.ofNullable((IUser) context.get(ACTIVE_USER));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getActiveUser();
		}
		return ret;
	}
	
	@Override
	public void setActiveUser(IUser user){
		if (user == null) {
			context.remove(ACTIVE_USER);
		} else {
			setNamed(ACTIVE_USER, user);
		}
	}
	
	@Override
	public Optional<IContact> getActiveUserContact(){
		Optional<IContact> ret = Optional.ofNullable((IContact) context.get(ACTIVE_USERCONTACT));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getActiveUserContact();
		} else if (!ret.isPresent() && getActiveUser().isPresent()) {
			IContact contact = getActiveUser().get().getAssignedContact();
			if (contact != null) {
				setActiveUserContact(contact);
				ret = Optional.of(contact);
			}
		}
		return ret;
	}
	
	@Override
	public void setActiveUserContact(IContact userContact){
		if (userContact == null) {
			context.remove(ACTIVE_USERCONTACT);
		} else {
			setNamed(ACTIVE_USERCONTACT, userContact);
		}
	}
	
	@Override
	public Optional<IPatient> getActivePatient(){
		Optional<IPatient> ret = Optional.ofNullable((IPatient) context.get(ACTIVE_PATIENT));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getActivePatient();
		}
		return ret;
	}
	
	@Override
	public void setActivePatient(IPatient patient){
		if (patient == null) {
			context.remove(ACTIVE_PATIENT);
		} else {
			setNamed(ACTIVE_PATIENT, patient);
		}
	}
	
	@Override
	public Optional<IMandator> getActiveMandator(){
		Optional<IMandator> ret = Optional.ofNullable((IMandator) context.get(ACTIVE_MANDATOR));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getActiveMandator();
		}
		return ret;
	}
	
	@Override
	public void setActiveMandator(IMandator mandator){
		if (mandator == null) {
			context.remove(ACTIVE_MANDATOR);
		} else {
			setNamed(ACTIVE_MANDATOR, mandator);
		}
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
			Optional<Class<?>> modelInterface = getModelInterface(object);
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
			if (interfaze.getName().startsWith("ch.elexis.core.model")) {
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
