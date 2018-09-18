package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.IObservation.ObservationCategory;
import ch.elexis.core.findings.IObservation.ObservationType;
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
		iObservation.setText("Source Observation");
		IObservation iSubObservation = FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iSubObservation);
		iSubObservation.setText("Target Observation");
		// add sub as target
		iObservation.addTargetObservation(iSubObservation, ObservationLinkType.REF);
		List<IObservation> targets = iObservation.getTargetObseravtions(ObservationLinkType.REF);
		assertNotNull(targets);
		assertFalse(targets.isEmpty());
		assertEquals(iSubObservation.getId(), targets.get(0).getId());
		// save and reload
		FindingsServiceComponent.getService().saveFinding(iObservation);
		Optional<IObservation> loaded = FindingsServiceComponent.getService()
			.findById(iObservation.getId(),
			IObservation.class);
		assertTrue(loaded.isPresent());
		targets = loaded.get().getTargetObseravtions(ObservationLinkType.REF);
		assertNotNull(targets);
		assertFalse(targets.isEmpty());
		assertEquals(iSubObservation.getId(), targets.get(0).getId());
		// remove save reload
		iObservation.removeTargetObservation(iSubObservation, ObservationLinkType.REF);
		FindingsServiceComponent.getService().saveFinding(iObservation);
		loaded = FindingsServiceComponent.getService()
			.findById(iObservation.getId(), IObservation.class);
		targets = loaded.get().getTargetObseravtions(ObservationLinkType.REF);
		assertNotNull(targets);
		assertTrue(targets.isEmpty());
	}
	
	@Test
	public void testObservationLink(){
		IObservation iObservation =
			FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iObservation);
		iObservation.setText("Source Observation");
		IObservation iSubObservation =
			FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iSubObservation);
		iSubObservation.setText("Target Observation");
		IObservation iSubSubObservation =
			FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iSubObservation);
		iSubObservation.setText("Targets Target Observation");
		// add sub as target
		iObservation.addTargetObservation(iSubObservation, ObservationLinkType.REF);
		iSubObservation.addTargetObservation(iSubSubObservation, ObservationLinkType.REF);
		List<IObservation> targets = iObservation.getTargetObseravtions(ObservationLinkType.REF);
		assertNotNull(targets);
		assertFalse(targets.isEmpty());
		assertEquals(iSubObservation.getId(), targets.get(0).getId());
		targets = iSubObservation.getTargetObseravtions(ObservationLinkType.REF);
		assertNotNull(targets);
		assertFalse(targets.isEmpty());
		assertEquals(iSubSubObservation.getId(), targets.get(0).getId());
		List<IObservation> sources = iSubObservation.getSourceObservations(ObservationLinkType.REF);
		assertNotNull(sources);
		assertFalse(sources.isEmpty());
		assertEquals(iObservation.getId(), sources.get(0).getId());
		sources = iSubSubObservation.getSourceObservations(ObservationLinkType.REF);
		assertNotNull(sources);
		assertFalse(sources.isEmpty());
		assertEquals(iSubObservation.getId(), sources.get(0).getId());
		iSubObservation.removeSourceObservation(iObservation, ObservationLinkType.REF);
		sources = iSubObservation.getSourceObservations(ObservationLinkType.REF);
		assertNotNull(sources);
		assertTrue(sources.isEmpty());
		targets = iObservation.getTargetObseravtions(ObservationLinkType.REF);
		assertNotNull(targets);
		assertTrue(targets.isEmpty());
		Optional<IObservation> loaded = FindingsServiceComponent.getService()
			.findById(iObservation.getId(),
			IObservation.class);
		targets = loaded.get().getTargetObseravtions(ObservationLinkType.REF);
		assertNotNull(targets);
		assertTrue(targets.isEmpty());
	}
	
	@Test
	public void testObservationValue(){
		IObservation iObservation =
			FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iObservation);
		iObservation.setText("Source Observation");
		IObservation iSubObservation =
			FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iSubObservation);
		iSubObservation.setText("Target Observation");
		IObservation iOtherSubObservation =
			FindingsServiceComponent.getService().create(IObservation.class);
		assertNotNull(iOtherSubObservation);
		iOtherSubObservation.setText("Other Target Observation");
		// add sub as target
		iObservation.addTargetObservation(iSubObservation, ObservationLinkType.REF);
		iObservation.addTargetObservation(iOtherSubObservation, ObservationLinkType.REF);
		List<IObservation> targets = iObservation.getTargetObseravtions(ObservationLinkType.REF);
		assertNotNull(targets);
		assertFalse(targets.isEmpty());
		assertEquals(2, targets.size());
		
		iObservation.setObservationType(ObservationType.REF);
		FindingsServiceComponent.getService().saveFinding(iObservation);
		iSubObservation.setObservationType(ObservationType.TEXT);
		iSubObservation.setStringValue("Test string value");
		FindingsServiceComponent.getService().saveFinding(iSubObservation);
		iOtherSubObservation.setObservationType(ObservationType.NUMERIC);
		iOtherSubObservation.setNumericValue(BigDecimal.valueOf(2.3), "test");
		FindingsServiceComponent.getService().saveFinding(iOtherSubObservation);
		
		Optional<IObservation> loaded = FindingsServiceComponent.getService()
			.findById(iObservation.getId(), IObservation.class);
		targets = loaded.get().getTargetObseravtions(ObservationLinkType.REF);
		for (IObservation subObservation : targets) {
			if (subObservation.getObservationType() == ObservationType.TEXT) {
				assertEquals("Test string value", subObservation.getStringValue().get());
			} else if (subObservation.getObservationType() == ObservationType.NUMERIC) {
				assertEquals("2.3", subObservation.getNumericValue().get().toPlainString());
				assertEquals("test", subObservation.getNumericValueUnit().get());
			} else {
				fail("Observation type not set");
			}
		}
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
