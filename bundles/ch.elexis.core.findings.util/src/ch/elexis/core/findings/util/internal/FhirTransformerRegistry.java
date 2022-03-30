package ch.elexis.core.findings.util.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.findings.util.fhir.IFhirTransformerRegistry;

@Component
public class FhirTransformerRegistry implements IFhirTransformerRegistry {

	private List<IFhirTransformer<?, ?>> transformers;

	private HashMap<String, IFhirTransformer<?, ?>> cache = new HashMap<>();

	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void bindFhirTransformer(IFhirTransformer<?, ?> transformer) {
		if (transformers == null) {
			transformers = new ArrayList<IFhirTransformer<?, ?>>();
		}
		transformers.add(transformer);
	}

	public void unbindFhirTransformer(IFhirTransformer<?, ?> transformer) {
		if (transformers == null) {
			transformers = new ArrayList<IFhirTransformer<?, ?>>();
		}
		transformers.remove(transformer);
	}

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
}
