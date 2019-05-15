package ch.elexis.core.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.Identifiable;

public interface IStoreToStringContribution {
	
	public final String DOUBLECOLON = "::";
	
	/**
	 * Split a storeToString into an array containing the type and the id. Expected separator string
	 * is {@link #DOUBLECOLON}.
	 * 
	 * @param storeToString
	 * @return a size 2 array with type [0] and id [1] or <code>null</code> in either [0] or [1]
	 */
	public default String[] splitIntoTypeAndId(String storeToString){
		String[] split = storeToString.split(DOUBLECOLON);
		return Arrays.copyOf(split, 2);
	}
	
	/**
	 * Get a reference string that can be used to load the {@link Identifiable} using
	 * {@link #loadFromString(String)}.
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
	 * Load a list of {@link Identifiable} matching the partial store to string. <b>Should not be
	 * used, except from barcode bundle</b>
	 * 
	 * @param partialStoreToString
	 * @return
	 */
	public default List<Identifiable> loadFromStringWithIdPart(String partialStoreToString){
		return Collections.emptyList();
	}
	
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
	 * Get the type name that is known for the provided interface class.
	 * 
	 * @param type
	 * @return
	 */
	public String getTypeForModel(Class<?> interfaze);
}
