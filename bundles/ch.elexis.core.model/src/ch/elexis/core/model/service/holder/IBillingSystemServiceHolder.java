package ch.elexis.core.model.service.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IBillingSystemService;

@Component
public class IBillingSystemServiceHolder {
	
	private static IBillingSystemService service;
	
	@Reference
	public void setModelService(IBillingSystemService service){
		IBillingSystemServiceHolder.service = service;
	}
	
	public static IBillingSystemService get(){
		if (service == null) {
			throw new IllegalStateException("No IModelService available");
		}
		return service;
	}
}
