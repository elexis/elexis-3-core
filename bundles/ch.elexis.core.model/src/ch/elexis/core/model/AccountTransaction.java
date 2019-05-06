package ch.elexis.core.model;

import java.time.LocalDate;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
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
			getEntity().setInvoice(
				((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Invoice>) value).getEntity());
		} else if (value == null) {
			getEntity().setInvoice(null);
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
			getEntity().setPatient(
				((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Kontakt>) value).getEntity());
		} else if (value == null) {
			getEntity().setPatient(null);
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
	public String getAccount(){
		return getEntity().getAccount();
	}
	
	@Override
	public void setAccount(String value){
		getEntity().setAccount(value);
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
