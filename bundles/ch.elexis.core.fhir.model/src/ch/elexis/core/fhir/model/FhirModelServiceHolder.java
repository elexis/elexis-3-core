package ch.elexis.core.fhir.model;

import ch.elexis.core.services.IElexisServerService.ConnectionStatus;
import ch.elexis.core.utils.OsgiServiceUtil;

public class FhirModelServiceHolder {

	private static IFhirModelService fhirModelService;

	public static synchronized IFhirModelService get() {
		if (fhirModelService == null) {
			fhirModelService = OsgiServiceUtil.getServiceWait(IFhirModelService.class, 5000).get();
		}
		return fhirModelService;
	}

	public static boolean isAvailable() {
		return get() != null && get().getConnectionStatus() == ConnectionStatus.REMOTE;
	}
}
