package ch.elexis.core.hl7.v2x;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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

public class Test_HL7_v25_Imports {
	
	private static DummyPatientResolver resolver;
	
	@BeforeClass
	public static void setUpBeforeClass() throws Exception{
		Patient dummyPatient = new Patient("Pickle", "Rick", "19.06.1969", Patient.MALE);
		resolver = new DummyPatientResolver(dummyPatient);
	}
	
	@Test
	public void testORU_R01_10655() throws IOException, ElexisException{
		File importFile = new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
			"rsc/v25/0218040634_6467538389964.hl7");
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(importFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals(HL7ReaderV25.class, reader.getClass());
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		List<IValueType> observations = observationMsg.getObservations();
		System.out.println("Observations [" + observations.size() + "]");
		assertEquals(5, observations.size());
		
		LabResultData lrd = (LabResultData) observations.get(1);
		assertEquals("VAGINA-ABSTRICH - Grampräparat", lrd.getName());
		assertEquals("GRAM", lrd.getCode());
		assertTrue(lrd.getValue().startsWith("Leukozyten"));
		assertNull(lrd.getFlag());
		assertNull(lrd.getComment());
		assertNull(lrd.getDate());
		assertNull(lrd.getRange());
		assertNull(lrd.getUnit());
		assertEquals("", lrd.getGroup());
		assertEquals(LabResultStatus.FINAL, lrd.getResultStatus());
		
		lrd = (LabResultData) observations.get(2);
		assertEquals("VAGINA-ABSTRICH - Kultur aerob", lrd.getName());
		assertEquals("KULA", lrd.getCode());
		assertTrue(lrd.getValue().startsWith("Candida albicans"));
		assertNull(lrd.getFlag());
		assertNull(lrd.getComment());
		assertNull(lrd.getRange());
		assertNull(lrd.getUnit());
		assertEquals(LabResultStatus.FINAL, lrd.getResultStatus());
		
		lrd = (LabResultData) observations.get(4);
		assertEquals("VAGINA-ABSTRICH - Chlamydia trachomatis PCR", lrd.getName());
		assertEquals("CHLATP", lrd.getCode());
		assertTrue(lrd.getValue().equals("positiv"));
		assertNull(lrd.getFlag());
		assertNull(lrd.getComment());
		assertNull(lrd.getRange());
		assertNull(lrd.getUnit());
		assertEquals(LabResultStatus.FINAL, lrd.getResultStatus());
	}
	
	@Test
	public void testORU_R01_10655_2() throws IOException, ElexisException{
		File importFile = new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
			"rsc/v25/0218040388_6467233598199.hl7");
		
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(importFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals(HL7ReaderV25.class, reader.getClass());
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		List<IValueType> observations = observationMsg.getObservations();
		System.out.println("Observations [" + observations.size() + "]");
		assertEquals(2, observations.size());
		
		LabResultData lrd = (LabResultData) observations.get(1);
		assertEquals("STUHL - Helicobacter pylori  (Ag-Nachweis)", lrd.getName());
		assertEquals("HELP", lrd.getCode());
		assertTrue(lrd.getValue().equals("positiv"));
		assertNull(lrd.getFlag());
		assertNull(lrd.getComment());
		assertNull(lrd.getRange());
		assertNull(lrd.getUnit());
		assertEquals(LabResultStatus.FINAL, lrd.getResultStatus());
	}
	
	@Test
	public void testLabItemName_11507() throws IOException, ElexisException{
		File importFile = new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
			"rsc/Analytica/HBA1.hl7");
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(importFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals(HL7ReaderV25.class, reader.getClass());
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		List<IValueType> observations = observationMsg.getObservations();
		assertEquals(1, observations.size());
		
		LabResultData lrd = (LabResultData) observations.get(0);
		assertEquals("HbA1c", lrd.getName());
		assertEquals("HBA1", lrd.getCode());
		assertTrue(lrd.getValue().equals("5.1"));
		assertNull(lrd.getFlag());
		assertTrue(lrd.getComment().startsWith("Bemerkung zu HbA1c"));
		assertEquals("< 5.7", lrd.getRange());
		assertEquals("%", lrd.getUnit());
		assertEquals(LabResultStatus.FINAL, lrd.getResultStatus());
	}
	
	@Test
	public void testLabItemName_11507_2() throws IOException, ElexisException{
		File importFile = new File(PlatformHelper.getBasePath("ch.elexis.core.hl7.v2x.tests"),
			"rsc/Analytica/Ferritin.hl7");
		List<HL7Reader> hl7Readers = HL7ReaderFactory.INSTANCE.getReader(importFile);
		assertNotNull(hl7Readers);
		assertEquals(1, hl7Readers.size());
		HL7Reader reader = hl7Readers.get(0);
		assertEquals(HL7ReaderV25.class, reader.getClass());
		
		ObservationMessage observationMsg = reader.readObservation(resolver, false);
		List<IValueType> observations = observationMsg.getObservations();
		assertEquals(3, observations.size());
		
		boolean found = false;
		for (IValueType iValueType : observations) {
			LabResultData lrd = (LabResultData) iValueType;
			if ("Ferritin".equals(lrd.getName())) {
				assertEquals("FERR", lrd.getCode());
				assertTrue(lrd.getValue().equals("66"));
				assertNull(lrd.getFlag());
				assertNull(lrd.getComment());
				assertEquals("ug/l", lrd.getUnit());
				assertEquals("22 - 322", lrd.getRange());
				assertEquals(LabResultStatus.FINAL, lrd.getResultStatus());
				found = true;
			}
		}
		assertTrue(found);
		
	}
}
