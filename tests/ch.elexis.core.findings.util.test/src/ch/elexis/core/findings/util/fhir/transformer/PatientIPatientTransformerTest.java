package ch.elexis.core.findings.util.fhir.transformer;

import static org.junit.Assert.assertNotNull;

import org.hl7.fhir.r4.model.Patient;
import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.findings.util.fhir.IFhirTransformer;
import ch.elexis.core.model.IPatient;

public class PatientIPatientTransformerTest {

	private static IFhirTransformer<Patient, IPatient> transformer;

	@BeforeClass
	public static void beforeClass() {
		transformer = (IFhirTransformer<Patient, IPatient>) AllTransformerTests.getTransformerRegistry()
				.getTransformerFor(Patient.class, IPatient.class);
		assertNotNull(transformer);
	}

	@Test
	public void promotePersonToPatient() {

	}

	@Test
	public void demotePersonToPatientFails() {

	}

}
