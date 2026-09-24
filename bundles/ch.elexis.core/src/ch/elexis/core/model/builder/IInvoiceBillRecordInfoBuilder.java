package ch.elexis.core.model.builder;

import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IInvoiceBillRecordInfo;
import ch.elexis.core.services.IModelService;

public class IInvoiceBillRecordInfoBuilder extends AbstractBuilder<IInvoiceBillRecordInfo> {

	public IInvoiceBillRecordInfoBuilder(IModelService modelService, IInvoice invoice, IBilled billed) {
		super(modelService);

		object = modelService.create(IInvoiceBillRecordInfo.class);
		object.setInvoice(invoice);
		object.setBilled(billed);
	}
}
