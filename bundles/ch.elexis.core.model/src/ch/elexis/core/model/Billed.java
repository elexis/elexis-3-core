package ch.elexis.core.model;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Behandlung;
import ch.elexis.core.jpa.entities.Kontakt;
import ch.elexis.core.jpa.entities.Verrechnet;
import ch.elexis.core.jpa.entities.VerrechnetCopy;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.AbstractIdModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.ExtInfoHandler;
import ch.elexis.core.model.service.holder.StoreToStringServiceHolder;
import ch.elexis.core.model.util.internal.ModelUtil;
import ch.elexis.core.model.verrechnet.Constants;
import ch.elexis.core.services.IStoreToStringContribution;
import ch.rgw.tools.Money;

public class Billed extends AbstractIdDeleteModelAdapter<Verrechnet>
		implements IdentifiableWithXid, IBilled {
	
	private ExtInfoHandler extInfoHandler;
	
	public Billed(Verrechnet entity){
		super(entity);
		extInfoHandler = new ExtInfoHandler(this);
	}
	
	@Override
	public IBillable getBillable(){
		Optional<String> storeToString = getBillableStoreToString();
		if (storeToString.isPresent()) {
			return (IBillable) StoreToStringServiceHolder.get().loadFromString(storeToString.get())
				.orElse(null);
		}
		return null;
	}
	
	private Optional<String> getBillableStoreToString(){
		String billableClass = getEntity().getKlasse();
		String billableId = getEntity().getLeistungenCode();
		if (StringUtils.isNotBlank(billableClass) && StringUtils.isNotBlank(billableId)) {
			return Optional.of(billableClass + IStoreToStringContribution.DOUBLECOLON + billableId);
		}
		return Optional.empty();
	}
	
	@Override
	public void setBillable(IBillable value){
		String storeToString = StoreToStringServiceHolder.get().storeToString(value).orElseThrow(
			() -> new IllegalStateException("Could not get store to string for [" + value + "]"));
		String[] split = storeToString.split(IStoreToStringContribution.DOUBLECOLON);
		if (split.length > 1) {
			getEntity().setKlasse(split[0]);
			getEntity().setLeistungenCode(split[1]);
		}
	}
	
	@Override
	public IEncounter getEncounter(){
		if (getEntity().getBehandlung() != null) {
			return ModelUtil.getAdapter(getEntity().getBehandlung(), IEncounter.class);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void setEncounter(IEncounter value){
		if (value instanceof AbstractIdModelAdapter) {
			getEntity().setBehandlung(((AbstractIdModelAdapter<Behandlung>) value).getEntity());
		} else if (value == null) {
			getEntity().setBehandlung(null);
		}
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
		if (value % 1 == 0) {
			// integer
			getEntity().setZahl((int) value);
			setSecondaryScale(100);
		} else {
			if (!isChangedPrice()) {
				// double
				getEntity().setZahl(1);
				int scale2 = (int) Math.round(value * 100);
				setSecondaryScale(scale2);
			} else {
				throw new IllegalStateException(
					"Can not set non integer amount if price was changed");
			}
		}
	}
	
	@Override
	public Money getPrice(){
		return new Money(getPoints()).multiply(getFactor());
	}
	
	@Override
	public void setPrice(Money value){
		if (isNonIntegerAmount()) {
			throw new IllegalStateException("Can not set price if non integer amount was set");
		} else {
			setExtInfo(Constants.FLD_EXT_CHANGEDPRICE, "true");
			setPoints(value.getCents());
			setSecondaryScale(100);
		}
	}
	
	@Override
	public Money getNetPrice(){
		return new Money(getEntity().getEk_kosten());
	}
	
	@Override
	public void setNetPrice(Money value){
		if (value != null) {
			getEntity().setEk_kosten(value.getCents());
		} else {
			getEntity().setEk_kosten(0);
		}
	}
	
	@Override
	public String getText(){
		return getEntity().getLeistungenText();
	}
	
	@Override
	public void setText(String value){
		getEntity().setLeistungenText(value);
		
	}
	
	@Override
	public int getPoints(){
		return getEntity().getVk_tp();
	}
	
	@Override
	public void setPoints(int value){
		getEntity().setVk_tp(value);
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
		getEntity().setVk_scale(Double.toString(value));
	}
	
	@Override
	public int getPrimaryScale(){
		return getEntity().getScale();
	}
	
	@Override
	public void setPrimaryScale(int value){
		getEntity().setScale(value);
	}
	
	@Override
	public int getSecondaryScale(){
		return getEntity().getScale2();
	}
	
	@Override
	public void setSecondaryScale(int value){
		getEntity().setScale2(value);
	}
	
	@Override
	public Object getExtInfo(Object key){
		return extInfoHandler.getExtInfo(key);
	}
	
	@Override
	public void setExtInfo(Object key, Object value){
		extInfoHandler.setExtInfo(key, value);
	}
	
	@Override
	public String getCode(){
		IBillable billable = getBillable();
		return billable != null ? billable.getCode() : getBillableStoreToString().orElse("?");
	}
	
	@Override
	public Money getTotal(){
		// do not use getAmount here, as the changed amount is included via secondary scale
		return getPrice().multiply(getPrimaryScaleFactor()).multiply(getSecondaryScaleFactor())
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
	public double getPrimaryScaleFactor(){
		if (getPrimaryScale() == 0) {
			return 1.0;
		}
		return ((double) getPrimaryScale()) / 100.0;
	}
	
	@Override
	public double getSecondaryScaleFactor(){
		if (getSecondaryScale() == 0) {
			return 1.0;
		}
		return ((double) getSecondaryScale()) / 100.0;
	}
	
	@Override
	public IContact getBiller(){
		return ModelUtil.getAdapter(getEntity().getUser(), IContact.class);
	}
	
	@Override
	public void setBiller(IContact value){
		if (value instanceof AbstractIdDeleteModelAdapter) {
			getEntity().setUser((Kontakt) ((AbstractIdDeleteModelAdapter<?>) value).getEntity());
		} else if (value == null) {
			getEntity().setUser(null);
		}
	}
	
	@Override
	public void copy(IInvoiceBilled to){
		if (to instanceof AbstractIdDeleteModelAdapter) {
			// IInvoiceBilled do not support set operations, so copy properties of the entities
			@SuppressWarnings("unchecked")
			VerrechnetCopy toEntity =
				((AbstractIdDeleteModelAdapter<VerrechnetCopy>) to).getEntity();
			toEntity.setKlasse(getEntity().getKlasse());
			toEntity.setLeistungenCode(getEntity().getLeistungenCode());
			toEntity.setLeistungenText(getEntity().getLeistungenText());
			toEntity.setZahl(getEntity().getZahl());
			toEntity.setEk_kosten(getEntity().getEk_kosten());
			toEntity.setVk_tp(getEntity().getVk_tp());
			toEntity.setVk_scale(getEntity().getVk_scale());
			toEntity.setVk_preis(getEntity().getVk_preis());
			toEntity.setScale(getEntity().getScale());
			toEntity.setScale2(getEntity().getScale2());
			
			toEntity.setBehandlung(getEntity().getBehandlung());
			toEntity.setExtInfo(getEntity().getExtInfo());
			toEntity.setUser(getEntity().getUser());
		}
	}
}
