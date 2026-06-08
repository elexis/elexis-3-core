package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IBillingService;

public class BillingServiceHolder {

	public static IBillingService get() {
		return PortableServiceLoader.get(IBillingService.class);
	}
}
