package ch.elexis.core.services;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public interface INativeQuery {
	/**
	 * Execute the query with the provided parameters and return a list with the resulting objects.
	 * 
	 * The parameters can not be resolved by name, but mus be indexed.
	 * <p>
	 * <i> Named parameters follow the rules for identifiers defined in Section 4.4.1. The use of
	 * named parameters applies to the Java Persistence query language, and is not defined for
	 * native queries. Only positional parameter binding may be portably used for native queries.
	 * </i>
	 * </p>
	 * 
	 * @param paramters
	 * @return
	 */
	public Stream<?> executeWithParameters(Map<Integer, Object> parameters);
	
	/**
	 * Helper Method for creating a indexed map to execute a {@link INativeQuery}.
	 * 
	 * @param parameters
	 * @return
	 */
	public default Map<Integer, Object> getIndexedParameterMap(Object... parameters){
		HashMap<Integer, Object> ret = new HashMap<>();
		for (int i = 0; i < parameters.length; i += 2) {
			ret.put((Integer) parameters[i], parameters[i + 1]);
		}
		return ret;
	}
}
