package ch.elexis.core.hl7.v2x;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
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

public class Test_HL7_Imports {

	private static DummyPatientResolver resolver;

	private enum TestType {
		READ, OBSERVATION
	};

	@BeforeClass
	public static void beforeClass() {
		IPatient dummyPatient = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(), "test", "test",
				LocalDate.of(2001, 1, 1), Gender.FEMALE).buildAndSave();
		resolver = new DummyPatientResolver(dummyPatient);
	}

	private void testGetReaderOneHL7file(File f) throws IOException {
		String name = f.getAbsolutePath();
		if (f.canRead() && (name.toLowerCase().endsWith(".hl7"))) {
			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(f);
			assertNotNull(hl7Readers);
			assertFalse(hl7Readers.isEmpty());
			assertNotNull(hl7Readers.get(0).getVersion());
			assertFalse(hl7Readers.get(0).getVersion().isEmpty());
			System.out.println("Selected Reader [" + hl7Readers.get(0).getClass().getName() + "]");
		} else {
			System.out.println("Skipping Datei " + name);
		}
	}

	private void testGetObservationsOneHL7file(File f) throws ElexisException, IOException {
		String name = f.getAbsolutePath();
		if (f.canRead() && (name.toLowerCase().endsWith(".hl7"))) {
			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(f);
			String raw = null;
			try {
				raw = Files.readString(f.toPath(), java.nio.charset.StandardCharsets.UTF_8);
			} catch (java.nio.charset.MalformedInputException e) {
				raw = Files.readString(f.toPath(), java.nio.charset.StandardCharsets.ISO_8859_1);
				System.out.println("Fallback auf ISO-8859-1 für Datei: " + f.getName());
			}
			if (raw.contains("11488-4") || raw.toLowerCase().contains("consult note")) {
				System.out.println("Skipping KI report file: " + f.getName());
				return;
			}
			ObservationMessage obs = hl7Readers.get(0).readObservation(resolver, false);
			assertNotNull(hl7Readers.get(0).getSender());
			assertNotNull(obs);
			assertNotNull(obs.getObservations());
			for (IValueType iValueType : obs.getObservations()) {
				if (iValueType instanceof LabResultData) {
					LabResultData hl7LabResult = (LabResultData) iValueType;
					assertNotNull(hl7LabResult.getValue());
				}
			}
			assertNotNull(hl7Readers.get(0).getPatient());
			assertEquals(resolver.getPatient().getFirstName(), hl7Readers.get(0).getPatient().getFirstName());
		} else {
			System.out.println("Skipping Datei " + name);
		}
	}

	private void getReadersAllHL7files(File directory, TestType type) throws ElexisException, IOException {
		File[] files = directory.listFiles();
		int nrFiles = 0;
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			if (file.isDirectory()) {
				getReadersAllHL7files(file, type);
			} else {
				System.out.println("TESTING..." + file.getAbsolutePath());
				if (type == TestType.READ) {
					testGetReaderOneHL7file(file);
				} else if (type == TestType.OBSERVATION) {
					testGetObservationsOneHL7file(file);
				}
				nrFiles += 1;
			}
		}
		System.out.println("testHL7files: " + nrFiles + " files in " + directory.toString());
	}

	/**
	 * Test method for
	 * {@link ch.elexis.importers.HL7#HL7(java.lang.String, java.lang.String)}.
	 *
	 * @throws ElexisException
	 * @throws IOException
	 */
	@Test
	public void testGetObservationsHL7files() throws ElexisException, IOException {
		System.out.println("testHL7files in elexis-import_test/rsc: This will take some time");
		getReadersAllHL7files(new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"), "rsc"),
				TestType.OBSERVATION);
	}

	/**
	 * Test method for
	 * {@link ch.elexis.importers.HL7#HL7(java.lang.String, java.lang.String)}.
	 *
	 * @throws ElexisException
	 * @throws IOException
	 */
	@Test
	public void testGetReaderHL7files() throws ElexisException, IOException {
		System.out.println("testHL7files in elexis-import_test/rsc: This will take some time");
		getReadersAllHL7files(new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"), "rsc"),
				TestType.READ);
	}
}
