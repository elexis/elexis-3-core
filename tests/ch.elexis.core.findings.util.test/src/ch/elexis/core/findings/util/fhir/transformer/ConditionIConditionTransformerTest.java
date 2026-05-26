package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the static
 * {@link ConditionIConditionTransformer#mapCodingSystemToReferredClass(String)}
 * helper that converts FHIR Coding.system URIs to the
 * {@code diagnosen.KLASSE} string used by the Elexis client.
 *
 * <p>
 * This is a pure-POJO test that does not require an OSGi runtime or
 * database, so it runs cleanly from {@code mvn test} or the Eclipse JUnit
 * runner without further setup. The mapping has to stay in lock-step with
 * the one in {@link ClaimVerrechnetTransformer}; if either drifts, billing
 * (Claim) and diagnosis (Condition) round-trips will use different
 * KLASSE values for the same code system, which would silently break
 * client-side filtering.
 * </p>
 */
public class ConditionIConditionTransformerTest {

	@Test
	public void icdDeMapsToICD10() {
		// CodingSystem.ICD_DE_CODESYSTEM is the German ICD-10 URI used by
		// the Elexis-DE FHIR profile.
		assertEquals("ch.elexis.data.ICD10", ConditionIConditionTransformer
				.mapCodingSystemToReferredClass("http://hl7.org/fhir/sid/icd-10-de"));
	}

	@Test
	public void icdGenericMapsToICD10() {
		// HAPI-default ICD-10 URI without the German "-de" suffix - used
		// by most off-the-shelf FHIR clients. Without explicit handling
		// this would silently fall through to FreeTextDiagnose.
		assertEquals("ch.elexis.data.ICD10", ConditionIConditionTransformer
				.mapCodingSystemToReferredClass("http://hl7.org/fhir/sid/icd-10"));
	}

	@Test
	public void tessinerCodeMapsToTICode() {
		// CodingSystem.ELEXIS_DIAGNOSE_TESSINERCODE - swiss "Tessiner Code"
		// for TARDOC-internal diagnosis groupings.
		assertEquals("ch.elexis.data.TICode", ConditionIConditionTransformer
				.mapCodingSystemToReferredClass("www.elexis.info/diagnose/tessinercode"));
	}

	@Test
	public void unknownSystemFallsBackToFreeText() {
		// Any other / unknown URI is treated as a free-text diagnosis so
		// no data is lost; matches the ClaimVerrechnetTransformer
		// behaviour.
		assertEquals("ch.elexis.data.FreeTextDiagnose", ConditionIConditionTransformer
				.mapCodingSystemToReferredClass("http://snomed.info/sct"));
	}

	@Test
	public void nullSystemFallsBackToFreeText() {
		// A Coding without an explicit system must not throw a
		// NullPointerException - defensive callers may strip the system
		// before invoking the transformer.
		assertEquals("ch.elexis.data.FreeTextDiagnose",
				ConditionIConditionTransformer.mapCodingSystemToReferredClass(null));
	}

	@Test
	public void emptySystemFallsBackToFreeText() {
		assertEquals("ch.elexis.data.FreeTextDiagnose",
				ConditionIConditionTransformer.mapCodingSystemToReferredClass(""));
	}
}
