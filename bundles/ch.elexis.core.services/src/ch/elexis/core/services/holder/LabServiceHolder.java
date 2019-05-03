package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.ILabService;

@Component
public class LabServiceHolder {
	
	private static ILabService labService;
	
	@Reference
	public void setOrderService(ILabService labService){
		LabServiceHolder.labService = labService;
	}
	
	public static ILabService get(){
		if (labService == null) {
			throw new IllegalStateException("No ILabService available");
		}
		return labService;
	}
}
