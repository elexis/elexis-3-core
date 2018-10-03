package ch.elexis.core.data.service;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.data.PersistentObject;

@Component
public class StoreToStringServiceHolder {
	
	private static IStoreToStringService storeToStringService;
	
	@Reference
	public void setStoreToStringService(IStoreToStringService modelService){
		StoreToStringServiceHolder.storeToStringService = modelService;
	}
	
	public static IStoreToStringService get(){
		if (storeToStringService == null) {
			throw new IllegalStateException("No IModelService available");
		}
		return storeToStringService;
	}
	
	/**
	 * Get the elexis store to string for the object. Can handle {@link Identifiable} and
	 * {@link PersistentObject} objects.
	 * 
	 * @param object
	 * @return
	 */
	public static String getStoreToString(Object object){
		if (object instanceof PersistentObject) {
			return ((PersistentObject) object).storeToString();
		} else if (object instanceof Identifiable) {
			return StoreToStringServiceHolder.get().storeToString((Identifiable) object)
				.orElseThrow(
					() -> new IllegalStateException("No storeToString for [" + object + "]"));
		}
		throw new IllegalStateException("No storeToString for [" + object + "]");
	}
	
	/**
	 * Load the object identified by the elexis storeToString. Can handle {@link Identifiable} and
	 * {@link PersistentObject} objects.
	 * 
	 * @param storeToString
	 * @return
	 */
	public static Object getLoadFromString(String storeToString){
		Optional<Identifiable> loaded =
			StoreToStringServiceHolder.get().loadFromString(storeToString);
		if (!loaded.isPresent()) {
			return CoreHub.poFactory.createFromString(storeToString);
		} else {
			return loaded.get();
		}
	}
}
