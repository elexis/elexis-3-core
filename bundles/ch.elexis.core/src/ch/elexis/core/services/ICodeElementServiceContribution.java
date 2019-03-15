package ch.elexis.core.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.services.ICodeElementService.CodeElementTyp;

/**
 * Interface that must be implemented, for each code element system, by bundles that provide a
 * {@link ICodeElement} implementation to the system.
 * 
 * @author thomas
 *
 */
public interface ICodeElementServiceContribution {
	
	public String getSystem();
	
	public CodeElementTyp getTyp();
	
	/**
	 * Load a {@link ICodeElement} instance using a code of the code system. Implementations should
	 * always be able to load using the code attribute of the {@link ICodeElement}, but also can
	 * extend to other attributes, for example pharma code for medicament. The context can be used
	 * to provide additional information like valid dates etc. can be null, and is implementation
	 * specific<br/>
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
	public Optional<ICodeElement> loadFromCode(String code, Map<Object, Object> context);
	
	/**
	 * Convenience method calling {@link ICodeElementService#loadFromString(String, String, Map)}
	 * with null context.
	 * 
	 * @param code
	 * @return
	 */
	public default Optional<ICodeElement> loadFromCode(String code){
		return loadFromCode(code, null);
	}
	
	/**
	 * Get all {@link ICodeElement} instances (for the given context) of the
	 * {@link ICodeElementServiceContribution}.
	 * 
	 * @param context
	 * @return
	 */
	public List<ICodeElement> getElements(Map<Object, Object> context);
}
