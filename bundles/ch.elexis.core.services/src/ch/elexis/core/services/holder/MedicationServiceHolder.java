package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IMedicationService;

@Component
public class MedicationServiceHolder {
	private static IMedicationService medicationService;
	
	@Reference
	public void setContextService(IMedicationService medicationService){
		MedicationServiceHolder.medicationService = medicationService;
	}
	
	public static IMedicationService get(){
		return medicationService;
	}
}
