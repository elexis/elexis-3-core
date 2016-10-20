package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.test.AllTests;

public class CreateFindingsTest {
	
	@Before
	public void beforeTest(){
		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(AllTests.PATIENT_ID,
				IFinding.class);
		assertTrue(findings.isEmpty());
	}
	
	@Test
	public void createEncounter(){
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
		IEncounter encounter = factory.createEncounter();
		assertNotNull(encounter);
		encounter.setConsultationId(AllTests.CONSULTATION_ID);
		encounter.setPatientId(AllTests.PATIENT_ID);
		FindingsServiceComponent.getService().saveFinding(encounter);
		
		List<IFinding> encounters = FindingsServiceComponent.getService()
			.getConsultationsFindings(AllTests.CONSULTATION_ID, IEncounter.class);
		assertNotNull(encounters);
		assertFalse(encounters.isEmpty());
		assertEquals(1, encounters.size());
		assertEquals(AllTests.CONSULTATION_ID,
			((IEncounter) encounters.get(0)).getConsultationId());
	}
	
	@Test
	public void createCondition(){
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
		ICondition condition = factory.createCondition();
		condition.setPatientId(AllTests.PATIENT_ID);
		FindingsServiceComponent.getService().saveFinding(condition);
		
		List<IFinding> conditions = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID, ICondition.class);
		assertNotNull(conditions);
		assertFalse(conditions.isEmpty());
		assertEquals(1, conditions.size());
		assertEquals(AllTests.PATIENT_ID, conditions.get(0).getPatientId());
	}
	
	@Test
	public void createClinicalImpression(){
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
		IEncounter encounter = factory.createEncounter();
		assertNotNull(encounter);
		encounter.setConsultationId(AllTests.CONSULTATION_ID);
		encounter.setPatientId(AllTests.PATIENT_ID);
		FindingsServiceComponent.getService().saveFinding(encounter);
		IClinicalImpression clinicalImpression = factory.createClinicalImpression();
		clinicalImpression.setEncounter(encounter);
		FindingsServiceComponent.getService().saveFinding(clinicalImpression);
		
		List<IFinding> clinicalImpressions = FindingsServiceComponent.getService()
			.getConsultationsFindings(AllTests.CONSULTATION_ID, IClinicalImpression.class);
		assertNotNull(clinicalImpressions);
		assertFalse(clinicalImpressions.isEmpty());
		assertEquals(1, clinicalImpressions.size());
		assertEquals(AllTests.PATIENT_ID, clinicalImpressions.get(0).getPatientId());
	}
	
	@Test
	public void createObservation(){
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
		IEncounter encounter = factory.createEncounter();
		assertNotNull(encounter);
		encounter.setConsultationId(AllTests.CONSULTATION_ID);
		encounter.setPatientId(AllTests.PATIENT_ID);
		FindingsServiceComponent.getService().saveFinding(encounter);
		IObservation observation = factory.createObservation();
		observation.setEncounter(encounter);
		FindingsServiceComponent.getService().saveFinding(observation);
		
		List<IFinding> observations = FindingsServiceComponent.getService()
			.getConsultationsFindings(AllTests.CONSULTATION_ID, IObservation.class);
		assertNotNull(observations);
		assertFalse(observations.isEmpty());
		assertEquals(1, observations.size());
		assertEquals(AllTests.PATIENT_ID, observations.get(0).getPatientId());
	}
	
	@Test
	public void createProcedureRequest(){
		IFindingsFactory factory = FindingsServiceComponent.getService().getFindingsFactory();
		assertNotNull(factory);
		IEncounter encounter = factory.createEncounter();
		assertNotNull(encounter);
		encounter.setConsultationId(AllTests.CONSULTATION_ID);
		encounter.setPatientId(AllTests.PATIENT_ID);
		FindingsServiceComponent.getService().saveFinding(encounter);
		IProcedureRequest procedureRequest = factory.createProcedureRequest();
		procedureRequest.setEncounter(encounter);
		FindingsServiceComponent.getService().saveFinding(procedureRequest);
		
		List<IFinding> procedureRequests = FindingsServiceComponent.getService()
			.getConsultationsFindings(AllTests.CONSULTATION_ID, IProcedureRequest.class);
		assertNotNull(procedureRequests);
		assertFalse(procedureRequests.isEmpty());
		assertEquals(1, procedureRequests.size());
		assertEquals(AllTests.PATIENT_ID, procedureRequests.get(0).getPatientId());
	}
}
