package ch.elexis.core.model.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class CoreModelServiceTest {
	
	private IModelService modelSerice;
	
	@Before
	public void before() {
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
	}
	
	@After
	public void after(){
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
		
		person.setLastName("test lastname");
		person.setFirstName("test first name");
		assertTrue(modelSerice.save(person));
		
		Optional<IPerson> loadedPerson = modelSerice.load(person.getId(), IPerson.class);
		assertTrue(loadedPerson.isPresent());
		assertFalse(person == loadedPerson.get());
		assertEquals(person, loadedPerson.get());
	}
	
	private String multiThreadPersonId;
	
	@Test
	public void multiThreadSequential() throws InterruptedException{
		ExecutorService service = Executors.newSingleThreadExecutor();
		// create a person
		service.execute(new Runnable() {
			@Override
			public void run(){
				IPerson person = modelSerice.create(IPerson.class);
				person.setLastName("test lastname");
				person.setFirstName("test first name");
				assertTrue(modelSerice.save(person));
				multiThreadPersonId = person.getId();
			}
		});
		// load the person
		service.execute(new Runnable() {
			@Override
			public void run(){
				Optional<IPerson> loadedPerson =
					modelSerice.load(multiThreadPersonId, IPerson.class);
				assertTrue(loadedPerson.isPresent());
			}
		});
		// query the person
		service.execute(new Runnable() {
			@Override
			public void run(){
				IQuery<IPerson> query = modelSerice.getQuery(IPerson.class);
				query.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE, "%lastname");
				List<IPerson> results = query.execute();
				assertEquals(multiThreadPersonId, results.get(0).getId());
			}
		});
		// wait for executor to terminate
		service.shutdown();
		service.awaitTermination(5, TimeUnit.SECONDS);
		// remove the person
		modelSerice.remove(modelSerice.load(multiThreadPersonId, IPerson.class).get());
	}
	
	@Test
	public void multiThreadConcurrent() throws InterruptedException{
		ExecutorService service = Executors.newCachedThreadPool();
		for (int i = 0; i < 1000; i++) {
			service.execute(new CreatePerson(i));
		}
		// wait for executor to terminate
		service.shutdown();
		service.awaitTermination(5, TimeUnit.SECONDS);
		service = Executors.newCachedThreadPool();
		for (int i = 0; i < 1000; i++) {
			service.execute(new QueryPerson(i));
		}
		// wait for executor to terminate
		service.shutdown();
		service.awaitTermination(5, TimeUnit.SECONDS);
		service = Executors.newCachedThreadPool();
		for (int i = 0; i < 1000; i++) {
			service.execute(new RemovePerson(i));
		}
		// wait for executor to terminate
		service.shutdown();
		service.awaitTermination(5, TimeUnit.SECONDS);
		
		IQuery<IPerson> query = modelSerice.getQuery(IPerson.class);
		query.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE, "%lastname %");
		List<IPerson> results = query.execute();
		assertTrue(results.isEmpty());
	}
	
	private class CreatePerson implements Runnable {
		private int index;
		
		public CreatePerson(int index){
			this.index = index;
		}
		
		@Override
		public void run(){
			IPerson person = modelSerice.create(IPerson.class);
			person.setLastName("test lastname " + index);
			person.setFirstName("test first name " + index);
			assertTrue(modelSerice.save(person));
		}
	}
	
	private class QueryPerson implements Runnable {
		private int index;
		
		public QueryPerson(int index){
			this.index = index;
		}
		
		@Override
		public void run(){
			IQuery<IPerson> query = modelSerice.getQuery(IPerson.class);
			query.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE,
				"%lastname " + index);
			List<IPerson> results = query.execute();
			assertEquals(1, results.size());
		}
	}
	
	private class RemovePerson implements Runnable {
		private int index;
		
		public RemovePerson(int index){
			this.index = index;
		}
		
		@Override
		public void run(){
			IQuery<IPerson> query = modelSerice.getQuery(IPerson.class);
			query.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE,
				"%lastname " + index);
			List<IPerson> results = query.execute();
			assertEquals(1, results.size());
			modelSerice.remove(results.get(0));
		}
	}
}
