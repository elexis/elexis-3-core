package ch.elexis.core.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ch.elexis.core.model.Identifiable;

@Component
public class StoreToStringService implements IStoreToStringService {
	
	private List<IStoreToStringContribution> contributions = new ArrayList<>();
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void setCodeElementServiceContribution(
		IStoreToStringContribution contribution){
		contributions.add(contribution);
	}
	
	public synchronized void unsetCodeElementServiceContribution(
		IStoreToStringContribution contribution){
		contributions.remove(contribution);
	}
	
	private Map<Class<?>, IStoreToStringContribution> classToContributionMap = new HashMap<>();
	
	private Cache<String, Identifiable> loadFromStringCache =
		CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.SECONDS).maximumSize(100).build();
	
	@Override
	public Optional<String> storeToString(Identifiable identifiable){
		IStoreToStringContribution contribution =
			classToContributionMap.get(identifiable.getClass());
		if (contribution != null) {
			return contribution.storeToString(identifiable);
		}
		for (IStoreToStringContribution iStoreToStringContribution : contributions) {
			Optional<String> string = iStoreToStringContribution.storeToString(identifiable);
			if (string.isPresent()) {
				classToContributionMap.put(identifiable.getClass(), iStoreToStringContribution);
				return string;
			}
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<Identifiable> loadFromString(String storeToString){
		Identifiable cached = loadFromStringCache.getIfPresent(storeToString);
		if (cached != null) {
			return Optional.of(cached);
		}
		for (IStoreToStringContribution iStoreToStringContribution : contributions) {
			Optional<Identifiable> identifiable =
				iStoreToStringContribution.loadFromString(storeToString);
			if (identifiable.isPresent()) {
				loadFromStringCache.put(storeToString, identifiable.get());
				return identifiable;
			}
		}
		return Optional.empty();
	}
}
