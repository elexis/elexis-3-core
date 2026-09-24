package ch.elexis.core.ui.reminder.supplier;

import ch.elexis.core.fhir.model.IFhirModelService;
import ch.elexis.core.rcp.utils.OsgiServiceUtil;
import ch.elexis.core.services.IElexisServerService.ConnectionStatus;

public class FhirModelServiceHolder {

	private static IFhirModelService fhirModelService;

	public static synchronized IFhirModelService get() {
		if (fhirModelService == null) {
			fhirModelService = OsgiServiceUtil.getService(IFhirModelService.class).orElse(null);
		}
		return fhirModelService;
	}

	public static boolean isAvailable() {
		return get() != null && get().getConnectionStatus() == ConnectionStatus.REMOTE;
	}
}
