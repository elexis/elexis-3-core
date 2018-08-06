package ch.elexis.core.data.service;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

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
			throw new IllegalStateException("No IModelService available");
		}
		return storeToStringService;
	}
}
