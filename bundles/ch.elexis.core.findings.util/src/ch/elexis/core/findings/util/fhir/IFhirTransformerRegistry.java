package ch.elexis.core.findings.util.fhir;

import java.util.Optional;

import ch.elexis.core.model.Identifiable;

/**
 * Registry definition used to collect all {@link IFhirTransformer} services
 * available.
 *
 * @author thomas
 *
 */
public interface IFhirTransformerRegistry {
	/**
	 * Get an {@link IFhirTransformer} instance that matches the types of fhirClazz
	 * and localClazz.
	 *
	 * @param fhirClazz
	 * @param localClazz
	 * @return
	 */
	public <FHIR, LOCAL> IFhirTransformer<FHIR, LOCAL> getTransformerFor(Class<FHIR> fhirClazz,
			Class<LOCAL> localClazz);

	/**
	 * Use the {@link IFhirTransformer} implementations to load the local object for
	 * a FHIR reference String.
	 * 
	 * @param fhirReference e.g. "Patient/11233"
	 * @return
	 */
	public Optional<? extends Identifiable> getLocalObjectForReference(String fhirReference);

}
