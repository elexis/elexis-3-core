package ch.elexis.core.data.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Kontakt.statL;
import ch.elexis.data.PersistentObject;

@Component(service = {})
public class CodeElementServiceHolder {
	
	private static HashMap<Object, Object> emptyMap = new HashMap<>();
	
	private static ICodeElementService elementService;
	
	@Reference(unbind = "-")
	public void setCodeElementService(ICodeElementService elementService){
		CodeElementServiceHolder.elementService = elementService;
	}
	
	public static ICodeElementService get(){
		return elementService;
	}
	
	public static HashMap<Object, Object> createContext(){
		HashMap<Object, Object> ret = new HashMap<>();
		Optional<Konsultation> consultation =
			ContextServiceHolder.get().getRootContext().getTyped(Konsultation.class);
		if (consultation.isPresent()) {
			ret.put(ContextKeys.CONSULTATION, consultation.get());
		}
		Optional<Fall> coverage = ContextServiceHolder.get().getRootContext().getTyped(Fall.class);
		if (coverage.isPresent()) {
			ret.put(ContextKeys.COVERAGE, coverage.get());
		}
		return ret;
	}
	
	public static HashMap<Object, Object> emtpyContext(){
		return emptyMap;
	}
	
	/**
	 * Get a sorted list with the elements of the statistic key.
	 * 
	 * @param key
	 * @param contact
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> getStatistics(String key, IContact contact){
		ArrayList<statL> list = (ArrayList<statL>) contact.getExtInfo(key);
		ArrayList<Object> ret = new ArrayList<>();
		if (list != null) {
			for (statL sl : list) {
				Object loaded = StoreToStringServiceHolder.getLoadFromString(sl.v);
				if (loaded != null) {
					ret.add(loaded);
				}
			}
		}
		return ret;
	}
	
	/**
	 * Clear all statistics connected to the contact.
	 * 
	 * @param key
	 * @param contact
	 */
	@SuppressWarnings("unchecked")
	public static void clearStatistics(String key, IContact contact){
		ArrayList<statL> list = (ArrayList<statL>) contact.getExtInfo(key);
		if (list != null) {
			list.clear();
			contact.setExtInfo(key, list);
		}
	}
	
	/**
	 * Add 1 to the statistics for the object matching the key and the element. Element matching is
	 * done using the Elexis store to string concept.
	 * 
	 * @param key
	 * @param element
	 * @param contact
	 */
	@SuppressWarnings("unchecked")
	public static void updateStatistics(String key, Object element, IContact contact){
		String storeToString = null;
		if (element instanceof Identifiable) {
			storeToString =
				StoreToStringServiceHolder.get().storeToString((Identifiable) element).orElse(null);
		} else if (element instanceof PersistentObject) {
			storeToString = ((PersistentObject) element).storeToString();
		}
		
		if (storeToString != null) {
			// get or start new list of statL
			ArrayList<statL> list = (ArrayList<statL>) contact.getExtInfo(key);
			if (list == null) {
				list = new ArrayList<statL>();
			}
			// limit the size of the statistics
			while (list.size() > 40) {
				list.remove(list.size() - 1);
			}
			// lookup store to string
			boolean found = false;
			for (statL c : list) {
				if (c.v.equals(storeToString)) {
					c.c++; // found add 1
					found = true;
					break;
				}
			}
			if (found == false) {
				list.add(new statL(storeToString)); // not found add new
			}
			Collections.sort(list);
			contact.setExtInfo(key, list);
		}
	}
}
