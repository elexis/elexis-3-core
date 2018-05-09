package ch.elexis.core.model.ch;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;

public enum BillingLaw implements ILocalizedEnum {
		
		KVG, UVG, IVG, MVG, VVG, NONE, OTHER;
	
	@Override
	public String getLocaleText(){
		try {
			return ResourceBundle.getBundle("ch.elexis.core.model.ch.messages")
				.getString(BillingLaw.class.getSimpleName() + "." + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}
	
}
