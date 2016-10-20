package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

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
	public void getProperties(){
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
		ICondition condition = factory.createCondition();
		assertNotNull(condition);
		condition.setPatientId(AllTests.PATIENT_ID);
		LocalDate dateRecorded = LocalDate.of(2016, Month.OCTOBER, 19);
		condition.setDateRecorded(dateRecorded);
		condition.setCategory(ConditionCategory.DIAGNOSIS);
		condition.setStatus(ConditionStatus.ACTIVE);
		FindingsServiceComponent.getService().saveFinding(condition);
		
		List<IFinding> conditions = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID, ICondition.class);
		assertNotNull(conditions);
		assertFalse(conditions.isEmpty());
		assertEquals(1, conditions.size());
		ICondition readcondition = (ICondition) conditions.get(0);
		assertEquals(AllTests.PATIENT_ID,
			readcondition.getPatientId());
		assertTrue(readcondition.getDateRecorded().isPresent());
		assertEquals(dateRecorded, readcondition.getDateRecorded().get());
	}
}
