package ch.elexis.core.services;

import java.util.Map;
import java.util.stream.Stream;

public interface INativeQuery {
	/**
	 * Execute the query with the provided parameters and return a list with the resulting objects.
	 * 
	 * @param paramters
	 * @return
	 */
	public Stream<?> executeWithParameters(Map<String, Object> parameters);
	
}
