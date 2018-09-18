package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class PatientTest {
	
	private IModelService modelService;
	
	IPatient patient;
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
		
		LocalDate dob = LocalDate.of(2016, 9, 1);
		patient =
			(IPatient) new IContactBuilder.PatientBuilder(modelService, "", "", dob, Gender.MALE)
				.buildAndSave();
	}
	
	@After
	public void after(){
		modelService.delete(patient);
	}
	
	@Test
	public void createPatient(){
		IPatient patient = new IContactBuilder.PatientBuilder(modelService, "Vorname", "Nachname",
			LocalDate.now(), Gender.FEMALE).buildAndSave();
		patient.setExtInfo(PatientConstants.FLD_EXTINFO_BIRTHNAME, "Birthname");
		modelService.save(patient);
		assertTrue(patient.isPatient());
		assertTrue(patient.isPerson());
		assertFalse(patient.isMandator());
		assertFalse(patient.isOrganization());
		assertFalse(patient.isLaboratory());
		
		String id = patient.getId();
		assertNotNull(id);
		assertNotNull(patient.getCode());
		IContact findById = modelService.load(id, IContact.class).get();
		assertNotNull(findById);
		assertEquals("Birthname", findById.getExtInfo(PatientConstants.FLD_EXTINFO_BIRTHNAME));
		modelService.delete(patient);
	}
	
	@Test
	public void modifyFindCoverages(){
		ICoverage coverage = new ICoverageBuilder.Builder(modelService, patient, "testCoverage",
			"testReason", "testBillingSystem").buildAndSave();	
		assertTrue(patient.getCoverages().contains(coverage));
		
		IQuery<ICoverage> query = modelService.getQuery(ICoverage.class);
		query.and(ModelPackage.Literals.ICOVERAGE__PATIENT, COMPARATOR.EQUALS, patient);
		assertEquals(coverage, query.executeSingleResult().get());
		
		modelService.delete(coverage);
		assertFalse(patient.getCoverages().contains(coverage));
		
	}
	
}
