package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.test.AllTests;

public class EncounterTest {
	
	@Before
	public void beforeTest(){
		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(AllTests.PATIENT_ID,
				IFinding.class);
		assertTrue(findings.isEmpty());
	}
	
	@Test
	public void manyEncounters() {
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
		// create many
		for (int i = 0; i < 1000; i++) {
			IEncounter encounter = factory.createEncounter();
			assertNotNull(encounter);
			// set the properties
			encounter.setConsultationId(AllTests.CONSULTATION_ID);
			encounter.setPatientId(AllTests.PATIENT_ID);
			encounter.setStartTime(LocalDateTime.of(2016, Month.DECEMBER, 29, 9, 56));
			encounter.setText("Encounter " + i);

			FindingsServiceComponent.getService().saveFinding(encounter);
		}
		// test many
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(AllTests.PATIENT_ID,
				IEncounter.class);
		assertEquals(1000, findings.size());
		for (IFinding iFinding : findings) {
			assertTrue(iFinding instanceof IEncounter);
			Optional<LocalDateTime> startTime = ((IEncounter) iFinding).getStartTime();
			assertTrue(startTime.isPresent());
			assertEquals(LocalDateTime.of(2016, Month.DECEMBER, 29, 9, 56), startTime.get());
		}
	}

	@Test
	public void getProperties(){
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
		IEncounter encounter = factory.createEncounter();
		assertNotNull(encounter);
		encounter.setConsultationId(AllTests.CONSULTATION_ID);
		encounter.setPatientId(AllTests.PATIENT_ID);
		LocalDateTime effectiveTime = LocalDateTime.of(2016, Month.SEPTEMBER, 12, 9, 33);
		encounter.setStartTime(effectiveTime);
		FindingsServiceComponent.getService().saveFinding(encounter);
		
		List<IFinding> encounters = FindingsServiceComponent.getService()
				.getConsultationsFindings(AllTests.CONSULTATION_ID, IEncounter.class);
		assertNotNull(encounters);
		assertFalse(encounters.isEmpty());
		assertEquals(1, encounters.size());
		IEncounter readEncounter = (IEncounter) encounters.get(0);
		assertEquals(AllTests.CONSULTATION_ID,
			readEncounter.getConsultationId());
		assertTrue(readEncounter.getStartTime().isPresent());
		assertEquals(effectiveTime, readEncounter.getStartTime().get());
	}
}
