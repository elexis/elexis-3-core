package ch.elexis.core.findings.fhir.po.migrator.messwert;

import java.util.ArrayList;
import java.util.List;

import ch.elexis.befunde.Messwert;
import ch.elexis.core.data.activator.CoreHub;

public class MesswertFieldMapping {
	
	private static final String MAPPING_CONFIG = "ch.elexis.core.findins/messwert/mapping";
	
	private String localMesswert;
	private String localMesswertField;
	
	private String findingsCode;
	
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
		return
			localMesswert != null && localMesswertField != null && findingsCode != null;
	}
	
	public String getLocalBefund(){
		return localMesswert;
	}
	
	public String getLocalBefundField(){
		return localMesswertField;
	}
	
	public String getFindingsCode(){
		return findingsCode;
	}
	
	/**
	 * Load all mappings from the mandant configuration.
	 * 
	 * @return
	 */
	public static List<MesswertFieldMapping> getMappings(){
		List<MesswertFieldMapping> ret = new ArrayList<MesswertFieldMapping>();
		if (CoreHub.mandantCfg != null) {
			String mapping = CoreHub.mandantCfg.get(MAPPING_CONFIG, "");
			String[] mappings = mapping.split(Messwert.SETUP_SEPARATOR);
			for (String string : mappings) {
				MesswertFieldMapping createdMapping = MesswertFieldMapping.createFromString(string);
				if (createdMapping != null) {
					ret.add(createdMapping);
				}
			}
		} else {
			throw new IllegalStateException("No mandant config available");
		}
		return ret;
	}
}
