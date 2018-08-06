package ch.elexis.core.services;

import java.util.Arrays;
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
}
