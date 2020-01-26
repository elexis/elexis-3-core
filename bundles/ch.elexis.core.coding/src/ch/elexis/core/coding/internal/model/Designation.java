package ch.elexis.core.coding.internal.model;

public class Designation {
	public String language;
	public String type;
	public String displayName;
	
	@Override
	public String toString(){
		return "Designation [language=" + language + ", type=" + type + ", displayName="
			+ displayName + "]";
	}
	
}
