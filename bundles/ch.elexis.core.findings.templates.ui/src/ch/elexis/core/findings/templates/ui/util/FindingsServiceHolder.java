package ch.elexis.core.findings.templates.ui.util;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.templates.service.IFindingsTemplateService;

@Component(service = {})
public class FindingsServiceHolder {
	
	public static IFindingsTemplateService findingsTemplateService;
	public static ICodingService codingService;
	public static IFindingsService findingsService;
	
	@Reference(unbind = "-")
	public synchronized void setFindingsTemplateService(IFindingsTemplateService service){
		findingsTemplateService = service;
	}
	
	@Reference(unbind = "-")
	public synchronized void setCodingService(ICodingService service){
		codingService = service;
	}
	
	@Reference(unbind = "-")
	public synchronized void setFindingsService(IFindingsService service){
		findingsService = service;
	}
	
}
