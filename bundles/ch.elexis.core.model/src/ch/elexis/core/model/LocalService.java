package ch.elexis.core.model;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Eigenleistung;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.jpa.model.adapter.mixin.IdentifiableWithXid;
import ch.elexis.core.model.billable.AbstractOptifier;
import ch.elexis.core.model.billable.DefaultVerifier;
import ch.elexis.core.model.localservice.Constants;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.ModelUtil;
import ch.rgw.tools.Money;

public class LocalService
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Eigenleistung>
		implements IdentifiableWithXid, ILocalService {
	
	private IBillableOptifier optifier;
	private IBillableVerifier verifier;
	
	public LocalService(Eigenleistung entity){
		super(entity);
		optifier = new AbstractOptifier(CoreModelServiceHolder.get()) {
			@Override
			protected void setPrice(IBilled billed){
				billed.setPrimaryScale(100);
				billed.setSecondaryScale(100);
				billed.setFactor(1.0);
				billed.setNetPrice(getNetPrice());
				billed.setPoints(getPrice().getCents());
			}
		};
		verifier = new DefaultVerifier();
	}
	
	@Override
	public Money getPrice(){
		String priceString = getEntity().getSalePrice();
		if (StringUtils.isNumeric(priceString)) {
			return ModelUtil.getMoneyForCentString(priceString).orElse(null);
		}
		return null;
	}
	
	@Override
	public void setPrice(Money value){
		getEntity().setSalePrice(value.getCentsAsString());
	}
	
	@Override
	public Money getNetPrice(){
		String priceString = getEntity().getBasePrice();
		if (StringUtils.isNumeric(priceString)) {
			return ModelUtil.getMoneyForCentString(priceString).orElse(null);
		}
		return null;
	}
	
	@Override
	public void setNetPrice(Money value){
		getEntity().setBasePrice(value.getCentsAsString());
	}
	
	@Override
	public IBillableOptifier getOptifier(){
		return optifier;
	}
	
	@Override
	public IBillableVerifier getVerifier(){
		return verifier;
	}
	
	@Override
	public String getCodeSystemName(){
		return Constants.TYPE_NAME;
	}
	
	@Override
	public String getCode(){
		return getEntity().getCode();
	}
	
	@Override
	public void setCode(String value){
		getEntity().setCode(value);
	}
	
	@Override
	public String getText(){
		return getEntity().getDescription();
	}
	
	@Override
	public void setText(String value){
		getEntity().setDescription(value);
	}
	
	@Override
	public String getLabel(){
		return getText();
	}
	
	@Override
	public int getMinutes(){
		return 0;
	}
	
	@Override
	public void setMinutes(int value){
		throw new UnsupportedOperationException();
	}
}
