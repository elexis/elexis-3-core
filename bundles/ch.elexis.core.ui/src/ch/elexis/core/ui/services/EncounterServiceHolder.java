package ch.elexis.core.ui.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IEncounterService;

@Component
public class EncounterServiceHolder {
	private static IEncounterService encounterService;
	
	@Reference
	public void setModelService(IEncounterService encounterService){
		EncounterServiceHolder.encounterService = encounterService;
	}
	
	public static IEncounterService get(){
		if (encounterService == null) {
			throw new IllegalStateException("No IEncounterService available");
		}
		return encounterService;
	}
}
