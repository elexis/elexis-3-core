package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.test.AllTests;

public class ObservationTest {
	
	@Before
	public void beforeTest(){
		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(AllTests.PATIENT_ID,
				IFinding.class);
		assertTrue(findings.isEmpty());
	}
	
	@Test
	public void saveObservationsWithSpecialTexts(){
		testObservationText("ABC DEF abz", 1);
		testObservationText("124567890", 2);
		testObservationText("aa! bbb ? 129982.,,;: 'aa' 	b.+-*/-", 3);
		testObservationText("ABC &sect; ABC1", 4);
		testObservationText("ABC &copy; &sect; ABC2", 5);
		testObservationText("!$%&/()=?`´*#'*@€^°\n\n\ta", 6);
		testObservationText("§", 7); //BUG!!!!
	}

	private void testObservationText(String text, int size){

		IObservation iObservation =
			FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iObservation);
		// set the properties
		iObservation.setCategory(ObservationCategory.SOCIALHISTORY);
		iObservation.setPatientId(AllTests.PATIENT_ID);
		iObservation.setText(text);
		
		FindingsServiceComponent.getService().saveFinding(iObservation);
		
		// test many
		List<IObservation> findings = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID,
				IObservation.class);
		assertEquals(size, findings.size());
		IObservation found = findings.get(size - 1);
		Assert.assertTrue(found.getText().get().equals(text));
		Assert.assertEquals(ObservationCategory.SOCIALHISTORY, found.getCategory());
			

	}
}
