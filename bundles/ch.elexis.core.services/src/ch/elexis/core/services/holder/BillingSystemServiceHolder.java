package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IBillingSystemService;

public class BillingSystemServiceHolder {

	public static IBillingSystemService get() {
		return PortableServiceLoader.get(IBillingSystemService.class);
	}
}
