package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ICoverageServiceTest extends AbstractServiceTest {

	private ICoverageService coverageService = OsgiServiceUtil.getService(ICoverageService.class).get();

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();
	}

	@After
	public void after() {
		cleanup();
	}

	@Test
	public void getLatestEncounter() throws InterruptedException {

		IEncounter encounter = new IEncounterBuilder(coreModelService, testCoverages.get(0), testMandators.get(0))
				.buildAndSave();
		encounter.setTimeStamp(LocalDateTime.now().plusDays(1));
		coreModelService.save(encounter);

		Optional<IEncounter> latestEncounter = coverageService.getLatestEncounter(testCoverages.get(0));
		assertTrue(latestEncounter.isPresent());
		assertEquals(encounter, latestEncounter.get());

		coreModelService.remove(encounter);
	}
}
