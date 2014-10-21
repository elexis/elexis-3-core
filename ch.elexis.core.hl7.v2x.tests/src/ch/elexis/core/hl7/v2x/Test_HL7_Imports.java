package ch.elexis.core.hl7.v2x;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.hl7.HL7PatientResolver;
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
	public static void beforeClass(){
		Patient dummyPatient = new Patient("test", "test", "01.01.01", Patient.FEMALE);
		resolver = new DummyPatientResolver(dummyPatient);
	}
	
	private void testGetReaderOneHL7file(File f){
		String name = f.getAbsolutePath();
		if (f.canRead() && (name.toLowerCase().endsWith(".hl7"))) {
			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(f);
			assertNotNull(hl7Readers);
			assertFalse(hl7Readers.isEmpty());
			assertNotNull(hl7Readers.get(0).getVersion());
			assertFalse(hl7Readers.get(0).getVersion().isEmpty());
		} else {
			System.out.println("Skipping Datei " + name);
		}
	}
	
	private void testGetObservationsOneHL7file(File f) throws ElexisException{
		String name = f.getAbsolutePath();
		if (f.canRead() && (name.toLowerCase().endsWith(".hl7"))) {
			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(f);
			ObservationMessage obs = hl7Readers.get(0).readObservation(resolver, false);
			assertNotNull(obs);
			assertNotNull(obs.getObservations());
			for (IValueType iValueType : obs.getObservations()) {
				if (iValueType instanceof LabResultData) {
					LabResultData hl7LabResult = (LabResultData) iValueType;
					assertNotNull(hl7LabResult.getValue());
				}
			}
			assertNotNull(hl7Readers.get(0).getPatient());
			assertEquals(resolver.getPatient().getVorname(), hl7Readers.get(0).getPatient()
				.getVorname());
		} else {
			System.out.println("Skipping Datei " + name);
		}
	}
	
	private void getReadersAllHL7files(File directory, TestType type) throws ElexisException{
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
	 * Test method for {@link ch.elexis.importers.HL7#HL7(java.lang.String, java.lang.String)}.
	 * 
	 * @throws ElexisException
	 */
	@Test
	public void testGetObservationsHL7files() throws ElexisException{
		System.out.println("testHL7files in elexis-import_test/rsc: This will take some time");
		getReadersAllHL7files(new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
			"rsc"), TestType.OBSERVATION);
	}
	
	/**
	 * Test method for {@link ch.elexis.importers.HL7#HL7(java.lang.String, java.lang.String)}.
	 * 
	 * @throws ElexisException
	 */
	@Test
	public void testGetReaderHL7files() throws ElexisException{
		System.out.println("testHL7files in elexis-import_test/rsc: This will take some time");
		getReadersAllHL7files(new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
			"rsc"), TestType.READ);
	}
	
	private static class DummyPatientResolver extends HL7PatientResolver {
		
		private Patient patient;
		
		public Patient getPatient(){
			return patient;
		}
		
		public DummyPatientResolver(Patient dummyPatient){
			this.patient = dummyPatient;
		}
		
		@Override
		public Patient resolvePatient(String firstname, String lastname, String birthDate){
			return patient;
		}
		
		@Override
		public boolean matchPatient(Patient patient, String firstname, String lastname,
			String birthDate){
			return false;
		}
	}
}
