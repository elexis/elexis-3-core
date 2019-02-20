package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IBillingSystemService;

@Component
public class BillingSystemServiceHolder {
	private static IBillingSystemService billingSystemService;
	
	@Reference
	public void setBillingSystemService(IBillingSystemService billingSystemService){
		BillingSystemServiceHolder.billingSystemService = billingSystemService;
	}
	
	public static IBillingSystemService get(){
		return billingSystemService;
	}
}
