package ch.elexis.core.findings.ui.preferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import ch.elexis.befunde.Messwert;
import ch.elexis.core.data.activator.CoreHub;
import ch.rgw.tools.StringTool;

public class MesswertUtil {
	private static final String MAPPING_CONFIG = "ch.elexis.core.findins/messwert/mapping";
	
	/**
	 * Get the setup Messwert from the DB connection.
	 * 
	 * @param connection
	 * @return
	 */
	private static Messwert getSetup(){
		Messwert setup = Messwert.load("__SETUP__"); //$NON-NLS-1$
		
		if (setup.exists()) {
			return setup;
		}
		return null;
	}
	
	/**
	 * Load all avialable Befunde from the connection specified, or from the local connection if
	 * parameter is null.
	 * 
	 * @param connection
	 * @return
	 */
	public static List<String> getSetupBefunde(){
		Messwert setup = MesswertUtil.getSetup();
		List<String> ret = Collections.emptyList();
		
		@SuppressWarnings("rawtypes")
		Map fields = setup.getMap("Befunde"); //$NON-NLS-1$
		String names = (String) fields.get("names"); //$NON-NLS-1$
		if (!StringTool.isNothing(names)) {
			ret = Arrays.asList(names.split(Messwert.SETUP_SEPARATOR));
		}
		return ret;
	}
	
	/**
	 * Load the fields available for the Befund from the connection specified, or from the local
	 * connection if parameter is null.
	 * 
	 * @param connection
	 * @return
	 */
	public static List<String> getSetupBefundFields(String befund){
		Messwert setup = MesswertUtil.getSetup();
		List<String> ret = new ArrayList<String>();
		
		@SuppressWarnings("rawtypes")
		Map befunde = setup.getMap("Befunde"); //$NON-NLS-1$
		String befundFields = (String) befunde.get(befund + "_FIELDS");
		if (befundFields != null) {
			String[] fields = befundFields.split(Messwert.SETUP_SEPARATOR);
			for (String field : fields) {
				String[] fieldParts = field.split(Messwert.SETUP_CHECKSEPARATOR);
				if (fieldParts.length > 0) {
					ret.add(fieldParts[0]);
				}
			}
		}
		return ret;
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
	
	/**
	 * Load all mappings from the mandant configuration. And add mappings for local befunde fields
	 * which are not mapped yet.
	 * 
	 * @return
	 */
	public static List<MesswertFieldMapping> getLocalMappings(){
		List<MesswertFieldMapping> ret = new ArrayList<MesswertFieldMapping>();
		List<MesswertFieldMapping> existingMappings = getMappings();
		ret.addAll(existingMappings);
		List<MesswertFieldMapping> localMappings = new ArrayList<MesswertFieldMapping>();
		List<String> localBefunde = getSetupBefunde();
		for (String localBefund : localBefunde) {
			List<String> localBefundeFields = getSetupBefundFields(localBefund);
			for (String localBefundField : localBefundeFields) {
				boolean found = false;
				for (MesswertFieldMapping mapping : existingMappings) {
					if (mapping.isLocalMatching(localBefund, localBefundField)) {
						found = true;
						break;
					}
				}
				if (!found) {
					localMappings.add(new MesswertFieldMapping(localBefund, localBefundField));
				}
			}
		}
		ret.addAll(localMappings);
		return ret;
	}
	
	/**
	 * Save all mappings to the mandant configuration. Overwrites the existing mappings. Invalid
	 * mappings are skipped.
	 * 
	 * @param mappings
	 */
	public static void saveMappings(List<MesswertFieldMapping> mappings){
		if (CoreHub.mandantCfg != null) {
			StringBuilder sb = new StringBuilder();
			for (MesswertFieldMapping befundFieldMapping : mappings) {
				if (sb.length() > 0) {
					sb.append(Messwert.SETUP_SEPARATOR);
				}
				sb.append(befundFieldMapping.exportToString());
			}
			CoreHub.mandantCfg.set(MAPPING_CONFIG, sb.toString());
		} else {
			throw new IllegalStateException("No mandant config available");
		}
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean isExistingMesswert(String name){
		Messwert setup = getSetup();
		Map setupMap = setup.getMap("Befunde");
		String names = (String) setupMap.get("names");
		if (names != null) {
			String[] namesParts = names.split(Messwert.SETUP_SEPARATOR);
			for (String string : namesParts) {
				if (string.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}
}
