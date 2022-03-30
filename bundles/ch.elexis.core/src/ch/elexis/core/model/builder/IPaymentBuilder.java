package ch.elexis.core.model.builder;

import java.time.LocalDate;

import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IPayment;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.Money;

public class IPaymentBuilder extends AbstractBuilder<IPayment> {

	public IPaymentBuilder(IModelService modelService, IInvoice invoice, Money amount, String remark) {
		super(modelService);

		object = modelService.create(IPayment.class);
		object.setInvoice(invoice);
		object.setAmount(amount);
		object.setDate(LocalDate.now());
		object.setRemark(remark);
	}
}
