package ch.elexis.core.hl7.v2x;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
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
import ch.elexis.hl7.model.LabResultData.LabResultStatus;
import ch.elexis.hl7.model.ObservationMessage;
import ch.elexis.hl7.v2x.HL7ReaderV25;
import ch.elexis.hl7.v2x.HL7ReaderV251;
import ch.elexis.hl7.v2x.HL7ReaderV26;

public class Test_SpecificImportFiles {
	
	private static DummyPatientResolver resolver;
	
	@BeforeClass
	public static void beforeClass(){
		IPatient dummyPatient = new IContactBuilder.PatientBuilder(CoreModelServiceHolder.get(),
			"Test", "SpecificImport", LocalDate.of(2012, 2, 2), Gender.FEMALE).buildAndSave();
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
	
	@Test
	public void test_V251_OUL_R22_17300() throws IOException, ElexisException{
		File importFile = new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
			"rsc/LabCube/9885_LabCube_CelltacMEK6500_20191128093117_034358.hl7");
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(importFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals(HL7ReaderV251.class, reader.getClass());
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		List<IValueType> observations = observationMsg.getObservations();
		System.out.println("Observations [" + observations.size() + "]");
		assertEquals(18, observations.size());
		
		LabResultData lrd = (LabResultData) observations.get(3);
		assertEquals("Granulozyten", lrd.getName());
		assertEquals("GR%", lrd.getCode());
		assertEquals("60.4", lrd.getValue());
		assertEquals("40-74", lrd.getRange());
		assertTrue(lrd.isNumeric());
		assertFalse(lrd.isPlainText());
		assertEquals("%", lrd.getUnit());
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
	
	@Test
	public void test_V26_OUL_R24_Afias6() throws IOException, ElexisException{
		File importFile = new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
			"rsc/v26/afias6.hl7");
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(importFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals(HL7ReaderV26.class, reader.getClass());
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		List<IValueType> observations = observationMsg.getObservations();
		System.out.println("Observations [" + observations.size() + "]");
		assertEquals(1, observations.size());
		
		// OBX|1|TX|CRP||68.93|mg/L|||||R
		LabResultData lrd = (LabResultData) observations.get(0);
		assertEquals("CRP", lrd.getName());
		assertEquals("CRP", lrd.getCode());
		assertEquals("68.93", lrd.getValue());
		assertTrue(lrd.getRawAbnormalFlag() == null);
		assertTrue(lrd.getRange() == null);
		assertTrue(lrd.getFlag() == null);
		assertFalse(lrd.isNumeric());
		assertTrue(lrd.isPlainText());
		assertEquals("mg/L", lrd.getUnit());
		assertEquals("", lrd.getComment());
		assertEquals("", lrd.getGroup());
		assertEquals(LabResultStatus.UNDEFINED, lrd.getResultStatus());
	}
}
