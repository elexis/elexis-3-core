package ch.elexis.core.hl7.v2x;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import ch.elexis.core.data.util.PlatformHelper;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.data.Patient;
import ch.elexis.hl7.HL7Reader;
import ch.elexis.hl7.HL7ReaderFactory;
import ch.elexis.hl7.model.IValueType;
import ch.elexis.hl7.model.LabResultData;
import ch.elexis.hl7.model.LabResultData.LabResultStatus;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.v2x.HL7ReaderV25;
import ch.elexis.hl7.v2x.HL7ReaderV251;

public class Test_SpecificImportFiles {
	
	private static DummyPatientResolver resolver;
	
	@BeforeClass
	public static void beforeClass(){
		Patient dummyPatient = new Patient("SpecificImport", "Test", "02.02.12", Patient.FEMALE);
		resolver = new DummyPatientResolver(dummyPatient);
	}
	
	@Test
	public void test_V251_OUL_R22_11057() throws IOException, ElexisException{
		File importFile = new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
			"rsc/LabCube/5083_LabCube_DriChem7000_20180314131140_288107.hl7");
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(importFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals(HL7ReaderV251.class, reader.getClass());
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		List<IValueType> observations = observationMsg.getObservations();
		System.out.println("Observations [" + observations.size() + "]");
		assertEquals(8, observations.size());
		
		LabResultData lrd = (LabResultData) observations.get(3);
		assertEquals("GPT-P", lrd.getName());
		assertEquals("GPT-P", lrd.getCode());
		assertEquals("<10", lrd.getValue());
		assertEquals("LL", lrd.getRawAbnormalFlag());
		assertEquals("4-44", lrd.getRange());
		assertTrue(lrd.getFlag());
		assertTrue(lrd.isNumeric());
		assertFalse(lrd.isPlainText());
		assertEquals("U/l", lrd.getUnit());
		assertEquals("", lrd.getComment());
		assertEquals("", lrd.getGroup());
		assertEquals(LabResultStatus.FINAL, lrd.getResultStatus());
	}
	
	/**
	 * @throws IOException 
	 * @throws ElexisException 
	 * @see http://hl7-definition.caristix.com:9010/Default.aspx?version=HL7+v2.5&segment=OBX
	 */
	@Test
	public void test_V25_ORU_R01_PatientNotesAndComments_11154() throws IOException, ElexisException {
		File importFile = new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
				"rsc/Analytica/Spermiogramm.hl7");
			
			List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(importFile);
			assertNotNull(hl7Readers);
			assertEquals(1, hl7Readers.size());
			HL7Reader reader = hl7Readers.get(0);
			assertEquals(HL7ReaderV25.class, reader.getClass());
			
			ObservationMessage observationMsg = reader.readObservation(resolver, false);
			List<IValueType> observations = observationMsg.getObservations();
			assertEquals(2, observations.size());
			
			assertTrue(observationMsg.getPatientNotesAndComments().startsWith("Untersuchungsdatum und Zeit"));

			LabResultData lrd = (LabResultData) observations.get(1);
			assertEquals("Spermien: nachweisbar", lrd.getValue());
			assertEquals("negativ", lrd.getRange());
			assertEquals("SPERN", lrd.getCode());			
	}
	
}
