package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import ch.elexis.core.findings.test.AllTests;

public class ProcedureRequestTest {
	@Before
	public void beforeTest() {
		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(AllTests.PATIENT_ID,
				IFinding.class);
		assertTrue(findings.isEmpty());
	}

	@Test
	public void manyProcedureRequests() {

		IEncounter encounter = FindingsServiceComponent.getService().create(IEncounter.class);
		assertNotNull(encounter);
		encounter.setConsultationId(AllTests.CONSULTATION_ID);
		encounter.setPatientId(AllTests.PATIENT_ID);
		encounter.setStartTime(LocalDateTime.of(2016, Month.DECEMBER, 29, 9, 56));
		FindingsServiceComponent.getService().saveFinding(encounter);

		// create many
		for (int i = 0; i < 1000; i++) {
			IProcedureRequest procedureRequest =
				FindingsServiceComponent.getService().create(IProcedureRequest.class);
			assertNotNull(procedureRequest);
			// set the properties
			procedureRequest.setPatientId(AllTests.PATIENT_ID);
			procedureRequest.setText("ProcedureRequest " + i);
			procedureRequest.setEncounter(encounter);
			FindingsServiceComponent.getService().saveFinding(procedureRequest);
		}
		// test many
		List<IProcedureRequest> findings = FindingsServiceComponent.getService()
			.getPatientsFindings(AllTests.PATIENT_ID,
				IProcedureRequest.class);
		assertEquals(1000, findings.size());
		for (IProcedureRequest iFinding : findings) {
			assertEquals(iFinding.getEncounter().get().getId(), encounter.getId());
		}
	}

	@Test
	@Ignore("Not implemented!")
	public void getProperties() {
		IEncounter encounter = FindingsServiceComponent.getService().create(IEncounter.class);
		assertNotNull(encounter);
		encounter.setConsultationId(AllTests.CONSULTATION_ID);
		encounter.setPatientId(AllTests.PATIENT_ID);
		encounter.setStartTime(LocalDateTime.of(2016, Month.DECEMBER, 29, 9, 56));
		FindingsServiceComponent.getService().saveFinding(encounter);

		IProcedureRequest procedureRequest =
			FindingsServiceComponent.getService().create(IProcedureRequest.class);
		assertNotNull(procedureRequest);
		// set the properties
		procedureRequest.setPatientId(AllTests.PATIENT_ID);
		procedureRequest.setEncounter(encounter);
		LocalDateTime scheduledTime = LocalDateTime.of(2016, Month.JANUARY, 01, 0, 1);
		procedureRequest.setScheduledTime(scheduledTime);
		ICoding code = new ICoding() {

			@Override
			public String getSystem() {
				return "testSystem";
			}

			@Override
			public String getDisplay() {
				return "test display";
			}

			@Override
			public String getCode() {
				return "test";
			}
		};
		procedureRequest.setCoding(Collections.singletonList(code));

		FindingsServiceComponent.getService().saveFinding(procedureRequest);

		List<IProcedureRequest> procedureRequests = FindingsServiceComponent.getService()
				.getConsultationsFindings(encounter.getConsultationId(),
				IProcedureRequest.class);
		assertNotNull(procedureRequests);
		assertFalse(procedureRequests.isEmpty());
		assertEquals(1, procedureRequests.size());
		// read procedure request and test the properties
		IProcedureRequest readProcedureRequest = (IProcedureRequest) procedureRequests.get(0);
		assertEquals(AllTests.PATIENT_ID, readProcedureRequest.getPatientId());
		assertTrue(readProcedureRequest.getScheduledTime().isPresent());
		assertEquals(LocalDateTime.of(2016, Month.JANUARY, 01, 0, 1), readProcedureRequest.getScheduledTime().get());

		List<ICoding> coding = readProcedureRequest.getCoding();
		assertNotNull(coding);
		assertFalse(coding.isEmpty());
		assertEquals(coding.get(0).getDisplay(), "test display");
	}
}
