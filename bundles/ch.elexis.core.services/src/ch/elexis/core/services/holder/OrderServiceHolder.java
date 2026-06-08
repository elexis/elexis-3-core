package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IOrderService;

public class OrderServiceHolder {

	public static IOrderService get() {
		return PortableServiceLoader.get(IOrderService.class);
	}
}
