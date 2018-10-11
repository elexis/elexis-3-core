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

import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.AddressType;
import ch.elexis.core.types.Country;

public class ContactTest extends AbstractTest {
		
	@Before
	public void before(){
		super.before();
		createPerson();
	}
	
	@After
	public void after(){
		super.after();
	}
	
	@Test
	public void createAndRemoveIContact() throws InstantiationException, IllegalAccessException{
		assertNotNull(person.getId());
		assertTrue(person.isPerson());
		assertFalse(person.isPatient());
		assertFalse(person.isMandator());
		assertFalse(person.isOrganization());
		assertFalse(person.isLaboratory());
		
		IPerson findById = coreModelService.load(person.getId(), IPerson.class).get();
		assertEquals(person.getId(), findById.getId());
		assertEquals(LocalDate.of(2016, 9, 1), person.getDateOfBirth().toLocalDate());
		int ageInYears = findById.getAgeInYears();
		assertTrue(ageInYears >= 2);
	}
	
	@Test
	public void createRemoveAddress(){
		IAddress nursingHome = coreModelService.create(IAddress.class);
		nursingHome.setType(AddressType.NURSING_HOME);
		nursingHome.setStreet2("Street2");
		nursingHome.setZip("6840");
		nursingHome.setCountry(Country.AT);
		person.addAddress(nursingHome);
		coreModelService.save(Arrays.asList(nursingHome, person));
		
		assertTrue(person.getAddress().contains(nursingHome));
		coreModelService.delete(nursingHome);
		assertFalse(person.getAddress().contains(nursingHome));
	}
	
}
