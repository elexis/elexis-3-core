package ch.elexis.core.hl7.v2x;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.PlatformHelper;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.HL7ReaderFactory;
import ch.elexis.hl7.model.IValueType;
import ch.elexis.hl7.model.LabResultData;
import ch.elexis.hl7.model.ObservationMessage;

public class Test_HL7_v24_Imports {

	private static DummyPatientResolver resolver;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		IPatient dummyPatient = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "Christoph",
				"Grissemann", LocalDate.of(1966, 5, 17), Gender.MALE).buildAndSave();
		resolver = new DummyPatientResolver(dummyPatient);
	}

	@Test
	public void testGetSender() throws ElexisException, IOException {
		File[] files = loadv24Files();
		assertNotSame(0, files.length);

		for (int i = 0; i < files.length; i++) {
			File file = files[i];

			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(file);
			assertNotNull(hl7Readers);
			assertEquals(1, hl7Readers.size());
			HL7Reader reader = hl7Readers.get(0);
			System.out.println("Version [" + reader.getVersion() + "]");
			assertNotNull(reader.getSender());
			System.out.println("Sender [" + reader.getSender() + "]");
		}
	}

	@Test
	public void testGetPatient() throws ElexisException, IOException {
		File[] files = loadv24Files();
		assertNotSame(0, files.length);

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(file);
			assertNotNull(hl7Readers);
			assertEquals(1, hl7Readers.size());
			HL7Reader reader = hl7Readers.get(0);
			reader.readObservation(resolver, false);

			IPatient patient = reader.getPatient();
			assertEquals("Grissemann", patient.getLastName());
			assertTrue(patient.getDateOfBirth().atZone(ZoneId.systemDefault())
					.isEqual(LocalDate.of(1966, 5, 17).atStartOfDay(ZoneId.systemDefault())));
			assertEquals(Gender.MALE, patient.getGender());
		}
	}

	@Test
	public void testReadObservation() throws ElexisException, IOException {
		File[] files = loadv24Files();
		assertNotSame(0, files.length);

		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			System.out.println("TESTING Hl7 Observation of... " + file.getAbsolutePath());

			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(file);
			assertNotNull(hl7Readers);
			assertEquals(1, hl7Readers.size());
			HL7Reader reader = hl7Readers.get(0);

			ObservationMessage observationMsg = reader.readObservation(resolver, false);
			List<IValueType> observations = observationMsg.getObservations();
			System.out.println("Observations [" + observations.size() + "]");
			boolean valueFound = false;
			assertFalse(observations.isEmpty());
			for (IValueType iValueType : observations) {
				if (iValueType instanceof LabResultData) {
					LabResultData data = (LabResultData) iValueType;
					if (data.isNumeric()) {
						assertNotNull(data.getCode());
					}
					assertNotNull(data.getValue());
					valueFound = true;
				}
			}
			assertTrue(valueFound);
		}
	}

	private File[] loadv24Files() {
		File directory = new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"), "rsc/v24");

		if (directory.exists() && directory.isDirectory()) {
			return directory.listFiles();
		}
		return new File[] {};

	}

}
