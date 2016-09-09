package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.test.AllTests;

public class ChangeContentTest {
	
	@Before
	public void beforeTest(){
		AllTests.deleteAllFindings();
	}
	
	@Test
	public void changeEncounter(){
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
		IEncounter encounter = factory.createEncounter();
		assertNotNull(encounter);
		encounter.setConsultationId(AllTests.CONSULTATION_ID);
		encounter.setPatientId(AllTests.PATIENT_ID);
		LocalDateTime effectiveTime = LocalDateTime.of(2016, Month.SEPTEMBER, 12, 9, 33);
		encounter.setEffectiveTime(effectiveTime);
		FindingsServiceComponent.getService().saveFinding(encounter);
		
		List<IFinding> encounters = FindingsServiceComponent.getService()
			.getConsultationsFindings(AllTests.CONSULTATION_ID, IEncounter.class);
		assertNotNull(encounters);
		assertFalse(encounters.isEmpty());
		assertEquals(1, encounters.size());
		assertEquals(AllTests.CONSULTATION_ID,
			((IEncounter) encounters.get(0)).getConsultationId());
		assertTrue(encounters.get(0).getEffectiveTime().isPresent());
		assertEquals(effectiveTime, encounters.get(0).getEffectiveTime().get());
	}
}
