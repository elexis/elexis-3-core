package ch.elexis.core.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.model.ICodeElement;

/**
 * Interface for a service that can be used to load and store {@link ICodeElement} instances.
 * 
 * @author thomas
 *
 */
public interface ICodeElementService {
	
	public enum CodeElementTyp {
			SERVICE, DIAGNOSE, ARTICLE
	}
	
	public enum ContextKeys {
			CONSULTATION, COVERAGE, MANDATOR, LAW, DATE, TREE_ROOTS
	}
	
	/**
	 * Create a String representation of the {@link ICodeElement}. A {@link ICodeElement} can the be
	 * loaded using the {@link ICodeElementService#loadFromString(String, HashMap)} method.
	 * 
	 * @param element
	 * @return
	 */
	public default String storeToString(ICodeElement element){
		return element.getCodeSystemName() + "|" + element.getCode() + "|" + element.getText();
	}
	
	/**
	 * Load a {@link ICodeElement} instance using its String representation, generated using the
	 * {@link ICodeElementService#storeToString(ICodeElement)} method.<br/>
	 * <br/>
	 * The returned {@link ICodeElement} will have the same SystemName and Code property, the rest
	 * can differ from the {@link ICodeElement} used to generate the String. New imports of the
	 * {@link ICodeElement} dataset, or some value in the context can also change the returned
	 * object.
	 * 
	 * @param storeToString
	 * @param context may be <code>null</code>
	 * @return
	 */
	public default Optional<ICodeElement> loadFromString(String storeToString,
		Map<Object, Object> context){
		String[] parts = getStoreToStringParts(storeToString);
		// only system and code are relevant for loading 
		if (parts != null && parts.length > 1) {
			return loadFromString(parts[0], parts[1], context);
		}
		return Optional.empty();
	}
	
	/**
	 * Get the parts from the storeToString.<br/>
	 * <br/>
	 * <li>index 0, system</li>
	 * <li>index 1, code</li>
	 * <li>index 2, text</li>
	 * 
	 * @param storeToString
	 * @return
	 */
	public default String[] getStoreToStringParts(String storeToString){
		return storeToString.split("\\|");
	}
	
	/**
	 * Load a {@link ICodeElement} instance using its String representation, generated using the
	 * {@link ICodeElementService#storeToString(ICodeElement)} method.<br/>
	 * <br/>
	 * The returned {@link ICodeElement} will have the same SystemName and Code property, the rest
	 * can differ from the {@link ICodeElement} used to generate the String. New imports of the
	 * {@link ICodeElement} dataset, or some value in the context can also change the returned
	 * object.
	 * 
	 * @param storeToString
	 * @param context may be <code>null</code>
	 * @return
	 */
	public Optional<ICodeElement> loadFromString(String system, String code,
		Map<Object, Object> context);
	
	/**
	 * Get all available {@link ICodeElementServiceContribution}s available.
	 * 
	 * @param typ
	 * @return
	 */
	public List<ICodeElementServiceContribution> getContributionsByTyp(CodeElementTyp typ);
	
	/**
	 * Get the {@link ICodeElementServiceContribution} with matching {@link CodeElementTyp} and code
	 * system name.
	 * 
	 * @param typ
	 * @param codeSystemName
	 * @return
	 */
	public Optional<ICodeElementServiceContribution> getContribution(CodeElementTyp typ,
		String codeSystemName);
}
