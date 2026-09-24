package ch.elexis.core.services.holder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.model.Deleteable;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.model.Statistics;
import ch.elexis.core.services.ICodeElementService;
import ch.elexis.core.services.IStoreToStringContribution;

public class CodeElementServiceHolder {

	public static ICodeElementService get() {
		return PortableServiceLoader.get(ICodeElementService.class);
	}

	/**
	 * Create a default context using the current typed selection of encounter and
	 * coverage from the {@link ContextServiceHolder}s root context.
	 *
	 * @return
	 */
	public static Map<Object, Object> createContext() {
		return CodeElementServiceHolder.get().createContext();
	}

	/**
	 * Create a context using the provided encounter.
	 *
	 * @param encounter
	 * @return
	 */
	public static Map<Object, Object> createContext(IEncounter encounter) {
		return CodeElementServiceHolder.get().createContext(encounter);
	}

	/**
	 * Get a sorted list with the elements of the statistic key.
	 *
	 * @param key
	 * @param contact
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<Object> getStatistics(String key, IContact contact) {
		CoreModelServiceHolder.get().refresh(contact);
		ArrayList<Statistics> list = (ArrayList<Statistics>) contact.getExtInfo(key);
		if (list != null) {
			return list.stream()
					.map(sl -> StoreToStringServiceHolder.get().loadFromString(sl.getStoreToString()).orElse(null))
					.filter(o -> o != null && !isDeleted(o)).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private static boolean isDeleted(Object object) {
		if (object instanceof Deleteable && ((Deleteable) object).isDeleted()) {
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
	public static void clearStatistics(String key, IContact contact) {
		ArrayList<Statistics> list = (ArrayList<Statistics>) contact.getExtInfo(key);
		if (list != null) {
			list.clear();
			contact.setExtInfo(key, list);
			CoreModelServiceHolder.get().save(contact);
		}
	}

	/**
	 * Add 1 to the statistics for the object matching the key and the element.
	 * Element matching is done using the Elexis store to string concept.
	 *
	 * @param element
	 * @param contact
	 */
	@SuppressWarnings("unchecked")
	public static void updateStatistics(Object element, IContact contact) {
		if (element != null && contact != null) {
			String storeToString = null;
			if (element instanceof Identifiable) {
				storeToString = StoreToStringServiceHolder.get().storeToString((Identifiable) element).orElse(null);
			}

			if (storeToString != null) {
				String key = storeToString.split(IStoreToStringContribution.DOUBLECOLON)[0];
				// get or start new list of statL
				ArrayList<Statistics> list = (ArrayList<Statistics>) contact.getExtInfo(key);
				if (list == null) {
					list = new ArrayList<>();
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
				CoreModelServiceHolder.get().save(contact);
			}
		}
	}
}
