package ch.elexis.core.spotlight;

import java.util.Map;
import java.util.function.Consumer;

public interface ISpotlightService {
	
	void clearResult();
	
	void setSearchTerm(String searchTermin, Map<String, String> searchParams);
	
	void setResultsChangedConsumer(Consumer<ISpotlightResult> resultsChangedConsumer);
	
}
