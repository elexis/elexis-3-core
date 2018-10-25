package ch.elexis.core.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface INamedQuery<R> {
	/**
	 * Execute the query with the provided parameters and return a list with the resulting objects.
	 * 
	 * @param paramters
	 * @return
	 */
	public List<R> executeWithParameters(Map<String, Object> parameters);
	
	/**
	 * Helper Method for creating a map to execute a {@link INamedQuery}.
	 * 
	 * @param parameters
	 * @return
	 */
	public default Map<String, Object> getParameterMap(Object... parameters){
		HashMap<String, Object> ret = new HashMap<>();
		for (int i = 0; i < parameters.length; i += 2) {
			ret.put((String) parameters[i], parameters[i + 1]);
		}
		return ret;
	}
}
