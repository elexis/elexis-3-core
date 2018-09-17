package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ContactTest {
	
	private IModelService modelService;
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
	}
	
	@Test
	public void testCreateAndRemoveIContact() throws InstantiationException, IllegalAccessException{
		LocalDate dob = LocalDate.of(2016, 9, 1);
		IPerson val = new IContactBuilder.PersonBuilder(modelService, "", "", dob, Gender.MALE)
			.buildAndSave();
		assertNotNull(val.getId());
		
		IPerson findById = modelService.load(val.getId(), IPerson.class).get();
		assertEquals(val.getId(), findById.getId());
		assertEquals(dob, val.getDateOfBirth().toLocalDate());
		int ageInYears = findById.getAgeInYears();
		assertTrue(ageInYears >= 2);
		modelService.delete(findById);
		Optional<IPerson> found = modelService.load(val.getId(), IPerson.class);
		assertFalse(found.isPresent());
		
	}
	
	@Test
	public void testCreatePatient(){
		IPerson patient = new IContactBuilder.PersonBuilder(modelService, "Vorname", "Nachname",
			LocalDate.now(), Gender.FEMALE).patient().buildAndSave();
		patient.setExtInfo(PatientConstants.FLD_EXTINFO_BIRTHNAME, "Birthname");
		modelService.save(patient);
		String id = patient.getId();
		assertNotNull(id);
		assertNotNull(patient.getCode());
		IContact findById = modelService.load(id, IContact.class).get();
		assertNotNull(findById);
		assertEquals("Birthname", findById.getExtInfo(PatientConstants.FLD_EXTINFO_BIRTHNAME));
		modelService.delete(patient);
	}
	
}
