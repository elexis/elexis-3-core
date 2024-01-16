package ch.elexis.core.text;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.Identifiable;
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
	 * Get the {@link Identifiable} matching the type of this
	 * {@link ITextPlaceholderResolver} from the provided {@link IContext}.
	 * 
	 * @param context
	 * @return
	 */
	default Optional<? extends Identifiable> getIdentifiable(IContext context) {
		return Optional.empty();
	}

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
	 * Case-Insensitive load of an enumeration value. Characters not allowed for
	 * {@link Enum} names are replaced with '_'.
	 *
	 * @param <T>
	 * @param enumeration
	 * @param search
	 * @return
	 */
	default <T extends Enum<?>> T searchEnum(Class<T> enumeration, String search) {
		String searchEnumName = search.replace('-', '_');
		for (T each : enumeration.getEnumConstants()) {
			if (each.name().compareToIgnoreCase(searchEnumName) == 0) {
				return each;
			}
			if (each instanceof IPlaceholderAttributeEnum) {
				Optional<String> found = ((IPlaceholderAttributeEnum) each).getAlternativeNames().stream()
						.filter(an -> an.compareToIgnoreCase(searchEnumName) == 0).findFirst();
				if (found.isPresent()) {
					return each;
				}
			}
		}
		return null;
	}

	/**
	 * Extend the std. Enum class used for placeholder attributes with additional
	 * functionality.
	 */
	public interface IPlaceholderAttributeEnum {

		/**
		 * Get alternative names for the attribute.
		 * 
		 * @return
		 */
		public List<String> getAlternativeNames();
	}
}
