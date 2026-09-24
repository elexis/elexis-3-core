package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IAccountService;

public class AccountServiceHolder {

	public static IAccountService get() {
		return PortableServiceLoader.get(IAccountService.class);
	}
}
