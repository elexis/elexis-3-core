package ch.elexis.core.fhir.model.impl;

import ch.elexis.core.fhir.model.IFhirModelService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class FhirModelServiceHolder {

	private static IFhirModelService fhirModelService;

	public static synchronized IFhirModelService get() {
		if (fhirModelService == null) {
			fhirModelService = OsgiServiceUtil.getServiceWait(IFhirModelService.class, 5000).get();
		}
		return fhirModelService;
	}
}
