package ch.elexis.core.findings.util.fhir.transformer;

import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.model.Identifiable;

public class FhirTransformerCache<T> {

	private Cache<String, T> fhirObjectCache;

	public FhirTransformerCache() {
		fhirObjectCache = CacheBuilder.newBuilder().maximumSize(1000).build();
	}

	public Optional<T> get(Identifiable localObject, SummaryEnum summaryEnum, Set<Include> includes,
			Callable<T> callable) {
		try {
			T ret = fhirObjectCache.get(getCacheKey(localObject, summaryEnum, includes), callable);
			if (ret != null) {
				return Optional.of(ret);
			}
		} catch (ExecutionException e) {
			LoggerFactory.getLogger(getClass()).error("Error transform to FHIR [" + localObject + "]", e);
		}
		return Optional.empty();
	}

	private String getCacheKey(Identifiable localObject, SummaryEnum summaryEnum, Set<Include> includes) {
		StringJoiner sj = new StringJoiner("|");
		sj.add(localObject.getId());
		if (localObject.getLastupdate() != null) {
			sj.add(Long.toString(localObject.getLastupdate()));
		}
		if (summaryEnum != null) {
			sj.add(summaryEnum.name());
		}
		if (includes != null) {
			sj.add(includes.stream().map(i -> i.getValue()).collect(Collectors.joining(",")));
		}
		return sj.toString();
	}

	public void invalidate(Identifiable localObject) {
		fhirObjectCache.asMap().keySet().forEach(k -> {
			if (k.startsWith(localObject.getId() + "|")) {
				fhirObjectCache.invalidate(k);
			}
		});
	}
}
