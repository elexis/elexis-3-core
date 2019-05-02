package ch.elexis.core.services;

import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.Identifiable;

public interface IStoreToStringService {
	
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
	public List<Identifiable> loadFromStringWithIdPart(String partialStoreToString);
}
