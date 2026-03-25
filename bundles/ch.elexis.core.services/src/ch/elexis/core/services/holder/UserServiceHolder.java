package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IUserService;

public class UserServiceHolder {

	public static IUserService get() {
		return PortableServiceLoader.get(IUserService.class);
	}
}
