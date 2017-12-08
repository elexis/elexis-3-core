package ch.elexis.core.findings.ui.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.codes.ICodingService;

@Component
public class CodingServiceComponent {
	private static ICodingService codingService;
	
	@Reference(unbind = "-")
	public void setFindingsService(ICodingService codingService){
		CodingServiceComponent.codingService = codingService;
	}
	
	public static ICodingService getService(){
		return codingService;
	}
}
