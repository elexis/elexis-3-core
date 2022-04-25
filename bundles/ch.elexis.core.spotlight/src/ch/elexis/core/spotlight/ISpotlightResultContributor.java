package ch.elexis.core.spotlight;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ISpotlightResultContributor {

	/**
	 *
	 * @param stringTerms       one or more alphanumeric values to match (if numeric
	 *                          only, goes to numericTerms)
	 * @param dateTerms         one or more dates to match (user input is parsed to
	 *                          date values beforehand)
	 * @param numericTerms      one or more numeric only values to match
	 * @param spotlightResult   add results here - be fast and do not add more 5, if
	 *                          we have more, user has to specify better
	 * @param contextParameters context parameters to consider
	 */
	void computeResult(List<String> stringTerms, List<LocalDate> dateTerms, List<Number> numericTerms,
			ISpotlightResult spotlightResult, Map<String, String> contextParameters);
	// TODO intTerms ?

}
