package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.ICoverageService;

public class CoverageServiceHolder {

	public static ICoverageService get() {
		return PortableServiceLoader.get(ICoverageService.class);
	}
}
