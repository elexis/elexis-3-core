package ch.elexis.core.findings.templates.ui.util;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;

@Component(service = {})
public class FindingsServiceHolder {
	
	public static FindingsTemplateService findingsTemplateService;
	public static ICodingService codingService;
	
	@Reference(unbind = "-")
	public synchronized void setFindingsTemplateService(FindingsTemplateService service){
		findingsTemplateService = service;
	}
	
	@Reference(unbind = "-")
	public synchronized void setCodingService(ICodingService service){
		codingService = service;
	}
	
}
