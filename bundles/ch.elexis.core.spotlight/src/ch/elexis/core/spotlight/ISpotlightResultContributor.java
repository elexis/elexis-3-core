package ch.elexis.core.spotlight;

import java.util.Map;

public interface ISpotlightResultContributor {
	
	/**
	 * 
	 * @param searchTerm
	 * @param spotlightResult
	 *            add results here - be fast and do not add more 5, if we have more, user has to
	 *            specify better
	 * @param searchParams
	 *            search parameters to consider
	 */
	// TODO multiple space separated search tokens
	// TODO formal date
	void setSearchTerm(String searchTerm, ISpotlightResult spotlightResult,
		Map<String, String> searchParams);
	
}
