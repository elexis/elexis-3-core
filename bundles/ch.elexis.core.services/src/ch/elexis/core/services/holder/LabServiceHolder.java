package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.ILabService;

public class LabServiceHolder {

	public static ILabService get() {
		return PortableServiceLoader.get(ILabService.class);
	}
}
