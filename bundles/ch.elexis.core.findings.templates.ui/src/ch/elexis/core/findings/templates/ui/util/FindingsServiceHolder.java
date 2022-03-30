package ch.elexis.core.findings.templates.ui.util;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.templates.service.IFindingsTemplateService;
import ch.elexis.core.services.IModelService;

@Component(service = {})
public class FindingsServiceHolder {

	public static IFindingsTemplateService findingsTemplateService;
	public static ICodingService codingService;
	public static IFindingsService findingsService;
	public static IModelService findingsModelService;

	@Reference(unbind = "-")
	public void setFindingsTemplateService(IFindingsTemplateService service) {
		findingsTemplateService = service;
	}

	@Reference(unbind = "-")
	public void setCodingService(ICodingService service) {
		codingService = service;
	}

	@Reference(unbind = "-")
	public void setFindingsService(IFindingsService service) {
		findingsService = service;
	}

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model)")
	public void setFindingsModelService(IModelService service) {
		findingsModelService = service;
	}
}
