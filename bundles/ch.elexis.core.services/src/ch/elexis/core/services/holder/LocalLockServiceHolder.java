package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.ILocalLockService;

public class LocalLockServiceHolder {

	public static ILocalLockService get() {
		return PortableServiceLoader.get(ILocalLockService.class);
	}
}
