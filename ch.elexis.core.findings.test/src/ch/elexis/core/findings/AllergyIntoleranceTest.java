package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.test.AllTests;

public class AllergyIntoleranceTest {
	
	@Before
	public void beforeTest(){
		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(AllTests.PATIENT_ID,
				IFinding.class);
		assertTrue(findings.isEmpty());
	}
	
	@Test
	public void maynAllergyIntolerance(){
		// create many
		for (int i = 0; i < 1000; i++) {
			IAllergyIntolerance allergyIntolerance =
				FindingsServiceComponent.getService().create(IAllergyIntolerance.class);
			assertNotNull(allergyIntolerance);
			// set the properties
			allergyIntolerance.setPatientId(AllTests.PATIENT_ID);
			allergyIntolerance.setText("AllergyIntolerance " + i);

			FindingsServiceComponent.getService().saveFinding(allergyIntolerance);
		}
		// test many
		List<IAllergyIntolerance> findings = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID,
				IAllergyIntolerance.class);
		assertEquals(1000, findings.size());
		for (IAllergyIntolerance iFinding : findings) {
			assertTrue(iFinding.getText().isPresent());
			assertFalse(iFinding.getId().isEmpty());
		}
	}
}
