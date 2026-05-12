package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IStockService;

public class StockServiceHolder {

	public static IStockService get() {
		return PortableServiceLoader.get(IStockService.class);
	}
}
