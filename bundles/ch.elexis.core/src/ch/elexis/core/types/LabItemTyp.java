package ch.elexis.core.types;

import ch.elexis.core.jdt.Nullable;

public enum LabItemTyp {
		NUMERIC(0), TEXT(1), ABSOLUTE(2), FORMULA(3), DOCUMENT(4);
	
	private int type;
	
	private LabItemTyp(int type){
		this.type = type;
	}
	
	public int getType(){
		return type;
	}
	
	public static @Nullable LabItemTyp fromType(int type){
		for (LabItemTyp lit : LabItemTyp.values()) {
			if (type == lit.getType()) {
				return lit;
			}
		}
		return null;
	}
	
	public static @Nullable LabItemTyp fromType(String type){
		try {
			int parseInt = Integer.parseInt(type);
			return LabItemTyp.fromType(parseInt);
		} catch (NumberFormatException nfe) {
			return null;
		}
	}
}
