package ch.elexis.core.findings.util.fhir.accessor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Reference;
import org.junit.Before;
import org.junit.Test;

/**
 * Pure-POJO tests for the Coverage round-trip helpers added to
 * {@link EncounterAccessor}. The mapping is the source of truth for the
 * Encounter.account &lt;-&gt; IEncounter.coverage round-trip used by
 * {@link ch.elexis.core.findings.util.fhir.transformer.mapper.IEncounterEncounterAttributeMapper}.
 *
 * <p>The tests deliberately stay free of OSGi / DS so they can be executed
 * with plain {@code mvn -pl tests/ch.elexis.core.findings.util.test test}.
 */
public class EncounterAccessorTest {

	private EncounterAccessor accessor;
	private Encounter fhirEncounter;

	@Before
	public void setUp() {
		accessor = new EncounterAccessor();
		fhirEncounter = new Encounter();
	}

	// ------------------------------------------------------------------
	//  setCoverageId
	// ------------------------------------------------------------------

	@Test
	public void setCoverageId_addsAccountReference() {
		accessor.setCoverageId(fhirEncounter, "fall-42");

		assertEquals("exactly one account entry", 1, fhirEncounter.getAccount().size());
		assertEquals("Coverage/fall-42", fhirEncounter.getAccountFirstRep().getReference());
	}

	@Test
	public void setCoverageId_replacesExistingAccountReferences() {
		fhirEncounter.addAccount(new Reference("Coverage/old-1"));
		fhirEncounter.addAccount(new Reference("Coverage/old-2"));

		accessor.setCoverageId(fhirEncounter, "fall-new");

		assertEquals(1, fhirEncounter.getAccount().size());
		assertEquals("Coverage/fall-new", fhirEncounter.getAccountFirstRep().getReference());
	}

	@Test
	public void setCoverageId_nullOrEmpty_clearsAccount() {
		fhirEncounter.addAccount(new Reference("Coverage/old"));

		accessor.setCoverageId(fhirEncounter, null);
		assertTrue("null id must clear the account list", fhirEncounter.getAccount().isEmpty());

		fhirEncounter.addAccount(new Reference("Coverage/old"));
		accessor.setCoverageId(fhirEncounter, "");
		assertTrue("empty id must clear the account list", fhirEncounter.getAccount().isEmpty());
	}

	// ------------------------------------------------------------------
	//  getCoverageId
	// ------------------------------------------------------------------

	@Test
	public void getCoverageId_returnsEmptyForFreshEncounter() {
		Optional<String> id = accessor.getCoverageId(fhirEncounter);
		assertFalse(id.isPresent());
	}

	@Test
	public void getCoverageId_readsBackWhatWasSet() {
		accessor.setCoverageId(fhirEncounter, "fall-rt");

		Optional<String> id = accessor.getCoverageId(fhirEncounter);
		assertTrue(id.isPresent());
		assertEquals("fall-rt", id.get());
	}

	@Test
	public void getCoverageId_returnsFirstCoverageReferenceAndIgnoresOtherTypes() {
		fhirEncounter.addAccount(new Reference("Patient/some-patient"));
		fhirEncounter.addAccount(new Reference("Coverage/the-fall"));
		fhirEncounter.addAccount(new Reference("Coverage/another-fall"));

		Optional<String> id = accessor.getCoverageId(fhirEncounter);
		assertTrue(id.isPresent());
		assertEquals("must pick the first Coverage reference", "the-fall", id.get());
	}

	@Test
	public void getCoverageId_emptyWhenOnlyNonCoverageReferences() {
		fhirEncounter.addAccount(new Reference("Patient/abc"));
		fhirEncounter.addAccount(new Reference("Organization/xyz"));

		assertFalse(accessor.getCoverageId(fhirEncounter).isPresent());
	}
}
