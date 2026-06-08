package ch.elexis.core.services.holder;

import ch.elexis.core.cdi.PortableServiceLoader;
import ch.elexis.core.services.IInvoiceService;

public class InvoiceServiceHolder {

	public static IInvoiceService get() {
		return PortableServiceLoader.get(IInvoiceService.class);
	}
}
