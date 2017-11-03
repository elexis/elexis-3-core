package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
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
	public void testObservationCreation() {
		IObservation iObservation = FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iObservation);
		IObservation iSubObservation = FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iSubObservation);
		// add sub as target
		iObservation.addTargetObservation(iSubObservation, ObservationLinkType.REF);
		List<IObservation> targets = iObservation.getTargetObseravtions(ObservationLinkType.REF);
		assertNotNull(targets);
		assertFalse(targets.isEmpty());
		assertEquals(iSubObservation.getId(), targets.get(0).getId());
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
		testObservationText("<3 & >3", 8);
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
		
		List<IObservation> findings = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID,
				IObservation.class);
		assertEquals(size, findings.size());
		IObservation found = findings.get(size - 1);
		Assert.assertTrue("found [" + found.getText().get() + "] not equals [" + text + "]",
			found.getText().get().equals(text));
		Assert.assertEquals(ObservationCategory.SOCIALHISTORY, found.getCategory());
	}
	
	@Test
	public void saveObservationsWithInvalidCharacters(){
		IObservation iObservation =
			FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iObservation);
		// set the properties
		iObservation.setCategory(ObservationCategory.SOCIALHISTORY);
		iObservation.setPatientId(AllTests.PATIENT_ID);
		iObservation.setText("ABCD" + String.valueOf((char) 0xa0) + "EFG");
		
		Optional<String> validText = iObservation.getText();
		assertTrue(validText.isPresent());
		assertEquals(validText.get().indexOf(0xa0), -1);
		assertTrue(validText.get().indexOf(' ') != -1);
	}
}
