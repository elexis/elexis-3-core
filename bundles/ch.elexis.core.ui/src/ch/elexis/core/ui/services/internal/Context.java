package ch.elexis.core.ui.services.internal;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.e4.core.contexts.IEclipseContext;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
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
			updateElexisEventDispatcher(object);
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
	
	private void updateElexisEventDispatcher(Object object){
		// if the selection is not same in ElexisEventDispatcher fire a selection event
		if (object instanceof IPatient) {
			Patient poPatient = Patient.load(((IPatient) object).getId());
			Patient poSelected = ElexisEventDispatcher.getSelectedPatient();
			if (poSelected == null || !poSelected.equals(poPatient)) {
				ElexisEventDispatcher.fireSelectionEvent(poPatient);
			}
		}
		if (object instanceof ICoverage) {
			Fall po = Fall.load(((ICoverage) object).getId());
			IPersistentObject selected = ElexisEventDispatcher.getSelected(Fall.class);
			if (selected == null || !selected.equals(po)) {
				ElexisEventDispatcher.fireSelectionEvent(po);
			}
		}
		if (object instanceof IEncounter) {
			Konsultation po = Konsultation.load(((IEncounter) object).getId());
			IPersistentObject selected = ElexisEventDispatcher.getSelected(Konsultation.class);
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
		return CoreHub.localCfg.get(Preferences.STATION_IDENT_ID, null);
	}
}
