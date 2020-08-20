package ch.elexis.core.data.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.Statistics;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.ICodeElementService.ContextKeys;
import ch.elexis.core.services.IContextService;
import ch.elexis.data.Konsultation;
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
	
	/**
	 * Create a context map using the selection of {@link IContextService}.
	 * 
	 * @return
	 */
	public static HashMap<Object, Object> createContext(){
		HashMap<Object, Object> ret = new HashMap<>();
		Optional<IEncounter> consultation =
			ContextServiceHolder.get().getRootContext().getTyped(IEncounter.class);
		if (consultation.isPresent()) {
			ret.put(ContextKeys.CONSULTATION, consultation.get());
		}
		Optional<ICoverage> coverage =
			ContextServiceHolder.get().getRootContext().getTyped(ICoverage.class);
		if (coverage.isPresent()) {
			ret.put(ContextKeys.COVERAGE, coverage.get());
		}
		return ret;
	}
	
	/**
	 * Create a context map using the provided {@link Konsultation}.
	 * 
	 * @param consultation
	 * @return
	 */
	public static HashMap<Object, Object> createContext(Konsultation consultation){
		HashMap<Object, Object> ret = new HashMap<>();
		if (consultation != null) {
			IEncounter encounter =
				NoPoUtil.loadAsIdentifiable(consultation, IEncounter.class).get();
			ret.put(ContextKeys.CONSULTATION, encounter);
			ret.put(ContextKeys.COVERAGE, encounter.getCoverage());
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
		ArrayList<Statistics> list = (ArrayList<Statistics>) contact.getExtInfo(key);
		if (list != null) {
			return list.stream()
				.map(sl -> StoreToStringServiceHolder.getLoadFromString(sl.getStoreToString()))
				.filter(o -> o != null && !isDeleted(o)).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}
	
	private static boolean isDeleted(Object object){
		if (object instanceof Deleteable && ((Deleteable) object).isDeleted()) {
			return true;
		} else if (object instanceof PersistentObject && !((PersistentObject) object).exists()) {
			return true;
		}
		return false;
	}
	
	/**
	 * Clear all statistics connected to the contact.
	 * 
	 * @param key
	 * @param contact
	 */
	@SuppressWarnings("unchecked")
	public static void clearStatistics(String key, IContact contact){
		ArrayList<Statistics> list = (ArrayList<Statistics>) contact.getExtInfo(key);
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
			ArrayList<Statistics> list = (ArrayList<Statistics>) contact.getExtInfo(key);
			if (list == null) {
				list = new ArrayList<Statistics>();
			}
			// limit the size of the statistics
			while (list.size() > 40) {
				list.remove(list.size() - 1);
			}
			// lookup store to string
			boolean found = false;
			for (Statistics statistic : list) {
				if (statistic.getStoreToString().equals(storeToString)) {
					statistic.increase();
					found = true;
					break;
				}
			}
			if (found == false) {
				list.add(new Statistics(storeToString)); // not found add new
			}
			Collections.sort(list);
			contact.setExtInfo(key, list);
		}
	}
}
