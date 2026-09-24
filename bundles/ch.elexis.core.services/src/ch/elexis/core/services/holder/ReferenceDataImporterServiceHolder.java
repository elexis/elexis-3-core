package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IReferenceDataImporterService;

public class ReferenceDataImporterServiceHolder {

	public static IReferenceDataImporterService get() {
		return PortableServiceLoader.get(IReferenceDataImporterService.class);
	}
}
