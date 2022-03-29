package ch.elexis.core.findings.util.fhir;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.hl7.fhir.r4.model.Identifier;

import ca.uhn.fhir.model.api.Include;
import ca.uhn.fhir.rest.api.SummaryEnum;
import ch.elexis.core.findings.IdentifierSystem;
import ch.elexis.core.model.Identifiable;

/**
 * Service definition for transforming FHIR objects to local Objects. Provides
 * REST CRUD (create, read, update, delete) operations on the local objects
 * using the FHIR objects.
 * 
 * @author thomas
 *
 * @param <F> FHIR class
 * @param <L> Local class, extends AbstractDBObjectIdDeleted
 */
public interface IFhirTransformer<F, L> {

	/**
	 * ID to directly reference a specific transformer
	 */
	public static final String TRANSFORMERID = "transformer.id";

	/**
	 * Create a new FHIR object representing the localObject, optionally including
	 * referenced resources or considering a {@link SummaryEnum}
	 * 
	 * @param localObject
	 * @param summaryEnum the <a href=
	 *                    "https://www.hl7.org/fhir/search.html#summary">summary</a>
	 *                    mode; {@link SummaryEnum#TEXT} and include do not match
	 * @param includes    the resources to <a href=
	 *                    "http://hapifhir.io/doc_rest_operations.html#Resource_Includes__include">include</a>
	 * 
	 * @return
	 */
	public Optional<F> getFhirObject(L localObject, SummaryEnum summaryEnum, Set<Include> includes);

	public default Optional<F> getFhirObject(L localObject) {
		return getFhirObject(localObject, SummaryEnum.FALSE, Collections.emptySet());
	}

	/**
	 * Search for the local Object matching the FHIR object.
	 * 
	 * @param fhirObject
	 * @return
	 */
	public Optional<L> getLocalObject(F fhirObject);

	/**
	 * Update the local Object with the content of the FHIR object.
	 * 
	 * @param fhirObject
	 * @param localObject
	 * @return the updated local Object, possibly not same as localObject parameter,
	 *         empty if nothing changed
	 */
	public Optional<L> updateLocalObject(F fhirObject, L localObject);

	/**
	 * Create a new local Object matching the FHIR object.
	 * 
	 * @param fhirObject
	 * @return
	 */
	public Optional<L> createLocalObject(F fhirObject);

	/**
	 * Test if the implementation has matching FHIR and local class types.
	 * 
	 * @param fhirClazz
	 * @param localClazz
	 * @return
	 */
	public boolean matchesTypes(Class<?> fhirClazz, Class<?> localClazz);

	default Identifier getElexisObjectIdentifier(Identifiable dbObject) {
		Identifier identifier = new Identifier();
		identifier.setSystem(IdentifierSystem.ELEXIS_OBJID.getSystem());
		identifier.setValue(dbObject.getId());
		return identifier;
	}
}
