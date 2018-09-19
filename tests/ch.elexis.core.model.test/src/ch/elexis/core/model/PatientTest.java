package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PatientTest extends AbstractTest {
	
	@Before
	public void before(){
		super.before();
		super.createPatient();
	}
	
	@After
	public void after(){
		super.removePatient();
		super.after();
	}
	
	@Test
	public void createDeletePatient(){
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
	
}
