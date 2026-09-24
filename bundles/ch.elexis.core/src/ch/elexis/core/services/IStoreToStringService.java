package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.Identifiable;

public interface IStoreToStringService {

	/**
	 * Convenience method that checks if the provided object is an instanceof
	 * {@link Identifiable}
	 * 
	 * @param object
	 * @return the store-to-string if of type {@link Identifiable}
	 * @throws IllegalArgumentException
	 * @since 3.13
	 */
	public default String getStoreToString(Object object) {
		if (object instanceof Identifiable identifiable) {
			return storeToString(identifiable)
					.orElseThrow(() -> new IllegalStateException("No storeToString for [" + object + "]"));
		}
		throw new IllegalArgumentException("No storeToString for [" + object + "]");
	}

	/**
	 * Get a reference string that can be used to load the {@link Identifiable}
	 * using {@link #loadFromString(String)}.
	 *
	 * @param identifiable
	 * @return
	 */
	public Optional<String> storeToString(Identifiable identifiable);

	/**
	 * Load an {@link Identifiable} using a string created using
	 * {@link #storeToString(Identifiable)}.
	 *
	 * @param string
	 * @return
	 */
	public Optional<Identifiable> loadFromString(String storeToString);

	/**
	 * Load a list of {@link Identifiable} matching the partial store to string.
	 * <b>Should not be used, except from barcode bundle</b>
	 *
	 * @param partialStoreToString
	 * @return
	 */
	public List<Identifiable> loadFromStringWithIdPart(String partialStoreToString);

	/**
	 * Get the entity class that is known under the type name.
	 *
	 * @param type
	 * @return
	 */
	public Class<?> getEntityForType(String type);

	/**
	 * Get the type name that is known for the entity instance.
	 *
	 * @param type
	 * @return
	 */
	public String getTypeForEntity(Object entityInstance);

	/**
	 * Get the type name that is known for the entity instance.
	 *
	 * @param type
	 * @return
	 */
	public String getTypeForModel(Class<?> interfaze);
}
