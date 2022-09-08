package ch.elexis.core.findings.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Narrative;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.IEncounter;
import ch.elexis.core.findings.IFindingsService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ModelUtilTest {

	private IFindingsService findingsService;

	@Before
	public void before() {
		if (findingsService == null) {
			findingsService = OsgiServiceUtil.getService(IFindingsService.class).get();
		}
	}

	@After
	public void after() {
		if (findingsService != null) {
			OsgiServiceUtil.ungetService(findingsService);
		}
	}

	@Test
	public void setNarrativeText() {
		String value = "\n"
				+ "[ 13.07.2016 Rezept 13.07.2016 ](JW) neues Dauerrezept + Rezept für panotile habe chron Ohrenentz. braucht nur wenn akut";
		Narrative narrative = new Narrative();
		ModelUtil.setNarrativeFromString(narrative, value);
		Optional<String> string = ModelUtil.getNarrativeAsString(narrative);
		assertTrue(string.isPresent());
		assertEquals(value, string.get());
	}

	@Test
	public void fixFhirResource() {
		IEncounter encounter = findingsService.create(IEncounter.class);
		Optional<IBaseResource> fhirResource = ModelUtil.loadResource(encounter);
		assertTrue(fhirResource.isPresent());
		assertTrue(fhirResource.get() instanceof Encounter);
		Encounter fhirEncounter = (Encounter) fhirResource.get();
		fhirEncounter.setClass_(null);
		ModelUtil.saveResource(fhirEncounter, encounter);
		assertTrue(ModelUtil.fixFhirResource(encounter));
		// reload after update
		fhirResource = ModelUtil.loadResource(encounter);
		fhirEncounter = (Encounter) fhirResource.get();
		assertNotNull(fhirEncounter.getClass_());
		assertNotNull(fhirEncounter.getClass_().getSystem());
	}
}
