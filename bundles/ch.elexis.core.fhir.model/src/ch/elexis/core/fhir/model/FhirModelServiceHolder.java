package ch.elexis.core.fhir.model;

import ch.elexis.core.cdi.PortableServiceLoader;

public class FhirModelServiceHolder {

	private static IFhirModelService fhirModelService;

	public static synchronized IFhirModelService get() {
		if (fhirModelService == null) {
			fhirModelService = PortableServiceLoader.get(IFhirModelService.class);
		}
		return fhirModelService;
	}
}
