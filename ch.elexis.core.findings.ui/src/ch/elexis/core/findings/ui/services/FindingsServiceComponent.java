package ch.elexis.core.findings.ui.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.IFindingsService;

@Component
public class FindingsServiceComponent {
	private static IFindingsService findingsService;
	
	@Reference(unbind = "-")
	public void setFindingsService(IFindingsService findingsServcie){
		FindingsServiceComponent.findingsService = findingsServcie;
	}
	
	public static IFindingsService getService(){
		return findingsService;
	}
}
