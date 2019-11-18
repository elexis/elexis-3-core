package ch.elexis.core.text;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.services.IContext;

/**
 * Resolve an attribute on a given supported type.
 */
public interface ITextPlaceholderResolver {
	
	/**
	 * @return the type this resolver acts upon. E.g. Patient
	 */
	String getSupportedType();
	
	/**
	 * 
	 * @return the attributes resolvable
	 */
	List<PlaceholderAttribute> getSupportedAttributes();
	
	/**
	 * replace the given attribute considering the provided context
	 * 
	 * @param context
	 * @param attribute
	 * @return
	 */
	Optional<String> replaceByTypeAndAttribute(IContext context, String attribute);
	
	/**
	 * Case-Insensitive load of an enumeration value
	 * 
	 * @param <T>
	 * @param enumeration
	 * @param search
	 * @return
	 */
	default <T extends Enum<?>> T searchEnum(Class<T> enumeration, String search){
		for (T each : enumeration.getEnumConstants()) {
			if (each.name().compareToIgnoreCase(search) == 0) {
				return each;
			}
		}
		return null;
	}
}
