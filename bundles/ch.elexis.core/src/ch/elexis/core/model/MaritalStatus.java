package ch.elexis.core.model;

import java.util.ResourceBundle;

import ch.elexis.core.interfaces.ILocalizedEnum;
import ch.elexis.core.interfaces.INumericEnum;

public enum MaritalStatus implements INumericEnum, ILocalizedEnum {
		UNKNOWN(0),
		ANNULLED(1), 
		DIVORCED(2), 
		INTERLOCUTORY(3), 
		LEGALLY_SEPARATED(4), 
		MARRIED(5), 
		POLYGAMOUS(6),
		NEVER_MARRIED(7),
		DOMESTIC_PARTNER(8), 
		UNMARRIED(9), 
		WIDOWED(10);
	
	private final int numeric;
	
	private MaritalStatus(int numeric){
		this.numeric = numeric;
	}
	
	public int numericValue(){
		return numeric;
	}
	
	@Override
	public String getLocaleText(){
		try {
			return ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
					.getString(MaritalStatus.class.getSimpleName() + "_" + this.name());
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return this.name();
		}
	}
	
	public static MaritalStatus byNumericSafe(String priority){
		for (MaritalStatus prio : MaritalStatus.values()) {
			if (Integer.toString(prio.numericValue()).equalsIgnoreCase(priority)) {
				return prio;
			}
		}
		return MaritalStatus.UNKNOWN;
	}
	
}
