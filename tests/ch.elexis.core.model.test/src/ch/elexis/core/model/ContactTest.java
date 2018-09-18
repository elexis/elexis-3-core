package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.Country;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;

public class ContactTest {
	
	private IModelService modelService;
	private IPerson person;
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
		
		LocalDate dob = LocalDate.of(2016, 9, 1);
		person = new IContactBuilder.PersonBuilder(modelService, "", "", dob, Gender.MALE)
			.buildAndSave();
	}
	
	@After
	public void after(){
		modelService.delete(person);
	}
	
	@Test
	public void createAndRemoveIContact() throws InstantiationException, IllegalAccessException{
		assertNotNull(person.getId());
		assertTrue(person.isPerson());
		assertFalse(person.isPatient());
		assertFalse(person.isMandator());
		assertFalse(person.isOrganization());
		assertFalse(person.isLaboratory());
		
		IPerson findById = modelService.load(person.getId(), IPerson.class).get();
		assertEquals(person.getId(), findById.getId());
		assertEquals(LocalDate.of(2016, 9, 1), person.getDateOfBirth().toLocalDate());
		int ageInYears = findById.getAgeInYears();
		assertTrue(ageInYears >= 2);
	}
	
	@Test
	public void createRemoveAddress(){
		IAddress nursingHome = modelService.create(IAddress.class);
		nursingHome.setType(AddressType.NURSING_HOME);
		nursingHome.setStreet2("Street2");
		nursingHome.setZip("6840");
		nursingHome.setCountry(Country.AT);
		person.addAddress(nursingHome);
		modelService.save(Arrays.asList(nursingHome, person));
		
		assertTrue(person.getAddress().contains(nursingHome));
		modelService.delete(nursingHome);
		assertFalse(person.getAddress().contains(nursingHome));
	}
	
}
