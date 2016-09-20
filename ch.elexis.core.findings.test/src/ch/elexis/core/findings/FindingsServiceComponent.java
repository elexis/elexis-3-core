package ch.elexis.core.findings;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component
public class FindingsServiceComponent {
	private static IFindingsService findingsService;
	
	@Reference
	public void setIFindingsService(IFindingsService findingsService) {
		FindingsServiceComponent.findingsService = findingsService;
	}
	
	public void unsetIFindingsService(IFindingsService findingsService) {
		FindingsServiceComponent.findingsService = null;
	}
	
	public static IFindingsService getService(){
		if (findingsService == null) {
			throw new IllegalStateException("No IFindingService set");
		}
		return findingsService;
	}
}
