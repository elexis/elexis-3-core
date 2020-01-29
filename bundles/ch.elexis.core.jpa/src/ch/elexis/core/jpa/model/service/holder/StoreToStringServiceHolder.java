package ch.elexis.core.jpa.model.service.holder;

import java.util.Optional;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IStoreToStringService;

@Component
public class StoreToStringServiceHolder {
	
	private static IStoreToStringService storeToStringService;
	
	@Reference
	public void setStoreToStringService(IStoreToStringService storeToStringService){
		StoreToStringServiceHolder.storeToStringService = storeToStringService;
	}
	
	public static IStoreToStringService get(){
		if (storeToStringService == null) {
			throw new IllegalStateException("No IStoreToStringService available");
		}
		return storeToStringService;
	}
	
	public static boolean isPresent(){
		return storeToStringService != null;
	}
	
	public static Optional<String> getStoreToString(Identifiable identifiable){
		return isPresent() ? storeToStringService.storeToString(identifiable) : Optional.empty();
	}
}
