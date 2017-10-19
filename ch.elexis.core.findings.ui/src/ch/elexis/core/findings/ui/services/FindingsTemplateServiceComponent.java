package ch.elexis.core.findings.ui.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.templates.service.FindingsTemplateService;

@Component
public class FindingsTemplateServiceComponent {
	private static FindingsTemplateService templateService;
	
	@Reference(unbind = "-")
	public void setFindingsService(FindingsTemplateService templateService){
		FindingsTemplateServiceComponent.templateService = templateService;
	}
	
	public static FindingsTemplateService getService(){
		return templateService;
	}
}
