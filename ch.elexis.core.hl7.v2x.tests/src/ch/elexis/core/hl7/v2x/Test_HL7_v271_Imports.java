package ch.elexis.core.hl7.v2x;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.icu.text.SimpleDateFormat;

import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.hl7.v2x.Test_HL7_Imports.DummyPatientResolver;
import ch.elexis.data.Patient;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.HL7ReaderFactory;
import ch.elexis.hl7.model.EncapsulatedData;
import ch.elexis.hl7.model.IValueType;
import ch.elexis.hl7.model.LabResultData;
import ch.elexis.hl7.model.ObservationMessage;

public class Test_HL7_v271_Imports {
	
	private static DummyPatientResolver resolver;
	
	private enum TestType {
			READ, OBSERVATION
	};
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		Patient dummyPatient = new Patient("Grissemann", "Christoph", "17.05.1966", Patient.MALE);
		resolver = new DummyPatientResolver(dummyPatient);
	}
	
	@Test
	public void testGetVersion(){
		File[] files = loadv271Files();
		assertNotSame(0, files.length);
		
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			System.out.println("TESTING Hl7Version of... " + file.getAbsolutePath());
			
			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(file);
			assertNotNull(hl7Readers);
			assertEquals(1, hl7Readers.size());
			HL7Reader reader = hl7Readers.get(0);
			assertEquals("2.6", reader.getVersion());
		}
	}
	
	@Test
	public void testGetSenderSendingFacilityGiven() throws ElexisException{
		File tstFile1 = loadSpecificv271File("v271test1.hl7");
		assertNotNull(tstFile1);
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(tstFile1);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals("poctGate", reader.getSender());
		
	}
	
	@Test
	public void testGetSenderSendingApplicationGiven() throws ElexisException{
		File tstFile2 = loadSpecificv271File("v271test2.hl7");
		assertNotNull(tstFile2);
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(tstFile2);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals("poctGate", reader.getSender());
	}
	
	@Test
	public void testGetSenderSendingFacilityAndApplicationGiven() throws ElexisException{
		File alerelisFile = loadSpecificv271File("alerelis.hl7");
		assertNotNull(alerelisFile);
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(alerelisFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals("poctGate", reader.getSender());
	}
	
	@Test
	public void testGetPatient() throws ElexisException{
		File tstFile3 = loadSpecificv271File("v271test3.hl7");
		assertNotNull(tstFile3);
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(tstFile3);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		reader.readObservation(resolver, false);
		
		Patient patient = reader.getPatient();
		assertEquals("Grissemann", patient.getName());
		assertEquals("17.05.1966", patient.getGeburtsdatum());
		assertEquals(Patient.MALE, patient.getGeschlecht());
	}
	
	@Test
	public void testReadObservation() throws ElexisException{
		File[] files = loadv271Files();
		assertNotSame(0, files.length);
		
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			System.out.println("TESTING Hl7Version of... " + file.getAbsolutePath());
			
			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(file);
			assertNotNull(hl7Readers);
			assertEquals(1, hl7Readers.size());
			HL7Reader reader = hl7Readers.get(0);
			
			ObservationMessage observationMsg = reader.readObservation(resolver, false);
			List<IValueType> observations = observationMsg.getObservations();
			int labResultDataCounter = 0;
			int encDataCounter = 0;
			for (IValueType iValueType : observations) {
				if (iValueType instanceof LabResultData) {
					labResultDataCounter++;
				} else if (iValueType instanceof EncapsulatedData) {
					encDataCounter++;
				}
			}
			
			if (file.getName().startsWith("alerelis")) {
				assertEquals(27, observations.size());
				assertEquals(24, labResultDataCounter);
				assertEquals(3, encDataCounter);
			} else if (file.getName().startsWith("v271test")) {
				assertEquals(9, observations.size());
				assertEquals(9, labResultDataCounter);
				assertEquals(0, encDataCounter);
			}
		}
	}
	
	@Test
	public void testReadObservationOfAlerelisFile() throws ElexisException{
		File alerelisFile = loadSpecificv271File("alerelis.hl7");
		assertNotNull(alerelisFile);
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(alerelisFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		Date msgDate = observationMsg.getDateTimeOfMessage();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss");
		assertEquals("18.11.2015-11:26:21", sdf.format(msgDate));
		
		List<IValueType> observations = observationMsg.getObservations();
		assertEquals(27, observations.size());
		
		assertTrue(observations.get(0) instanceof LabResultData);
		assertTrue(observations.get(2) instanceof LabResultData);
		assertTrue(observations.get(3) instanceof LabResultData);
		assertTrue(observations.get(7) instanceof LabResultData);
		assertTrue(observations.get(13) instanceof LabResultData);
		assertTrue(observations.get(22) instanceof EncapsulatedData);
		assertTrue(observations.get(23) instanceof EncapsulatedData);
		assertTrue(observations.get(24) instanceof EncapsulatedData);
		assertTrue(observations.get(25) instanceof LabResultData);
		
		LabResultData lrWBC = (LabResultData) observations.get(0);
		assertEquals("WBC", lrWBC.getName());
		assertEquals("4.43", lrWBC.getValue());
		assertEquals("10^9/l", lrWBC.getUnit());
		assertNull(lrWBC.getDate());
		assertNull(lrWBC.getOBRDateTime());
		
		LabResultData lrMON = (LabResultData) observations.get(2);
		assertEquals("MON", lrMON.getName());
		assertEquals("0.33", lrMON.getValue());
		assertEquals("10^9/l", lrMON.getUnit());
		assertNull(lrMON.getDate());
		assertNull(lrMON.getOBRDateTime());
		
		LabResultData lrGRA = (LabResultData) observations.get(3);
		assertEquals("GRA", lrGRA.getName());
		assertEquals("2.59", lrGRA.getValue());
		assertEquals("10^9/l", lrGRA.getUnit());
		assertNull(lrGRA.getDate());
		assertNull(lrGRA.getOBRDateTime());
		
		LabResultData lrRBC = (LabResultData) observations.get(7);
		assertEquals("RBC", lrRBC.getName());
		assertEquals("4.29", lrRBC.getValue());
		assertEquals("10^12/l", lrRBC.getUnit());
		assertNull(lrRBC.getDate());
		assertNull(lrRBC.getOBRDateTime());
		
		LabResultData lrRDWc = (LabResultData) observations.get(13);
		assertEquals("RDWc", lrRDWc.getName());
		assertEquals("15.8", lrRDWc.getValue());
		assertEquals("%", lrRDWc.getUnit());
		assertNull(lrRDWc.getDate());
		assertNull(lrRDWc.getOBRDateTime());
		
		EncapsulatedData encData1 = (EncapsulatedData) observations.get(22);
		assertEquals("image/jpeg", encData1.getName());
		assertEquals("0023", encData1.getSequence());
		assertNotNull(encData1.getData());
		assertNull(encData1.getDate());
		assertNull(encData1.getComment());
		
		EncapsulatedData encData2 = (EncapsulatedData) observations.get(23);
		assertEquals("image/jpeg", encData2.getName());
		assertEquals("0024", encData2.getSequence());
		assertNotNull(encData2.getData());
		assertNull(encData2.getDate());
		assertNull(encData2.getComment());
		
		EncapsulatedData encData3 = (EncapsulatedData) observations.get(24);
		assertEquals("image/jpeg", encData3.getName());
		assertEquals("0025", encData3.getSequence());
		assertNotNull(encData3.getData());
		assertNull(encData3.getDate());
		assertNull(encData3.getComment());
		
		LabResultData lrHuman = (LabResultData) observations.get(25);
		assertEquals("Mode", lrHuman.getName());
		assertEquals("Human", lrHuman.getValue());
		assertNull(lrHuman.getDate());
		assertNull(lrHuman.getUnit());
		assertNull(lrHuman.getOBRDateTime());
		
		assertEquals("poctGate", observationMsg.getSendingApplication());
		assertEquals("poctGate", observationMsg.getSendingFacility());
		
	}
	
	@Test
	public void testReadObservationOfv271test2File() throws ElexisException{
		File tst2File = loadSpecificv271File("v271test2.hl7");
		assertNotNull(tst2File);
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(tst2File);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		Date msgDate = observationMsg.getDateTimeOfMessage();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss");
		assertEquals("17.11.2015-02:22:32", sdf.format(msgDate));
		
		List<IValueType> observations = observationMsg.getObservations();
		assertEquals(9, observations.size());
		
		for (IValueType iValueType : observations) {
			assertTrue(iValueType instanceof LabResultData);
		}
		
		LabResultData lrNTpro = (LabResultData) observations.get(0);
		assertEquals("NTpro", lrNTpro.getName());
		assertEquals("111", lrNTpro.getValue());
		assertEquals("pg/mL", lrNTpro.getUnit());
		assertNull(lrNTpro.getDate());
		assertNull(lrNTpro.getOBRDateTime());
		
		LabResultData lrOperator = (LabResultData) observations.get(3);
		assertEquals("Operator", lrOperator.getName());
		assertEquals("9999999999", lrOperator.getValue());
		assertNull(lrOperator.getUnit());
		assertNull(lrOperator.getDate());
		assertNull(lrOperator.getOBRDateTime());
		
		LabResultData lrReportType = (LabResultData) observations.get(7);
		assertEquals("ReportType", lrReportType.getName());
		assertEquals("Q", lrReportType.getValue());
		assertNull(lrReportType.getUnit());
		assertNull(lrReportType.getDate());
		assertNull(lrReportType.getOBRDateTime());
		
		assertNull(observationMsg.getSendingApplication());
		assertEquals("poctGate", observationMsg.getSendingFacility());
	}
	
	@Test
	public void testReadObservationOfv271test6File() throws ElexisException{
		File tst6File = loadSpecificv271File("v271test6.hl7");
		assertNotNull(tst6File);
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(tst6File);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		Date msgDate = observationMsg.getDateTimeOfMessage();
		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss");
		assertEquals("17.11.2015-02:22:33", sdf.format(msgDate));
		
		List<IValueType> observations = observationMsg.getObservations();
		assertEquals(9, observations.size());
		
		for (IValueType iValueType : observations) {
			assertTrue(iValueType instanceof LabResultData);
		}
		
		LabResultData lrTNI = (LabResultData) observations.get(0);
		assertEquals("TNI", lrTNI.getName());
		assertEquals("< 0.01", lrTNI.getValue());
		assertEquals("ng/mL", lrTNI.getUnit());
		assertNull(lrTNI.getDate());
		assertNull(lrTNI.getOBRDateTime());
		
		LabResultData lrInstructSpecId = (LabResultData) observations.get(4);
		assertEquals("Instructor Specimen Id", lrInstructSpecId.getName());
		assertEquals("00073951^00072", lrInstructSpecId.getValue());
		assertNull(lrInstructSpecId.getUnit());
		assertNull(lrInstructSpecId.getDate());
		assertNull(lrInstructSpecId.getOBRDateTime());
		
		LabResultData lrDevSerialNr = (LabResultData) observations.get(8);
		assertEquals("Device Serial Number", lrDevSerialNr.getName());
		assertEquals("00073951", lrDevSerialNr.getValue());
		assertNull(lrDevSerialNr.getUnit());
		assertNull(lrDevSerialNr.getDate());
		assertNull(lrDevSerialNr.getOBRDateTime());
		
		assertEquals("poctGate", observationMsg.getSendingApplication());
		assertNull(observationMsg.getSendingFacility());
	}
	
	private File[] loadv271Files(){
		File directory =
			new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"), "rsc/v271");
			
		if (directory.exists() && directory.isDirectory()) {
			return directory.listFiles();
		}
		return new File[] {};
		
	}
	
	private File loadSpecificv271File(String name){
		File directory =
			new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"), "rsc/v271");
			
		if (directory.exists() && directory.isDirectory()) {
			for (File f : directory.listFiles()) {
				if (f.getName().equals(name)) {
					return f;
				}
			}
		}
		return null;
	}
}
