package ch.elexis.core.model.agenda;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;

public enum AreaType implements ILocalizedEnum {
		
		/**
		 * Association to a single user or contact
		 */
		CONTACT,
		/**
		 * Generic area not associated to any resource; the default value if none existing
		 */
		GENERIC;
		
	@Override
	public String getLocaleText(){
		try {
			return ResourceBundle.getBundle("ch.elexis.core.model.agenda.messages")
				.getString(AreaType.class.getSimpleName() + "." + this.name());
		} catch (Exception e) {
			return this.name();
		}
	}
	
}
