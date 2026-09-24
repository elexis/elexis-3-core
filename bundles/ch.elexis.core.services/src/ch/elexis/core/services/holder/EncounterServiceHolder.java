package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IEncounterService;

public class EncounterServiceHolder {

	public static IEncounterService get() {
		return PortableServiceLoader.get(IEncounterService.class);
	}
}
