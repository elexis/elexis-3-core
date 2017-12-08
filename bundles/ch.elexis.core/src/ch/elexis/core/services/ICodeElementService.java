package ch.elexis.core.services;

import java.util.HashMap;
import java.util.Optional;

import ch.elexis.core.model.ICodeElement;

/**
 * Interface for a service that can be used to load and store {@link ICodeElement} instances.
 * 
 * @author thomas
 *
 */
public interface ICodeElementService {
	
	public enum ContextKeys {
			CONSULTATION, COVERAGE, MANDATOR, LAW, DATE
	}
	
	/**
	 * Create a String representation of the {@link ICodeElement}. A {@link ICodeElement} can the be
	 * loaded using the {@link ICodeElementService#createFromString(String, HashMap)} method.
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
	 * @param context
	 * @return
	 */
	public default Optional<ICodeElement> createFromString(String storeToString,
		HashMap<Object, Object> context){
		String[] parts = getStoreToStringParts(storeToString);
		// only system and code are relevant for loading 
		if (parts != null && parts.length > 1) {
			return createFromString(parts[0], parts[1], context);
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
	 * @param context
	 * @return
	 */
	public Optional<ICodeElement> createFromString(String system, String code,
		HashMap<Object, Object> context);
}
