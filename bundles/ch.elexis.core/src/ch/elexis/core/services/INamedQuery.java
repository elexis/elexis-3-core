package ch.elexis.core.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface INamedQuery<R> {
	/**
	 * Execute the query with the provided parameters and return a list with the
	 * resulting objects.
	 *
	 * @param parameters
	 * @return
	 */
	public List<R> executeWithParameters(Map<String, Object> parameters);

	/**
	 * Execute the query with the provided parameters and return a
	 * {@link IQueryCursor} with the resulting objects.
	 *
	 * @param parameters
	 * @return
	 */
	public IQueryCursor<R> executeAsCursorWithParameters(Map<String, Object> parameters);

	/**
	 * Execute the query and return a single result. If more than one result is
	 * available, a warning is logged, and the first result is returned.
	 *
	 * @param parameters
	 * @return
	 */
	public Optional<R> executeWithParametersSingleResult(Map<String, Object> parameters);

	/**
	 * Helper Method for creating a map to execute a {@link INamedQuery}.
	 *
	 * @param parameters
	 * @return
	 */
	public default Map<String, Object> getParameterMap(Object... parameters) {
		HashMap<String, Object> ret = new HashMap<>();
		for (int i = 0; i < parameters.length; i += 2) {
			ret.put((String) parameters[i], parameters[i + 1]);
		}
		return ret;
	}

	/**
	 * Limit the maximum number of results to retrieve.
	 *
	 * @param limit
	 */
	INamedQuery<R> limit(int limit);
}
