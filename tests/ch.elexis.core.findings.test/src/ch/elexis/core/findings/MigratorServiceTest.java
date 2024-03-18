package ch.elexis.core.findings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.sql.SQLException;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.findings.IObservation.ObservationCode;
import ch.elexis.core.findings.test.AllTests;
import ch.elexis.core.findings.util.model.TransientCoding;
import ch.elexis.data.Patient;

public class MigratorServiceTest {

	private Patient patient;

	@Before
	public void beforeTest() throws SQLException {
		assertNotNull(FindingsServiceComponent.getService());
		assertNotNull(MigratorServiceComponent.getService());

		patient = new Patient("test", "test", "01.01.1970", Patient.FEMALE);

		AllTests.deleteAllFindings();
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(patient.getId(),
				IFinding.class);
		assertTrue(findings.isEmpty());
	}

	@After
	public void afterTest() {
		if(patient != null && patient.exists()) {
			patient.removeFromDatabase();
		}
	}

	@Test
	public void migratePersonalAnamnesis() {
		patient.setPersonalAnamnese("tobias_test@yahoo.xy");

		MigratorServiceComponent.getService().migratePatientsFindings(patient.getId(), IObservation.class,
				new TransientCoding(ObservationCode.ANAM_PERSONAL));
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(patient.getId(),
				IFinding.class);
		assertFalse(findings.isEmpty());
		assertEquals("tobias_test@yahoo.xy", findings.get(0).getText().get());
	}

	@Test
	public void migratePatientRiskfactors() {
		patient.setRisk("tobias_test@yahoo.xy");

		MigratorServiceComponent.getService().migratePatientsFindings(patient.getId(), IObservation.class,
				new TransientCoding(ObservationCode.ANAM_RISK));
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(patient.getId(),
				IFinding.class);
		assertFalse(findings.isEmpty());
		assertEquals("tobias_test@yahoo.xy", findings.get(0).getText().get());
	}

	@Test
	public void migratePatientCondition() {
		patient.setDiagnosen("tobias_test@yahoo.xy");

		MigratorServiceComponent.getService().migratePatientsFindings(patient.getId(), ICondition.class, null);
		List<IFinding> findings = FindingsServiceComponent.getService().getPatientsFindings(patient.getId(),
				IFinding.class);
		assertFalse(findings.isEmpty());
		assertEquals("tobias_test@yahoo.xy", findings.get(0).getText().get());
	}
}
