package ch.elexis.core.services;

import java.util.List;
import java.util.Map;

public interface INamedQuery<T> {
	/**
	 * Execute the query with the provided parameters and return a list with the resulting objects.
	 * 
	 * @param paramters
	 * @return
	 */
	public List<T> executeWithParameters(Map<String, Object> parameters);
}
