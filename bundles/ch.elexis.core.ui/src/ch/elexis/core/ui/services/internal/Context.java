package ch.elexis.core.ui.services.internal;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.e4.core.contexts.IEclipseContext;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IContext;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;

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
		setNamed(ACTIVE_USER, user);
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
		setNamed(ACTIVE_USERCONTACT, userContact);
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
		setNamed(ACTIVE_PATIENT, patient);
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
		setNamed(ACTIVE_MANDATOR, mandator);
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
			if (eclipseContext != null) {
				if (modelInterface.isPresent()) {
					eclipseContext.set(modelInterface.get().getName(), object);
				} else {
					eclipseContext.set(object.getClass().getName(), object);
				}
			}
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
		return ret;
	}
	
	@Override
	public void setNamed(String name, Object object){
		if (object == null) {
			context.remove(name);
		}
		else if (object.equals(context.get(name))) {
			// object is already in the context do nothing otherwise loop happens
			return;
		}
		else {
			context.put(name, object);
		}
		if (eclipseContext != null) {
			eclipseContext.set(name, object);
		}
		updateElexisEventDispatcher(name, object);
	}
	
	private void updateElexisEventDispatcher(String name, Object object){
		// if the selection is not same in ElexisEventDispatcher fire a selection event
		if (IContext.ACTIVE_PATIENT.equals(name) && object instanceof Identifiable) {
			Patient poPatient = Patient.load(((Identifiable) object).getId());
			Patient poSelected = ElexisEventDispatcher.getSelectedPatient();
			if (poSelected == null || !poSelected.equals(poPatient)) {
				ElexisEventDispatcher.fireSelectionEvent(poPatient);
			}
		}
		if (IContext.ACTIVE_COVERAGE.equals(name) && object instanceof Identifiable) {
			Fall po = Fall.load(((Identifiable) object).getId());
			IPersistentObject selected = ElexisEventDispatcher.getSelected(Fall.class);
			if (selected == null || !selected.equals(po)) {
				ElexisEventDispatcher.fireSelectionEvent(po);
			}
		}
	}
	
	public void setParent(Context parent){
		this.parent = parent;
	}
	
	public void setEclipseContext(IEclipseContext applicationContext){
		this.eclipseContext = applicationContext;
	}
	
	@Override
	public String getStationIdentifier(){
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<ICoverage> getActiveCoverage(){
		Optional<ICoverage> ret = Optional.ofNullable((ICoverage) context.get(ACTIVE_COVERAGE));
		if (!ret.isPresent() && parent != null) {
			ret = parent.getActiveCoverage();
		}
		return ret;
	}

	@Override
	public void setActiveCoverage(ICoverage coverage){
		setNamed(ACTIVE_COVERAGE, coverage);
	}
}
