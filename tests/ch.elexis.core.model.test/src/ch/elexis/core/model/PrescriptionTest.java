package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.test.AbstractTest;

public class PrescriptionTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		createPatient();
		createLocalArticle();
	}
	
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void createFindDelete(){
		IPrescription prescription =
			new IPrescriptionBuilder(coreModelService, null, localArticle, patient, "1-0-0-1")
				.buildAndSave();
		assertEquals(patient, prescription.getPatient());
		assertEquals(localArticle, prescription.getArticle());
		assertEquals("1-0-0-1", prescription.getDosageInstruction());
		assertNotNull(prescription.getDateFrom());
		assertEquals(EntryType.FIXED_MEDICATION, prescription.getEntryType());
		
		List<IPrescription> prescriptions = coreModelService.getQuery(IPrescription.class).execute();
		assertEquals(prescription, prescriptions.get(0));
		
		coreModelService.remove(prescription);
	}
	
	@Test
	public void patientGetMedication(){
		IPrescription prescription =
			new IPrescriptionBuilder(coreModelService, null, localArticle, patient, "1-0-0-1")
				.buildAndSave();
		assertEquals(patient, prescription.getPatient());
		assertEquals(localArticle, prescription.getArticle());
		assertEquals("1-0-0-1", prescription.getDosageInstruction());
		assertNotNull(prescription.getDateFrom());
		assertEquals(EntryType.FIXED_MEDICATION, prescription.getEntryType());
		
		List<IPrescription> medication =
			patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION));
		assertNotNull(medication);
		assertFalse(medication.isEmpty());
		
		// test other EntryType
		medication = patient.getMedication(Arrays.asList(EntryType.SYMPTOMATIC_MEDICATION));
		assertNotNull(medication);
		assertTrue(medication.isEmpty());
		
		// test stop
		prescription.setDateTo(LocalDateTime.now().minusSeconds(1));
		CoreModelServiceHolder.get().save(prescription);
		medication =
			patient.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION));
		assertNotNull(medication);
		assertTrue(medication.isEmpty());
		
		coreModelService.remove(prescription);
	}
}
