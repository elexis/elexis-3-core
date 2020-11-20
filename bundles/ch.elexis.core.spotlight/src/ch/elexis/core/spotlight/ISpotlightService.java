package ch.elexis.core.spotlight;

import java.util.Map;
import java.util.function.Consumer;

public interface ISpotlightService {
	
	/**
	 * Compute the result according to the given parameters. The computation will call the consumer
	 * registered in {@link #setResultsChangedConsumer(Consumer)} multiple times.
	 * 
	 * @param searchInput
	 * @param contextParameters
	 */
	void computeResult(String searchInput, Map<String, String> contextParameters);
	
	void setResultsChangedConsumer(Consumer<ISpotlightResult> resultsChangedConsumer);
	
}
