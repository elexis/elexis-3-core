package ch.elexis.core.services.holder;

import java.util.Optional;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IMedicationDetailService;

public class MedicationDetailServiceHolder {

	public static Optional<IMedicationDetailService> get() {
		return PortableServiceLoader.getOptional(IMedicationDetailService.class);
	}
}
