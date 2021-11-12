package ch.elexis.core.findings.util.fhir;

/**
 * Registry definition used to collect all {@link IFhirTransformer} services available.
 * 
 * @author thomas
 *
 */
public interface IFhirTransformerRegistry {
	/**
	 * Get an {@link IFhirTransformer} instance that matches the types of fhirClazz and localClazz.
	 * 
	 * @param fhirClazz
	 * @param localClazz
	 * @return
	 */
	public IFhirTransformer<?, ?> getTransformerFor(Class<?> fhirClazz, Class<?> localClazz);
}
