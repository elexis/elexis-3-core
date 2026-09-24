package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IXidService;

public class XidServiceHolder {

	public static IXidService get() {
		return PortableServiceLoader.get(IXidService.class);
	}
}
