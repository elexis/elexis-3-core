package ch.elexis.core.data.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.datatypes.IPersistentObject;

/**
 * The ElexisContext contains a current set of selected elements. It takes care that at any time, the
 * combination of {@link Patient}, {@link Fall} and {@link Konsultation} is consistent to one of the 
 * following states:
 * 
 * <ul>
 * <li><code>null / null / null</code>
 * <li><code>Patient / null / null</code>
 * <li><code>Patient/ {@link Fall} f | Fall element of {@link Patient#getFaelle()} / null</code>
 * <li><code>Patient/ {@link Fall} f | Fall element of {@link Patient#getFaelle()} | {@link Konsultation} k | k element of f {@link Fall#getBehandlungen(boolean)}</code>
 * </ul>
 * 
 * The selection has to be set using the {@link #setSelection(Class, IPersistentObject)} method, which will then return a list of {@link ElexisEvent}
 * to be thrown to communicate the new state. External code must not interfere with the selection process of P/F/K and solely communicate one selection
 * or consume n selections events.
 * <br><br>
 * Elements external to the P/F/K consistency have to take care for themselves to stick consistent with the selection.
 * 
 *@since 3.0.0
 */
public class ElexisContext {
	/**
	 * stores the current valid selection
	 */
	private HashMap<Class<?>, IPersistentObject> currentSelection;
	/**
	 * stores the transition selection, which will result in the new {@link #currentSelection}
	 */
	private HashMap<Class<?>, IPersistentObject> newSelection;
	
	private List<ElexisEvent> events = new ArrayList<>();
	
	private Logger log = LoggerFactory.getLogger(ElexisContext.class);

	public ElexisContext() {
		currentSelection = new HashMap<Class<?>, IPersistentObject>();
		newSelection = new HashMap<Class<?>, IPersistentObject>();
	}

	/**
	 * Set a new valid {@link ElexisContext}. 
	 * @param typeClass the {@link Class} to set
	 * @param object the object, of typeClass to set
	 * @return a list of {@link ElexisEvent} to be communicated to the listeners
	 */
	@SuppressWarnings("unchecked")
	public synchronized List<ElexisEvent> setSelection(Class<?> typeClass,
			IPersistentObject object) {
		newSelection = (HashMap<Class<?>, IPersistentObject>) currentSelection.clone();

		if (typeClass.equals(Patient.class)) {
			setPatientSelection((Patient) object);
		} else if (typeClass.equals(Konsultation.class)) {
			setKonsultationSelection((Konsultation) object);
		} else if (typeClass.equals(Fall.class)) {
			setFallSelection((Fall) object);
		} else {
			newSelection.put(typeClass, object);
		}

		return determineChangeEvents();
	}

	/**
	 * compare the {@link #currentSelection} with the {@link #newSelection} and determine
	 * this list of required {@link ElexisEvent} notifications. 
	 * @return a list of {@link ElexisEvent} to be communicated to the listeners
	 */
	@SuppressWarnings("unchecked")
	private List<ElexisEvent> determineChangeEvents() {
		events.clear();
		Set<Class<?>> keySet = newSelection.keySet();

		for (Class<?> clazz : keySet) {
			Object obj1 = newSelection.get(clazz);
			Object obj2 = currentSelection.get(clazz);
			
			if(obj1 == null) {
				if(obj1 != obj2) {
					events.add(new ElexisEvent((PersistentObject) obj1, clazz, ElexisEvent.EVENT_DESELECTED));
					log.debug("[DESEL] "+clazz+" \t|| "+obj2+" --> \t "+obj1);
				}
			} else {
				if(!obj1.equals(obj2)) {
					events.add(new ElexisEvent((PersistentObject) obj1, clazz, ElexisEvent.EVENT_SELECTED));
					log.debug("[SEL] "+clazz+" \t|| "+obj2+" --> \t "+obj1);
				}
			}		
		}
		
		currentSelection = (HashMap<Class<?>, IPersistentObject>) newSelection.clone();
		
		return events;
	}

	/**
	 * selects a {@link Fall} and guards for the respective context validity
	 * @param f
	 */
	private void setFallSelection(Fall f) {
		if(f==null) {
			newSelection.put(Fall.class, null);
			newSelection.put(Konsultation.class, null);
			return;
		}
		
		if(f.equals(currentSelection.get(Fall.class))) return;
		
		Patient p = f.getPatient();
		if(p!=null) {
			newSelection.put(Patient.class, p);
			newSelection.put(Fall.class, f);
			newSelection.put(Konsultation.class, f.getLetzteBehandlung());
		} else {
			log.error("Fall "+f+" without Patient!");
			return;
		}
	}

	/**
	 * selects a {@link Konsultation} and guards for the respective context validity
	 * @param k
	 */
	private void setKonsultationSelection(Konsultation k) {		
		if(k==null) {
			newSelection.put(Konsultation.class, null);
			return;
		}
		
		if(k.equals(currentSelection.get(Konsultation.class))) return;
		
		Patient p = null;
		Fall f = k.getFall();

		if (f != null) {
			p = f.getPatient();
		} else {
			log.error("Konsultation "+k+" without Fall!");
			return;
		}

		if (p != null) {
			newSelection.put(Patient.class, p);
			newSelection.put(Fall.class, f);
			newSelection.put(Konsultation.class, k);
		} else {
			log.error("Fall "+f+" without Patient!");
			return;
		}
	}

	/**
	 * selects a {@link Patient} and guards for the respective context validity
	 * @param p
	 */
	private void setPatientSelection(Patient p) {
		if(p == null) {
			newSelection.put(Patient.class, null);
			newSelection.put(Fall.class, null);
			newSelection.put(Konsultation.class, null);
			return;
		}
		
		if(p.equals(currentSelection.get(Patient.class))) return;

		Konsultation k = p.getLetzteKons(false);
		Fall f = (k != null) ? k.getFall() : null;

		newSelection.put(Patient.class, p);
		newSelection.put(Fall.class, f);
		newSelection.put(Konsultation.class, k);
	}

	/**
	 * 
	 * @param template
	 * @return the currently selected element of template class
	 */
	public IPersistentObject getSelected(Class<?> template) {
		return currentSelection.get(template);
	}

}
