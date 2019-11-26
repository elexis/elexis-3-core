package ch.elexis.core.ui.eigenartikel;

import org.eclipse.core.databinding.conversion.IConverter;

import ch.rgw.tools.Money;

public class Money2StringConverter implements IConverter<Money, String> {
	
	@Override
	public Object getFromType(){
		return Money.class;
	}
	
	@Override
	public Object getToType(){
		return String.class;
	}
	
	@Override
	public String convert(Money fromObject){
		if (fromObject != null) {
			return fromObject.getCentsAsString();
		}
		return null;
	}
}
