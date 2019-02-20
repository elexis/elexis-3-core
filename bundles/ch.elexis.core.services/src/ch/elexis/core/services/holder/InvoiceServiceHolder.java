package ch.elexis.core.services.holder;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.services.IInvoiceService;

@Component
public class InvoiceServiceHolder {
	private static IInvoiceService invoiceService;
	
	@Reference
	public void setInvoiceService(IInvoiceService invoiceService){
		InvoiceServiceHolder.invoiceService = invoiceService;
	}
	
	public static IInvoiceService get(){
		return invoiceService;
	}
}
