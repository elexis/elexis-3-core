package ch.elexis.core.model;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.VerrechnetCopy;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.service.holder.StoreToStringServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.model.verrechnet.Constants;
import ch.elexis.core.services.IStoreToStringContribution;
import ch.rgw.tools.Money;

public class InvoiceBilled extends AbstractIdDeleteModelAdapter<VerrechnetCopy>
		implements IdentifiableWithXid, IInvoiceBilled {
	
	private ExtInfoHandler extInfoHandler;
	
	public InvoiceBilled(VerrechnetCopy entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public IBillable getBillable(){
		String billableClass = getEntity().getKlasse();
		String billableId = getEntity().getLeistungenCode();
		if (StringUtils.isNotBlank(billableClass) && StringUtils.isNotBlank(billableId)) {
			return (IBillable) StoreToStringServiceHolder.get()
				.loadFromString(billableClass + IStoreToStringContribution.DOUBLECOLON + billableId)
				.orElse(null);
		}
		return null;
	}
	
	@Override
	public void setBillable(IBillable value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public IEncounter getEncounter(){
		if (getEntity().getBehandlung() != null) {
			return ModelUtil.getAdapter(getEntity().getBehandlung(), IEncounter.class);
		}
		return null;
	}
	
	@Override
	public void setEncounter(IEncounter value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double getAmount(){
		if (getSecondaryScale() == 100) {
			return getEntity().getZahl();
		}
		return getSecondaryScale() / 100d;
	}
	
	@Override
	public void setAmount(double value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Money getPrice(){
		return new Money(getPoints()).multiply(getFactor());
	}
	
	@Override
	public void setPrice(Money value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Money getNetPrice(){
		return new Money(getEntity().getEk_kosten());
	}
	
	@Override
	public void setNetPrice(Money value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getText(){
		return getEntity().getLeistungenText();
	}
	
	@Override
	public void setText(String value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getPoints(){
		return getEntity().getVk_tp();
	}
	
	@Override
	public void setPoints(int value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public double getFactor(){
		String scaleString = getEntity().getVk_scale();
		if (scaleString != null && !scaleString.isEmpty()) {
			try {
				return Double.parseDouble(scaleString);
			} catch (NumberFormatException e) {
				// ignore
			}
		}
		return 1.0;
	}
	
	@Override
	public void setFactor(double value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getPrimaryScale(){
		return getEntity().getScale();
	}
	
	@Override
	public void setPrimaryScale(int value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public int getSecondaryScale(){
		return getEntity().getScale2();
	}
	
	@Override
	public void setSecondaryScale(int value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public Object getExtInfo(Object key){
		return extInfoHandler.getExtInfo(key);
	}
	
	@Override
	public void setExtInfo(Object key, Object value){
		throw new UnsupportedOperationException();
	}
	
	@Override
	public String getCode(){
		return getEntity().getLeistungenCode();
	}
	
	@Override
	public Money getTotal(){
		return getPrice().multiply(getPrimaryScale() / 100d).multiply(getSecondaryScale() / 100d)
			.multiply(getEntity().getZahl());
	}
	
	@Override
	public boolean isChangedPrice(){
		Object changedPrice = getExtInfo(Constants.FLD_EXT_CHANGEDPRICE);
		if (changedPrice instanceof String) {
			return ((String) changedPrice).equalsIgnoreCase("true");
		} else if (changedPrice instanceof Boolean) {
			return (Boolean) changedPrice;
		}
		return false;
	}
	
	@Override
	public boolean isNonIntegerAmount(){
		if (isChangedPrice()) {
			return false;
		} else {
			return getSecondaryScale() != 100;
		}
	}
	
	@Override
	public IInvoice getInvoice(){
		return ModelUtil.getAdapter(getEntity().getInvoice(), IInvoice.class);
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
}
