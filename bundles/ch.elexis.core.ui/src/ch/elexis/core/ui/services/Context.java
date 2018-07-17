package ch.elexis.core.ui.services;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;

public class Context implements IContext {
	
	private ConcurrentHashMap<String, Object> context;
	
	private Context parent;
	
	public Context(){
		this(null, "root");
	}
	
	public Context(Context parent, String name){
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
		context.put(ACTIVE_USER, user);
	}
	
	@Override
	public Optional<IContact> getActiveUserContact(){
		Optional<IContact> ret = Optional.ofNullable((IContact) context.get(ACTIVE_USERCONTACT));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getActiveUserContact();
		}
		return ret;
	}
	
	@Override
	public void setActiveUserContact(IContact userContact){
		context.put(ACTIVE_USERCONTACT, userContact);
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
		context.put(ACTIVE_PATIENT, patient);
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
		context.put(ACTIVE_MANDATOR, mandator);
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
		context.put(object.getClass().getName(), object);
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
		context.put(name, object);
	}
	
	public void setParent(Context parent){
		this.parent = parent;
	}
}
