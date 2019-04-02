package ch.elexis.core.model.builder;

import java.time.LocalDate;

import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPayment;
import ch.elexis.core.services.IModelService;
import ch.rgw.tools.Money;

public class IAccountTransactionBuilder extends AbstractBuilder<IAccountTransaction> {
	
	public IAccountTransactionBuilder(IModelService modelService, IInvoice invoice,
		IPatient patient, Money amount, LocalDate date, String remark){
		super(modelService);
		
		object = modelService.create(IAccountTransaction.class);
		object.setInvoice(invoice);
		object.setPatient(patient);
		object.setAmount(amount);
		object.setDate(date);
		object.setRemark(remark);
	}
	
	public IAccountTransactionBuilder(IModelService modelService, IPayment payment){
		super(modelService);
		
		object = modelService.create(IAccountTransaction.class);
		object.setInvoice(payment.getInvoice());
		object.setPatient(payment.getInvoice().getCoverage().getPatient());
		object.setAmount(payment.getAmount());
		object.setDate(payment.getDate());
		object.setRemark(payment.getRemark());
		object.setPayment(payment);
	}
	
}
