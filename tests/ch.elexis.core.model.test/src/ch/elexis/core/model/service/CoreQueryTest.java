package ch.elexis.core.model.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class CoreQueryTest {
	private IModelService modelSerice;
	
	@Before
	public void before(){
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
		clearContacts();
	}
	
	@After
	public void after(){
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	@Test
	public void queryExecute(){
		IQuery<IContact> query = modelSerice.getQuery(IContact.class);
		assertNotNull(query);
		List<IContact> results = query.execute();
		assertNotNull(results);
		assertTrue(results.isEmpty());
		// query is closed after execute, calling again must throw exception
		boolean exception = false;
		try  {
			query.execute();
		} catch (IllegalStateException e) {
			exception = true;
		}
		assertTrue(exception);
	}
	
	@Test
	public void queryContact(){
		createContact("test1", "test1");
		createContact("test2", "test2");
		
		IQuery<IContact> query = modelSerice.getQuery(IContact.class);
		assertNotNull(query);
		List<IContact> results = query.execute();
		assertNotNull(results);
		assertEquals(2, results.size());
	}
	
	@Test
	public void queryContactDescription(){
		createContact("test1", "test1");
		createContact("test2", "test2");
		
		IQuery<IContact> query = modelSerice.getQuery(IContact.class);
		assertNotNull(query);
		query.add(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.EQUALS, "test1");
		List<IContact> results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("test1", results.get(0).getDescription1());
		
		query = modelSerice.getQuery(IContact.class);
		query.add(ModelPackage.Literals.ICONTACT__DESCRIPTION3, COMPARATOR.EQUALS, (String) null);
		results = query.execute();
		assertNotNull(results);
		assertEquals(2, results.size());
	}
	
	@Test
	public void queryPatient(){
		createContact("test1", "test1");
		createContact("test2", "test2");
		createPatient("patient1", "patient1", LocalDate.of(1999, 1, 1));
		createPatient("patient2", "patient2", LocalDate.of(1999, 2, 2));
		
		IQuery<IPatient> query = modelSerice.getQuery(IPatient.class);
		assertNotNull(query);
		List<IPatient> results = query.execute();
		assertNotNull(results);
		assertEquals(2, results.size());
	}
	
	@Test
	public void queryPatientNameAndDate(){
		createContact("test1", "test1");
		createContact("test2", "test2");
		createPatient("patient1", "patient1", LocalDate.of(1999, 1, 1));
		createPatient("patient2", "patient2", LocalDate.of(1999, 2, 2));
		createPatient("patient2", "patient2", LocalDate.of(1999, 12, 12));
		
		IQuery<IPatient> query = modelSerice.getQuery(IPatient.class);
		assertNotNull(query);
		query.add(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.EQUALS, "patient1");
		List<IPatient> results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("patient1", results.get(0).getFirstName());
		
		query = modelSerice.getQuery(IPatient.class);
		assertNotNull(query);
		query.add(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
			LocalDate.of(1999, 1, 1));
		results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(LocalDate.of(1999, 1, 1), results.get(0).getDateOfBirth().toLocalDate());
		
		query = modelSerice.getQuery(IPatient.class);
		assertNotNull(query);
		query.add(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.EQUALS, "patient2");
		query.add(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
			LocalDate.of(1999, 2, 2));
		results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("patient2", results.get(0).getFirstName());
		assertEquals(LocalDate.of(1999, 2, 2), results.get(0).getDateOfBirth().toLocalDate());
		
		query = modelSerice.getQuery(IPatient.class);
		assertNotNull(query);
		query.add(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.GREATER_OR_EQUAL,
			LocalDate.of(1999, 2, 2));
		query.add(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.LESS,
			LocalDate.of(1999, 12, 12));
		results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("patient2", results.get(0).getFirstName());
		assertEquals(LocalDate.of(1999, 2, 2), results.get(0).getDateOfBirth().toLocalDate());
		
	}
	
	private void clearContacts(){
		IQuery<IContact> query = modelSerice.getQuery(IContact.class);
		List<IContact> results = query.execute();
		results.parallelStream().forEach(c -> modelSerice.remove(c));
	}
	
	private void createContact(String desc1, String desc2){
		IContact contact = modelSerice.create(IContact.class);
		assertNotNull(contact);
		assertTrue(contact instanceof IContact);
		
		contact.setDescription1(desc1);
		contact.setDescription2(desc2);
		assertTrue(modelSerice.save(contact));
	}
	
	private void createPatient(String firstName, String lastName, LocalDate birthDate){
		IPatient patient = modelSerice.create(IPatient.class);
		assertNotNull(patient);
		assertTrue(patient instanceof IPatient);
		
		patient.setPatient(true);
		patient.setLastName(lastName);
		patient.setFirstName(firstName);
		patient.setDateOfBirth(birthDate.atStartOfDay());
		assertTrue(modelSerice.save(patient));
	}
}
