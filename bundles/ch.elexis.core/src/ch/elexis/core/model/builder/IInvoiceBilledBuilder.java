package ch.elexis.core.model.builder;

import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IInvoiceBilled;
import ch.elexis.core.services.IModelService;

public class IInvoiceBilledBuilder extends AbstractBuilder<IInvoiceBilled> {
	
	public IInvoiceBilledBuilder(IModelService modelService, IInvoice invoice, IBilled billed){
		super(modelService);
		
		object = modelService.create(IInvoiceBilled.class);
		billed.copy(object);
		object.setInvoice(invoice);
	}
}
