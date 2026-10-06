package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.test.AllTests;
import ch.elexis.core.findings.util.FindingSortOrder;

public class FindingSortOrderTest {
	@Before
	public void beforeTest() {
		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(AllTests.PATIENT_ID,
				IFinding.class);
		assertTrue(findings.isEmpty());
	}

	@Test
	public void sortConditions() {
		ICondition conditionA = FindingsServiceComponent.getService().create(ICondition.class);
		assertNotNull(conditionA);
		conditionA.setPatientId(AllTests.PATIENT_ID);
		conditionA.setDateRecorded(LocalDate.of(2020, Month.JANUARY, 1));
		conditionA.setCategory(ConditionCategory.PROBLEMLISTITEM);
		conditionA.setStatus(ConditionStatus.ACTIVE);
		FindingSortOrder.setSortOrder(conditionA, 1);

		ICondition conditionB = FindingsServiceComponent.getService().create(ICondition.class);
		assertNotNull(conditionB);
		conditionB.setPatientId(AllTests.PATIENT_ID);
		conditionB.setDateRecorded(LocalDate.of(2021, Month.JANUARY, 1));
		conditionB.setCategory(ConditionCategory.PROBLEMLISTITEM);
		conditionB.setStatus(ConditionStatus.ACTIVE);
		FindingSortOrder.setSortOrder(conditionB, 0);

		ICondition conditionC = FindingsServiceComponent.getService().create(ICondition.class);
		assertNotNull(conditionC);
		conditionC.setPatientId(AllTests.PATIENT_ID);
		conditionC.setDateRecorded(LocalDate.of(2019, Month.JANUARY, 1));
		conditionC.setCategory(ConditionCategory.PROBLEMLISTITEM);
		conditionC.setStatus(ConditionStatus.ACTIVE);

		ICondition conditionD = FindingsServiceComponent.getService().create(ICondition.class);
		assertNotNull(conditionD);
		conditionD.setPatientId(AllTests.PATIENT_ID);
		conditionD.setDateRecorded(LocalDate.of(2022, Month.JANUARY, 1));
		conditionD.setCategory(ConditionCategory.PROBLEMLISTITEM);
		conditionD.setStatus(ConditionStatus.ACTIVE);

		List<ICondition> conditions = Arrays.asList(conditionA, conditionB, conditionC, conditionD);
		conditions.sort(FindingSortOrder.comparator(FindingSortOrder.BY_DATE_RECORDED_DESC));
		assertEquals(Arrays.asList(conditionD, conditionC, conditionB, conditionA), conditions);
	}

	@Test
	public void replaceSortOrder() {
		ICondition condition = FindingsServiceComponent.getService().create(ICondition.class);
		assertNotNull(condition);
		condition.setPatientId(AllTests.PATIENT_ID);
		condition.setDateRecorded(LocalDate.of(2020, Month.JANUARY, 1));
		condition.setCategory(ConditionCategory.PROBLEMLISTITEM);
		condition.setStatus(ConditionStatus.ACTIVE);
		FindingSortOrder.setSortOrder(condition, 1);
		FindingSortOrder.setSortOrder(condition, 2);
		FindingsServiceComponent.getService().saveFinding(condition);

		List<ICondition> conditions = FindingsServiceComponent.getService().getPatientsFindings(AllTests.PATIENT_ID,
				ICondition.class);
		assertEquals(1, conditions.size());
		assertEquals(Optional.of(2), FindingSortOrder.getSortOrder(conditions.get(0)));
	}

	@Test
	public void getSortOrderWithoutExtension() {
		ICondition condition = FindingsServiceComponent.getService().create(ICondition.class);
		assertNotNull(condition);
		condition.setPatientId(AllTests.PATIENT_ID);
		condition.setDateRecorded(LocalDate.of(2020, Month.JANUARY, 1));
		condition.setCategory(ConditionCategory.PROBLEMLISTITEM);
		condition.setStatus(ConditionStatus.ACTIVE);

		assertEquals(Optional.empty(), FindingSortOrder.getSortOrder(condition));
	}
}
