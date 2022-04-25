package ch.elexis.core.ui.documents.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import ch.elexis.core.findings.util.fhir.IFhirTransformer;

@Component
public class FhirTransformersHolder {
	private static List<IFhirTransformer<?, ?>> transformers = new ArrayList<IFhirTransformer<?, ?>>();

	private static HashMap<String, IFhirTransformer<?, ?>> cache = new HashMap<>();

	@Reference(cardinality = ReferenceCardinality.AT_LEAST_ONE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	public synchronized void bindFhirTransformer(IFhirTransformer<?, ?> transformer) {
		transformers.add(transformer);
	}

	public void unbindFhirTransformer(IFhirTransformer<?, ?> transformer) {
		transformers.remove(transformer);
	}

	public static IFhirTransformer<?, ?> getTransformerFor(Class<?> fhirClazz, Class<?> localClazz) {
		String lookupString = fhirClazz.getName() + "-" + localClazz.getName();
		IFhirTransformer<?, ?> ret = cache.get(lookupString);
		if (ret == null) {
			for (IFhirTransformer<?, ?> iFhirTransformer : transformers) {
				if (iFhirTransformer.matchesTypes(fhirClazz, localClazz)) {
					ret = iFhirTransformer;
					cache.put(lookupString, iFhirTransformer);
					break;
				}
			}
		}
		return ret;
	}
}
