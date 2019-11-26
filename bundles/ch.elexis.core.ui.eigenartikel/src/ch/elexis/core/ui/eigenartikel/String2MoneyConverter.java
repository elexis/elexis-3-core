package ch.elexis.core.ui.eigenartikel;

import org.eclipse.core.databinding.conversion.IConverter;

import ch.rgw.tools.Money;

public class String2MoneyConverter implements IConverter<String, Money> {
	
	@Override
	public Object getFromType(){
		return String.class;
	}
	
	@Override
	public Object getToType(){
		return Money.class;
	}
	
	@Override
	public Money convert(String fromObject){
		if (fromObject != null) {
			try {
				return new Money(Integer.parseInt(fromObject));
			} catch (NumberFormatException e) {
				System.out.println(e.getMessage());
			}
		}
		return null;
	}
	
}
