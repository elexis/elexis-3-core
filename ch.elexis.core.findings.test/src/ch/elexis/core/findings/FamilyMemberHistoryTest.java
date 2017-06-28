package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.test.AllTests;

public class FamilyMemberHistoryTest {
	
	@Before
	public void beforeTest(){
		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(AllTests.PATIENT_ID,
				IFinding.class);
		assertTrue(findings.isEmpty());
	}
	
	@Test
	public void manyFamilyMemberHistories(){
		// create many
		for (int i = 0; i < 1000; i++) {
			IFamilyMemberHistory familyMemberHistory =
				FindingsServiceComponent.getService().create(IFamilyMemberHistory.class);
			assertNotNull(familyMemberHistory);
			// set the properties
			familyMemberHistory.setPatientId(AllTests.PATIENT_ID);
			familyMemberHistory.setText("FamMemberHistory " + i);

			FindingsServiceComponent.getService().saveFinding(familyMemberHistory);
		}
		// test many
		List<IFamilyMemberHistory> findings = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID,
				IFamilyMemberHistory.class);
		assertEquals(1000, findings.size());
		for (IFamilyMemberHistory iFinding : findings) {
			assertTrue(iFinding.getText().isPresent());
			assertFalse(iFinding.getId().isEmpty());
		}
	}
}
