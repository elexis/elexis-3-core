package ch.elexis.core.model.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.utils.OsgiServiceUtil;

public class CoreModelServiceTest {
	
	private IModelService modelSerice;
	
	@Before
	public void before() {
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
	}
	
	@After
	public void after() {
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	
	@Test
	public void createContact(){
		IContact contact = modelSerice.create(IContact.class);
		assertNotNull(contact);
		assertTrue(contact instanceof IContact);
		
		contact.setDescription1("test description 1");
		contact.setDescription2("test description 2");
		assertTrue(modelSerice.save(contact));
		
		Optional<IContact> loadedContact = modelSerice.load(contact.getId(), IContact.class);
		assertTrue(loadedContact.isPresent());
		assertFalse(contact == loadedContact.get());
		assertEquals(contact, loadedContact.get());
		assertEquals(contact.getDescription1(), loadedContact.get().getDescription1());
	}
	
	@Test
	public void contactPreconditions(){
		IContact contact = modelSerice.create(IContact.class);
		assertNotNull(contact);
		assertTrue(contact instanceof IContact);
		assertTrue(modelSerice.save(contact));
		
		Optional<IContact> loadedContact = modelSerice.load(contact.getId(), IContact.class);
		assertTrue(loadedContact.isPresent());
		Optional<IPerson> loadedPerson = modelSerice.load(contact.getId(), IPerson.class);
		assertFalse(loadedPerson.isPresent());
		Optional<IOrganization> loadedOrganization =
			modelSerice.load(contact.getId(), IOrganization.class);
		assertFalse(loadedOrganization.isPresent());
	}
	
	@Test
	public void createPatient(){
		IPatient patient = modelSerice.create(IPatient.class);
		assertNotNull(patient);
		assertTrue(patient instanceof IPatient);
		
		patient.setPatient(true);
		patient.setLastName("test lastname");
		patient.setFirstName("test first name");
		assertTrue(modelSerice.save(patient));
		
		Optional<IPatient> loadedPatient = modelSerice.load(patient.getId(), IPatient.class);
		assertTrue(loadedPatient.isPresent());
		assertFalse(patient == loadedPatient.get());
		assertEquals(patient, loadedPatient.get());
	}
	
	@Test
	public void createPerson(){
		IPerson person = modelSerice.create(IPerson.class);
		assertNotNull(person);
		assertTrue(person instanceof IPerson);
		
		person.setPerson(true);
		person.setLastName("test lastname");
		person.setFirstName("test first name");
		assertTrue(modelSerice.save(person));
		
		Optional<IPerson> loadedPerson = modelSerice.load(person.getId(), IPerson.class);
		assertTrue(loadedPerson.isPresent());
		assertFalse(person == loadedPerson.get());
		assertEquals(person, loadedPerson.get());
	}
}
