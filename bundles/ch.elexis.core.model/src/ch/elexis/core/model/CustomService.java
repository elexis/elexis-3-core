package ch.elexis.core.model;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.jpa.entities.Eigenleistung;
import ch.elexis.core.jpa.model.adapter.AbstractIdDeleteModelAdapter;
import ch.elexis.core.model.billable.AbstractOptifier;
import ch.elexis.core.model.billable.DefaultVerifier;
import ch.elexis.core.model.localservice.Constants;
import ch.elexis.core.model.service.holder.CoreModelServiceHolder;
import ch.elexis.core.model.util.ModelUtil;
import ch.rgw.tools.Money;

public class CustomService
		extends AbstractIdDeleteModelAdapter<ch.elexis.core.jpa.entities.Eigenleistung>
		implements IdentifiableWithXid, ICustomService {
	
	private static IBillableOptifier<CustomService> optifier;
	private static IBillableVerifier verifier;
	
	private static Money noMoney = new Money();
	
	public CustomService(Eigenleistung entity){
		super(entity);
	}
	
	@Override
	public Money getPrice(){
		String priceString = getEntity().getSalePrice();
		if (priceString != null) {
			if (StringUtils.isNumeric(priceString)) {
				return ModelUtil.getMoneyForCentString(priceString).orElse(noMoney);
			} else if (priceString.matches("[0-9\\.]+")) {
				return ModelUtil.getMoneyForPriceString(priceString).orElse(noMoney);
			}
		}
		return noMoney;
	}
	
	@Override
	public void setPrice(Money value){
		getEntity().setSalePrice(value.getCentsAsString());
	}
	
	@Override
	public Money getNetPrice(){
		String priceString = getEntity().getBasePrice();
		if (StringUtils.isNumeric(priceString)) {
			return ModelUtil.getMoneyForCentString(priceString).orElse(noMoney);
		} else if (priceString.matches("[0-9\\.]+")) {
			return ModelUtil.getMoneyForPriceString(priceString).orElse(noMoney);
		}
		return noMoney;
	}
	
	@Override
	public void setNetPrice(Money value){
		getEntity().setBasePrice(value.getCentsAsString());
	}
	
	@Override
	public synchronized IBillableOptifier<CustomService> getOptifier(){
		if (optifier == null) {
			optifier = new AbstractOptifier<CustomService>(CoreModelServiceHolder.get()) {
				
				@Override
				protected void setPrice(CustomService billable, IBilled billed){
					billed.setFactor(1.0);
					billed.setNetPrice(billable.getNetPrice());
					billed.setPoints(billable.getPrice().getCents());
				}
			};
		}
		return optifier;
	}
	
	@Override
	public synchronized IBillableVerifier getVerifier(){
		if (verifier == null) {
			verifier = new DefaultVerifier();
		}
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
