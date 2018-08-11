package ch.elexis.core.services;

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
}
