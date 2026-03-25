package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IMedicationService;

public class MedicationServiceHolder {

	public static IMedicationService get() {
		return PortableServiceLoader.get(IMedicationService.class);
	}
}
