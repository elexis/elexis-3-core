package ch.elexis.core.spotlight;

import java.util.Map;
import java.util.function.Consumer;

import ch.elexis.core.jdt.Nullable;

public interface ISpotlightService {

	/**
	 * used in contextParameters: Only find results within the context of the given
	 * patient id
	 */
	public static final String CONTEXT_FILTER_PATIENT_ID = "patientId";

	/**
	 * Compute the result according to the given parameters. The computation will
	 * call the consumer registered in {@link #setResultsChangedConsumer(Consumer)}
	 * multiple times.
	 * 
	 * @param searchInput
	 * @param contextParameters
	 */
	void computeResult(String searchInput, @Nullable Map<String, String> contextParameters);

	void setResultsChangedConsumer(Consumer<ISpotlightResult> resultsChangedConsumer);

}
