package ch.elexis.core.findings.ui.services;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.templates.service.IFindingsTemplateService;

@Component
public class FindingsTemplateServiceComponent {
	private static IFindingsTemplateService templateService;
	
	@Reference(unbind = "-")
	public void setFindingsService(IFindingsTemplateService templateService){
		FindingsTemplateServiceComponent.templateService = templateService;
	}
	
	public static IFindingsTemplateService getService(){
		return templateService;
	}
}
