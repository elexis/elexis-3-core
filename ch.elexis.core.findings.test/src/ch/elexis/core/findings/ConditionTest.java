package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.test.AllTests;

public class ConditionTest {
	@Before
	public void beforeTest(){
		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID, IFinding.class);
		assertTrue(findings.isEmpty());
	}
	
	@Test
	public void manyConditions() {
		// create many
		for (int i = 0; i < 1000; i++) {
			ICondition condition = FindingsServiceComponent.getService().create(ICondition.class);
			assertNotNull(condition);
			// set the properties
			condition.setPatientId(AllTests.PATIENT_ID);
			LocalDate dateRecorded = LocalDate.of(2016, Month.DECEMBER, 29);
			condition.setDateRecorded(dateRecorded);
			condition.setCategory(ConditionCategory.PROBLEMLISTITEM);
			condition.setStatus(ConditionStatus.ACTIVE);
			condition.setText("Condition " + i);
			FindingsServiceComponent.getService().saveFinding(condition);
		}
		// test many
		List<ICondition> findings = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID,
				ICondition.class);
		assertEquals(1000, findings.size());
		for (ICondition iFinding : findings) {
			Optional<LocalDate> dateRecorded = iFinding.getDateRecorded();
			assertTrue(dateRecorded.isPresent());
			assertEquals(LocalDate.of(2016, Month.DECEMBER, 29), dateRecorded.get());
		}
	}

	@Test
	public void getProperties(){
		ICondition condition = FindingsServiceComponent.getService().create(ICondition.class);
		assertNotNull(condition);
		// set the properties
		condition.setPatientId(AllTests.PATIENT_ID);
		LocalDate dateRecorded = LocalDate.of(2016, Month.OCTOBER, 19);
		condition.setDateRecorded(dateRecorded);
		condition.setCategory(ConditionCategory.PROBLEMLISTITEM);
		condition.setStatus(ConditionStatus.ACTIVE);
		
		condition.addNote("first note");
		condition.addNote("second note\nthird note");
		
		LocalDateTime startTime = LocalDateTime.of(2016, Month.OCTOBER, 1, 12, 0, 0);
		condition.setStart(startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
		
		ICoding code = new ICoding() {
			
			@Override
			public String getSystem(){
				return "testSystem";
			}
			
			@Override
			public String getDisplay(){
				return "test display";
			}
			
			@Override
			public String getCode(){
				return "test";
			}
		};
		condition.setCoding(Collections.singletonList(code));
		
		condition.addStringExtension("test", "testValue");
		
		FindingsServiceComponent.getService().saveFinding(condition);
		
		List<ICondition> conditions = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID, ICondition.class);
		assertNotNull(conditions);
		assertFalse(conditions.isEmpty());
		assertEquals(1, conditions.size());
		// read condition and test the properties
		ICondition readcondition = conditions.get(0);
		assertEquals(AllTests.PATIENT_ID,
			readcondition.getPatientId());
		assertTrue(readcondition.getDateRecorded().isPresent());
		assertEquals(dateRecorded, readcondition.getDateRecorded().get());
		
		List<String> notes = readcondition.getNotes();
		assertNotNull(notes);
		assertFalse(notes.isEmpty());
		assertTrue(notes.get(0).equals("first note"));
		assertTrue(notes.get(1).equals("second note\nthird note"));
		
		assertTrue(readcondition.getStart().isPresent());
		assertEquals(startTime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
			readcondition.getStart().get());
		
		List<ICoding> coding = readcondition.getCoding();
		assertNotNull(coding);
		assertFalse(coding.isEmpty());
		assertEquals(coding.get(0).getDisplay(), "test display");
		
		Map<String, String> extensions = readcondition.getStringExtensions();
		assertFalse(extensions.isEmpty());
		assertTrue(extensions.containsKey("test"));
		assertEquals("testValue", extensions.get("test"));
	}
}
