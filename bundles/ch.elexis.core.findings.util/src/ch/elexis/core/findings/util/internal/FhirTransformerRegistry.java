package ch.elexis.core.findings.util.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;
import ch.elexis.core.model.Identifiable;
import io.quarkus.arc.All;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@Component
public class FhirTransformerRegistry implements IFhirTransformerRegistry {

	@Inject
	@All
	@Reference(cardinality = ReferenceCardinality.MULTIPLE, policyOption = ReferencePolicyOption.GREEDY)
	volatile List<IFhirTransformer<?, ?>> transformers;

	private HashMap<String, IFhirTransformer<?, ?>> cache = new HashMap<>();

	private HashMap<String, IFhirTransformer<?, ?>> fhirClassCache = new HashMap<>();

	@SuppressWarnings("unchecked")
	@Override
	public <FHIR, LOCAL> IFhirTransformer<FHIR, LOCAL> getTransformerFor(Class<FHIR> fhirClazz,
			Class<LOCAL> localClazz) {
		String lookupString = fhirClazz.getName() + "-" + localClazz.getName();
		IFhirTransformer<FHIR, LOCAL> ret = (IFhirTransformer<FHIR, LOCAL>) cache.get(lookupString);
		if (ret == null) {
			for (IFhirTransformer<?, ?> iFhirTransformer : transformers) {
				if (iFhirTransformer.matchesTypes(fhirClazz, localClazz)) {
					ret = (IFhirTransformer<FHIR, LOCAL>) iFhirTransformer;
					cache.put(lookupString, iFhirTransformer);
					break;
				}
			}
		}
		return ret;
	}

	@Override
	public Optional<? extends Identifiable> getLocalObjectForReference(String fhirReference) {
		if (StringUtils.isNotBlank(fhirReference)) {
			if (fhirReference.indexOf("/") > -1) {
				String[] parts = fhirReference.split("/");
				IFhirTransformer<?, ?> transformer = fhirClassCache.get(parts[0]);
				if (transformer != null) {
					return transformer.getLocalObjectForReference(fhirReference);
				}
				for (IFhirTransformer<?, ?> iFhirTransformer : transformers) {
					Optional<? extends Identifiable> localObject = iFhirTransformer
							.getLocalObjectForReference(fhirReference);
					if (localObject.isPresent()) {
						fhirClassCache.put(parts[0], iFhirTransformer);
						return localObject;
					}
				}
			}
		}
		return Optional.empty();
	}
}
