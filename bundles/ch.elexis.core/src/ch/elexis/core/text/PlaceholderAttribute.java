package ch.elexis.core.text;

/**
 * A typed-attribute that may be resolved to a string
 */
public class PlaceholderAttribute {
	
	private final String typeName;
	private final String attributeName;
	private final String description;
	
	public PlaceholderAttribute(String typeName, String attributeName, String description){
		this.typeName = typeName;
		this.attributeName = attributeName;
		this.description = description;
	}
	
	public String getTypeName(){
		return typeName;
	}
	
	public String getAttributeName(){
		return attributeName;
	}
	
	public String getDescription(){
		return description;
	}
	
}
