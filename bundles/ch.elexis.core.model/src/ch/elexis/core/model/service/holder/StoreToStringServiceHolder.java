package ch.elexis.core.model.service.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IStoreToStringService;

@Component
public class StoreToStringServiceHolder {
	
	private static IStoreToStringService storeToStringService;
	
	@Reference
	public void setStoreToStringService(IStoreToStringService modelService){
		StoreToStringServiceHolder.storeToStringService = modelService;
	}
	
	public static IStoreToStringService get(){
		if (storeToStringService == null) {
			throw new IllegalStateException("No IStoreToStringService available");
		}
		return storeToStringService;
	}
	
	public static String getStoreToString(Object object){
		if (object instanceof Identifiable) {
			return StoreToStringServiceHolder.get().storeToString((Identifiable) object)
				.orElseThrow(
					() -> new IllegalStateException("No storeToString for [" + object + "]"));
		}
		throw new IllegalStateException("No storeToString for [" + object + "]");
	}
}
