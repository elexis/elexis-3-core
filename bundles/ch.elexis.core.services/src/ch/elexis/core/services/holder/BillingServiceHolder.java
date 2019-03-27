package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IBillingService;

@Component
public class BillingServiceHolder {
	private static IBillingService billingService;
	
	@Reference
	public void setModelService(IBillingService billingService){
		BillingServiceHolder.billingService = billingService;
	}
	
	public static IBillingService get(){
		if (billingService == null) {
			throw new IllegalStateException("No IBillingService available");
		}
		return billingService;
	}
}
