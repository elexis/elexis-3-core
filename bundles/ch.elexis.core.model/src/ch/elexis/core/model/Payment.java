package ch.elexis.core.model;

import java.time.LocalDate;

import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.rgw.tools.Money;

public class Payment
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Zahlung>
		implements IdentifiableWithXid, IPayment {
	
	public Payment(ch.elexis.core.jpa.entities.Zahlung entity){
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
			getEntityMarkDirty().setInvoice(
				((AbstractIdModelAdapter<ch.elexis.core.jpa.entities.Invoice>) value).getEntity());
		} else if (value == null) {
			getEntityMarkDirty().setInvoice(null);
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
			getEntityMarkDirty().setAmount(value.getCentsAsString());
		} else {
			getEntityMarkDirty().setAmount(null);
		}
	}
	
	@Override
	public String getRemark(){
		return getEntity().getRemark();
	}
	
	@Override
	public void setRemark(String value){
		getEntityMarkDirty().setRemark(value);
	}
	
	@Override
	public LocalDate getDate(){
		return getEntity().getDate();
	}
	
	@Override
	public void setDate(LocalDate value){
		getEntityMarkDirty().setDate(value);
	}
}
