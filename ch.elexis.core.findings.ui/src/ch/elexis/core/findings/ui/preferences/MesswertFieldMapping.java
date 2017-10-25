package ch.elexis.core.findings.ui.preferences;

public class MesswertFieldMapping {
	
	private String localMesswert;
	private String localMesswertField;
	
	private String findingsCode;
	
	private static final String MAPPING_FIELD_SEPARATOR = "||";
	private static final String MAPPING_FIELD_SEPARATOR_ESCAPED = "\\|\\|";
	private static final String MAPPING_SEPARATOR = "<->";
	private static final String MAPPING_TYPE = "_MESSWERTFIELDMAPPING_";
	
	/**
	 * Create a BefundFieldMapping from a String representation (see exportToString).
	 * 
	 * @param string
	 * @return a valid BefundFieldMapping, or null
	 */
	public static MesswertFieldMapping createFromString(String string){
		MesswertFieldMapping mapping = new MesswertFieldMapping();
		if (string.startsWith(MAPPING_TYPE)) {
			string = string.substring(MAPPING_TYPE.length(), string.length());
			String[] mappings = string.split(MAPPING_SEPARATOR);
			if (mappings.length == 2) {
				String[] localMapping = mappings[0].split(MAPPING_FIELD_SEPARATOR_ESCAPED);
				if (localMapping.length == 2) {
					mapping.localMesswert = localMapping[0];
					mapping.localMesswertField = localMapping[1];
				}
				mapping.findingsCode = mappings[1];
			}
		}
		return mapping.isValidMapping() ? mapping : null;
	}
	
	private MesswertFieldMapping(){
		// is only for use with createFromString method
	}
	
	public MesswertFieldMapping(String localBefund, String localBefundField){
		this(localBefund, localBefundField, null);
	}
	
	public MesswertFieldMapping(String localBefund, String localBefundField, String findingsCode){
		this.localMesswert = localBefund;
		this.localMesswertField = localBefundField;
		this.findingsCode = findingsCode;
	}
	
	public boolean isLocalMatching(String befund, String field){
		if (localMesswert != null && localMesswertField != null) {
			return localMesswert.equals(befund) && localMesswertField.equals(field);
		}
		return false;
	}
	
	public boolean isFindingsCodeMatching(String findingsCode){
		if (this.findingsCode != null && findingsCode != null) {
			return this.findingsCode.equals(findingsCode);
		}
		return false;
	}
	
	/**
	 * Test if the mapping is valid. Valid means local and remote befund field is set, and the
	 * befund is in the local setup exists.
	 * 
	 * @return
	 */
	public boolean isValidMapping(){
		boolean fieldsOk =
			localMesswert != null && localMesswertField != null && findingsCode != null;
		if (fieldsOk) {
			// validate that this Befund is still locally configured
			return MesswertUtil.isExistingMesswert(localMesswert);
		}
		return false;
	}
	
	/**
	 * Get a String representing a valid mapping. Not valid mappings return an empty string.
	 * 
	 * @return
	 */
	public String exportToString(){
		StringBuilder sb = new StringBuilder();
		if (isValidMapping()) {
			sb.append(MAPPING_TYPE).append(localMesswert).append(MAPPING_FIELD_SEPARATOR)
				.append(localMesswertField);
			sb.append(MAPPING_SEPARATOR).append(findingsCode);
		}
		return sb.toString();
	}
	
	public String getLocalFieldLabel(){
		if(localMesswert != null && localMesswertField != null) {
			return localMesswert + "." + localMesswertField;
		}
		return "?";
	}
	
	public String getLocalBefund(){
		return localMesswert;
	}
	
	public String getLocalBefundField(){
		return localMesswertField;
	}
	
	public String getFindingsCodeLabel(){
		if (findingsCode != null) {
			return findingsCode;
		}
		return "";
	}
	
	public String getFindingsCode(){
		return findingsCode;
	}
	
	public void setFindigsCode(String findingsCode){
		this.findingsCode = findingsCode;
	}
}
