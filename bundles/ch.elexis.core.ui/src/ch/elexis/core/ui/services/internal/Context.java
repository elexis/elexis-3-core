package ch.elexis.core.ui.services.internal;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.e4.core.contexts.IEclipseContext;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;

public class Context implements IContext {
	
	private ConcurrentHashMap<String, Object> context;
	
	private Context parent;
	
	private IEclipseContext eclipseContext;
	
	private ElexisEventDispatcher elexisEventDispatcher;
	
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
		if (eclipseContext != null) {
			eclipseContext.set(ACTIVE_USER, user);
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
		context.put(ACTIVE_USERCONTACT, userContact);
		if (eclipseContext != null) {
			eclipseContext.set(ACTIVE_USERCONTACT, userContact);
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
		context.put(ACTIVE_PATIENT, patient);
		if (eclipseContext != null) {
			eclipseContext.set(ACTIVE_PATIENT, patient);
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
		context.put(ACTIVE_MANDATOR, mandator);
		if (eclipseContext != null) {
			eclipseContext.set(ACTIVE_MANDATOR, mandator);
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
			context.put(object.getClass().getName(), object);
			if (eclipseContext != null) {
				eclipseContext.set(object.getClass().getName(), object);
			}
		}
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
		return ret;
	}
	
	@Override
	public void setNamed(String name, Object object){
		if (object == null) {
			context.remove(name);
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
	}
	
	public void setElexisEventDispatcher(ElexisEventDispatcher elexisEventDispatcher){
		this.elexisEventDispatcher = elexisEventDispatcher;
	}
}
