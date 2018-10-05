package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.prescription.EntryType;

public class PrescriptionTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		createPatient();
		createLocalArticle();
	}
	
	@After
	public void after(){
		removePatient();
		removeLocalArticle();
		super.after();
	}
	
	@Test
	public void createFindDelete(){
		IPrescription prescription =
			new IPrescriptionBuilder(modelService, localArticle, patient, "1-0-0-1").buildAndSave();
		assertEquals(patient, prescription.getPatient());
		assertEquals(localArticle, prescription.getArticle());
		assertEquals("1-0-0-1", prescription.getDosageInstruction());
		assertNotNull(prescription.getDateFrom());
		assertEquals(EntryType.FIXED_MEDICATION, prescription.getEntryType());
		
		List<IPrescription> prescriptions = modelService.getQuery(IPrescription.class).execute();
		assertEquals(prescription, prescriptions.get(0));
		
		modelService.remove(prescription);
	}
}
