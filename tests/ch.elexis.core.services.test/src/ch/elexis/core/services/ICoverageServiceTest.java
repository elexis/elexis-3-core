package ch.elexis.core.services;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.model.builder.IEncounterBuilder;
import ch.elexis.core.model.ch.BillingLaw;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ICoverageServiceTest extends AbstractServiceTest {

	private ICoverageService coverageService = OsgiServiceUtil.getService(ICoverageService.class).get();

	ICoverage coverage;
	IPatient patient;

	@Before
	public void before() {
		createTestMandantPatientFallBehandlung();
		patient = testPatients.get(0);

		for (ICoverage existingCoverages : patient.getCoverages()) {
			coreModelService.remove(existingCoverages);
		}
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

	@Test
	public void testGetCoverageWithLawNoMatch() {
		coverage = new ICoverageBuilder(coreModelService, patient, coverageService.getDefaultCoverageLabel(),
				coverageService.getDefaultCoverageReason(), "KGV").buildAndSave();

		Optional<ICoverage> result = coverageService.getCoverageWithLaw(patient,
				BillingLaw.privat);
		assertTrue(result.isEmpty());
		coreModelService.remove(coverage);
	}

	@Test
	public void testGetCoverageWithLawNoOpenCoverage() {
		coverage = new ICoverageBuilder(coreModelService, patient, coverageService.getDefaultCoverageLabel(),
				coverageService.getDefaultCoverageReason(), "KGV").buildAndSave();

		coverage.setDateTo(LocalDate.now());
		coreModelService.save(coverage);
		Optional<ICoverage> result = coverageService.getCoverageWithLaw(patient, BillingLaw.KVG);
		assertTrue(result.isEmpty());

		coreModelService.remove(coverage);
	}

	@Test
	public void testGetCoverageWithLawHasCoverage() {
		coverage = new ICoverageBuilder(coreModelService, patient, coverageService.getDefaultCoverageLabel(),
				coverageService.getDefaultCoverageReason(), "KGV").buildAndSave();

		ICoverage newCoverage = new ICoverageBuilder(coreModelService, patient,
				coverageService.getDefaultCoverageLabel(), coverageService.getDefaultCoverageReason(), "KGV")
				.buildAndSave();
		
		Optional<ICoverage> coverageWithLaw = coverageService.getCoverageWithLaw(testPatients.get(0), BillingLaw.KVG);
		assertTrue(coverageWithLaw.isPresent());
		assertEquals(coverageWithLaw.get(), newCoverage);

		coreModelService.remove(newCoverage);
		coreModelService.remove(coverage);
	}

}
