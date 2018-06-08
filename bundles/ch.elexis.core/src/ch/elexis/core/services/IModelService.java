package ch.elexis.core.services;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.Identifiable;

/**
 * Service interface for accessing the data model. Implementations should provide
 * {@link IModelService#SERVICEMODELNAME} as service property. Using the property clients can get
 * service for a specific model.
 * 
 * @author thomas
 *
 */
public interface IModelService {
	
	public final String SERVICEMODELNAME = "service.model.name";
	
	public final String DOUBLECOLON = "::";
	
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
	public Optional<Identifiable> loadFromString(String string);
	
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
	 * Create a new transient model instance of type clazz.
	 * 
	 * @param clazz
	 * @return
	 */
	public <T> T create(Class<T> clazz);
	
	/**
	 * Load a model object of type clazz by the id. Ignores deleted.
	 * 
	 * @param id
	 * @param clazz
	 * @return
	 */
	public <T> Optional<T> load(String id, Class<T> clazz);
	
	/**
	 * Save the model object.
	 * 
	 * @param object
	 * @return
	 */
	public boolean save(Identifiable identifiable);
	
	/**
	 * Save the model objects.
	 * 
	 * @param objects
	 * @return
	 */
	public boolean save(List<Identifiable> identifiables);
}
