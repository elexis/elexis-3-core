package ch.elexis.core.model.ch;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;

public enum BillingLaw implements ILocalizedEnum {
		
				// Pour plus de détails (auf Deutsch, français ou italien voir ci dessous)
		KVG, 	// https://www.admin.ch/opc/de/classified-compilation/19940073/index.html
				// Krankenversicherung
		UVG,	// https://www.admin.ch/opc/de/classified-compilation/19810038/index.html
				// Unfallversicherung
		IV,		// https://www.admin.ch/opc/de/classified-compilation/19590131/index.html
				// Invalidenversicherung
		MV,		// https://www.admin.ch/opc/de/classified-compilation/19920155/index.html
				// Militärversicherung 
		VVG,	// https://www.admin.ch/opc/de/classified-compilation/19080008/index.html
				// Versicherungsvertrag
		privat,
		NONE,
		OTHER;
	
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
