package ch.elexis.core.model;

import java.time.LocalDate;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.services.holder.AccountServiceHolder;
import ch.rgw.tools.Money;

public class AccountTransaction
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.AccountTransaction>
		implements IdentifiableWithXid, IAccountTransaction {
	
	public AccountTransaction(ch.elexis.core.jpa.entities.AccountTransaction entity){
		super(entity);
	}
	
	@Override
	public IInvoice getInvoice(){
		if (getEntity().getInvoice() != null) {
			return ModelUtil.getAdapter(getEntity().getInvoice(), IInvoice.class);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setInvoice(IInvoice value){
		if (value instanceof AbstractIdModelAdapter) {
			// TODO modify patient if opposite reference is available
			getEntity().setInvoice(
				((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Invoice>) value).getEntity());
		}
	}
	
	@Override
	public IPayment getPayment(){
		if (getEntity().getZahlung() != null) {
			return ModelUtil.getAdapter(getEntity().getZahlung(), IPayment.class);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setPayment(IPayment value){
		if (value instanceof AbstractIdModelAdapter) {
			getEntity().setZahlung(
				((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Zahlung>) value).getEntity());
		} else if (value == null) {
			getEntity().setZahlung(null);
		}
	}
	
	@Override
	public IPatient getPatient(){
		if (getEntity().getPatient() != null) {
			return ModelUtil.getAdapter(getEntity().getPatient(), IPatient.class);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setPatient(IPatient value){
		if (value instanceof AbstractIdModelAdapter) {
			// TODO modify patient if opposite reference is available
			getEntity().setPatient(
				((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Kontakt>) value).getEntity());
		}
	}
	
	@Override
	public Money getAmount(){
		return ch.elexis.core.model.util.ModelUtil.getMoneyForCentString(getEntity().getAmount())
			.orElse(null);
	}
	
	@Override
	public void setAmount(Money value){
		if (value != null) {
			getEntity().setAmount(value.getCentsAsString());
		} else {
			getEntity().setAmount(null);
		}
	}
	
	@Override
	public String getRemark(){
		return getEntity().getRemark();
	}
	
	@Override
	public void setRemark(String value){
		getEntity().setRemark(value);
	}
	
	@Override
	public IAccount getAccount(){
		String accountNumeric = getEntity().getAccount();
		if (accountNumeric != null && !accountNumeric.isEmpty()) {
			try {
				accountNumeric = accountNumeric.trim(); // care for postgres adding spaces
				return AccountServiceHolder.get().getAccounts()
					.get(Integer.parseInt(accountNumeric));
			} catch (NumberFormatException e) {}
		}
		return AccountServiceHolder.get().getUnknown();
	}
	
	@Override
	public void setAccount(IAccount value){
		getEntity().setAccount(Integer.toString(value.getNumeric()));
	}
	
	@Override
	public LocalDate getDate(){
		return getEntity().getDate();
	}
	
	@Override
	public void setDate(LocalDate value){
		getEntity().setDate(value);
	}
}
