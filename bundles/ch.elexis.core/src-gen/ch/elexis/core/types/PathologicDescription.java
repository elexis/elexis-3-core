package ch.elexis.core.types;

/**
 * Describes how the pathologic flag was determined. Description is how and reference can hold a
 * reference value that was used.
 * 
 * @author thomas
 *
 */
public class PathologicDescription {
	
	public enum Description {
			/** pathologic value from import */
			PATHO_IMPORT("Aus Import"),
			/** no pathologic value provided during import */
			PATHO_IMPORT_NO_INFO("Aus Import (Keine Information vorhanden)"),
			/** pathologic value determined by checking against absolute reference */
			PATHO_ABSOLUT("Absoluter Wert"),
			/** pathologic value determined by checking against reference from lab result */
			PATHO_REF("Resultat Referenzwert"),
			/** pathologic value determined by checking against reference from lab item */
			PATHO_REF_ITEM("Parameter Referenzwert"),
			/** pathologic value because no, or non parseable reference value */
			PATHO_NOREF("Kein Referenzwert"),
			/** pathologic value manually set */
			PATHO_MANUAL("Manuell"),
			/** default not known why the pathologic has its value */
			UNKNOWN("?");
			
		private String label;
		
		Description(String label){
			this.label = label;
		}
		
		public Object getLabel(){
			return label;
		}
		
	}
	
	private Description description;
	private String reference;
	
	public static PathologicDescription of(String string){
		PathologicDescription ret = new PathologicDescription();
		String[] parts = string.split("\\|\\|");
		if (parts.length == 2 && !parts[0].isEmpty()) {
			ret.description = Description.valueOf(parts[0]);
			ret.reference = parts[1];
		} else if (parts.length == 1 && !parts[0].isEmpty()) {
			ret.description = Description.valueOf(parts[0]);
			ret.reference = "";
		}
		return ret;
	}
	
	public PathologicDescription(Description description){
		this(description, "");
	}
	
	public PathologicDescription(Description description, String reference){
		this.description = description;
		this.reference = (reference != null) ? reference : "";
	}
	
	private PathologicDescription(){
		description = Description.UNKNOWN;
		reference = "";
	}
	
	@Override
	public String toString(){
		return description.name() + "||" + reference;
	}
	
	public Description getDescription(){
		return description;
	}
	
	public String getReference(){
		return reference;
	}
	
	public Object getLabel(){
		if (reference != null && !reference.isEmpty()) {
			return getDescription().getLabel() + " (" + reference + ")";
		} else {
			return getDescription().getLabel();
		}
	}
}
