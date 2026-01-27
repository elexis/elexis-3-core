package ch.elexis.core.findings.util;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IModelService;

@Component
public class FindingsModelServiceHolder {

	private static IModelService modelService;

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.findings.model)")
	public void setModelService(IModelService modelService) {
		FindingsModelServiceHolder.modelService = modelService;
	}

	public static IModelService get() {
		return modelService;
	}
}
