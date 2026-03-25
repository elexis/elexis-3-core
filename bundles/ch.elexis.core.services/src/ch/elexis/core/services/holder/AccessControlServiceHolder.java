package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IAccessControlService;

public class AccessControlServiceHolder {

	public static IAccessControlService get() {
		return PortableServiceLoader.get(IAccessControlService.class);
	}

}
