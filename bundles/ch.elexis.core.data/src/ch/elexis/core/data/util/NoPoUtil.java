package ch.elexis.core.data.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.PersistentObjectFactory;

/**
 * Utility class for compatibility between the Elexis persistence implementations
 * {@link PersistentObject} and JPA based {@link Identifiable}.
 * 
 * @author thomas
 *
 */
public class NoPoUtil {
	
	private static PersistentObjectFactory poFactory = new PersistentObjectFactory();
	
	/**
	 * Load {@link PersistentObject} implementation for the provided {@link Identifiable} using the
	 * {@link PersistentObjectFactory} and the {@link IStoreToStringService}.
	 * 
	 * @param identifiable
	 * @return
	 */
	public static PersistentObject loadAsPersistentObject(Identifiable identifiable){
		if (identifiable != null) {
			Optional<String> storeToString =
				StoreToStringServiceHolder.get().storeToString(identifiable);
			if (storeToString.isPresent()) {
				PersistentObject ret = poFactory.createFromString(storeToString.get());
				if (ret != null) {
					return ret;
				}
			}
			throw new IllegalStateException("Could not load [" + identifiable + "] ["
				+ storeToString.orElse("?") + "] as PersistentObject");
		}
		return null;
	}
	
	/**
	 * Load {@link PersistentObject} implementations for the provided {@link Identifiable}s using
	 * the {@link PersistentObjectFactory} and the {@link IStoreToStringService}.
	 * 
	 * @param identifiable
	 * @return
	 */
	public static List<PersistentObject> loadAsPersistentObject(List<Identifiable> identifiables){
		if (identifiables != null && !identifiables.isEmpty()) {
			List<PersistentObject> ret = new ArrayList<>();
			for (Identifiable identifiable : identifiables) {
				ret.add(loadAsPersistentObject(identifiable));
			}
			return ret;
		}
		return Collections.emptyList();
	}
	
	/**
	 * Load {@link PersistentObject} implementations for the provided {@link Identifiable}s using
	 * the {@link PersistentObjectFactory} and the {@link IStoreToStringService}.
	 * 
	 * @param identifiable
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] loadAsPersistentObject(Identifiable[] identifiables, Class<T> type){
		if (identifiables != null && identifiables.length > 0) {
			T[] ret = (T[]) Array.newInstance(type, identifiables.length);
			for (int i = 0; i < identifiables.length; i++) {
				ret[i] = (T) loadAsPersistentObject(identifiables[i]);
			}
			return ret;
		}
		return (T[]) Array.newInstance(type, 0);
	}
}
