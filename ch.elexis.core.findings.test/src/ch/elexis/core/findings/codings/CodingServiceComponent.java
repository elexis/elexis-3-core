package ch.elexis.core.findings.codings;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.codes.ICodingService;

@Component
public class CodingServiceComponent {
	private static ICodingService codingService;
	
	@Reference
	public void setIFindingsService(ICodingService findingsService){
		CodingServiceComponent.codingService = findingsService;
	}
	
	public void unsetIFindingsService(ICodingService findingsService){
		CodingServiceComponent.codingService = null;
	}
	
	public static ICodingService getService(){
		if (codingService == null) {
			throw new IllegalStateException("No ICodingService set");
		}
		return codingService;
	}
}
