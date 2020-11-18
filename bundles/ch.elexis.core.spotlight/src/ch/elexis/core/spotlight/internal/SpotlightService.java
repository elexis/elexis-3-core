package ch.elexis.core.spotlight.internal;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.spotlight.ISpotlightResult;
import ch.elexis.core.spotlight.ISpotlightResultContributor;
import ch.elexis.core.spotlight.ISpotlightService;

@Component
public class SpotlightService implements ISpotlightService {
	
	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	private volatile List<ISpotlightResultContributor> resultContributors;
	
	Consumer<ISpotlightResult> consumer;
	
	private ISpotlightResult spotlightResult = new SpotlightResult();
	
	@Override
	public void clearResult(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void setResultsChangedConsumer(Consumer<ISpotlightResult> resultsChangedConsumer){
		this.consumer = resultsChangedConsumer;
	}
	
	@Override
	public void setSearchTerm(String searchTerm, Map<String, String> searchParams){
		spotlightResult.clear();
		
		// TODO calculations
		// TODO date normalization
		// TODO context setting?
		// TODO Command integration
		// TODO filter searchTerm - only alphanumeric chars, space and mathematical operations - SQL injection!!!
		
		if (searchTerm == null || searchTerm.length() < 2) {
			return;
		}
		
		// only parallel fetch the first 5 of every category? show total count?
		resultContributors.parallelStream().forEach(contributor -> {
			contributor.setSearchTerm(searchTerm, spotlightResult, searchParams);
			// after each contributor finished, notify the consumer
			consumer.accept(spotlightResult);
		});
		
	}
	
}
