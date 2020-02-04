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

import javax.persistence.EntityManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IOrganization;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRelatedContact;
import ch.elexis.core.model.IUserConfig;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IElexisEntityManager;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.utils.OsgiServiceUtil;

public class CoreModelServiceTest {
	
	private IModelService modelService;
	
	@Before
	public void before() {
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
		clearContacts();
	}
	
	@After
	public void after(){
		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}

	private void clearContacts(){
		IQuery<IContact> query = modelService.getQuery(IContact.class, true);
		List<IContact> results = query.execute();
		results.stream().forEach(c -> modelService.remove(c));
	}
	
	@Test
	public void createContact(){
		IContact contact = modelService.create(IContact.class);
		assertNotNull(contact);
		assertTrue(contact instanceof IContact);
		
		contact.setDescription1("test description 1");
		contact.setDescription2("test description 2");
		assertTrue(modelService.save(contact));
		
		Optional<IContact> loadedContact = modelService.load(contact.getId(), IContact.class);
		assertTrue(loadedContact.isPresent());
		assertFalse(contact == loadedContact.get());
		assertEquals(contact, loadedContact.get());
		assertEquals(contact.getDescription1(), loadedContact.get().getDescription1());
	}
	
	@Test
	public void contactPreconditions(){
		IContact contact = modelService.create(IContact.class);
		assertNotNull(contact);
		assertTrue(contact instanceof IContact);
		assertTrue(modelService.save(contact));
		
		Optional<IContact> loadedContact = modelService.load(contact.getId(), IContact.class);
		assertTrue(loadedContact.isPresent());
		Optional<IPerson> loadedPerson = modelService.load(contact.getId(), IPerson.class);
		assertFalse(loadedPerson.isPresent());
		Optional<IOrganization> loadedOrganization =
			modelService.load(contact.getId(), IOrganization.class);
		assertFalse(loadedOrganization.isPresent());
	}
	
	@Test
	public void createPatient(){
		IPatient patient = modelService.create(IPatient.class);
		assertNotNull(patient);
		assertTrue(patient instanceof IPatient);
		
		patient.setLastName("test lastname");
		patient.setFirstName("test first name");
		assertTrue(modelService.save(patient));
		
		Optional<IPatient> loadedPatient = modelService.load(patient.getId(), IPatient.class);
		assertTrue(loadedPatient.isPresent());
		assertFalse(patient == loadedPatient.get());
		assertEquals(patient, loadedPatient.get());
		assertTrue(patient.isPerson());
		assertTrue(patient.isPatient());
	}
	
	@Test
	public void createPerson(){
		IPerson person = modelService.create(IPerson.class);
		assertNotNull(person);
		assertTrue(person instanceof IPerson);
		
		person.setLastName("test lastname");
		person.setFirstName("test first name");
		assertTrue(modelService.save(person));
		
		Optional<IPerson> loadedPerson = modelService.load(person.getId(), IPerson.class);
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
				IPerson person = modelService.create(IPerson.class);
				person.setLastName("test lastname");
				person.setFirstName("test first name");
				assertTrue(modelService.save(person));
				multiThreadPersonId = person.getId();
			}
		});
		// load the person
		service.execute(new Runnable() {
			@Override
			public void run(){
				Optional<IPerson> loadedPerson =
					modelService.load(multiThreadPersonId, IPerson.class);
				assertTrue(loadedPerson.isPresent());
			}
		});
		// query the person
		service.execute(new Runnable() {
			@Override
			public void run(){
				IQuery<IPerson> query = modelService.getQuery(IPerson.class);
				query.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE, "%lastname");
				List<IPerson> results = query.execute();
				assertEquals(multiThreadPersonId, results.get(0).getId());
			}
		});
		// wait for executor to terminate
		service.shutdown();
		service.awaitTermination(5, TimeUnit.SECONDS);
		// remove the person
		modelService.remove(modelService.load(multiThreadPersonId, IPerson.class).get());
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
		
		IQuery<IPerson> query = modelService.getQuery(IPerson.class);
		query.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE, "%lastname %");
		List<IPerson> results = query.execute();
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void refreshCacheUserConfig() throws InterruptedException{
		IPerson owner = modelService.create(IPerson.class);
		owner.setLastName("test lastname 1");
		owner.setFirstName("test first name 1");
		assertTrue(modelService.save(owner));
		
		IUserConfig config1 = modelService.create(IUserConfig.class);
		config1.setOwner(owner);
		config1.setKey("test key 1");
		config1.setValue("test value 1");
		assertTrue(modelService.save(config1));
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(new Runnable() {
			@Override
			public void run(){
				int affected = modelService
					.executeNativeUpdate("UPDATE userconfig SET value = 'test value', lastupdate = "
						+ 1 + " WHERE param = 'test key 1'", false);
				assertEquals(1, affected);
			}
		});
		executor.shutdown();
		executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
		// test query no refresh -> old value from cache 
		INamedQuery<IUserConfig> configQuery =
			modelService.getNamedQuery(IUserConfig.class, false, "ownerid", "param");
		List<IUserConfig> configs = configQuery.executeWithParameters(
			configQuery.getParameterMap("ownerid", owner.getId(), "param", "test key 1"));
		assertEquals(1, configs.size());
		assertEquals("test value 1", configs.get(0).getValue());
		// test query with refresh -> updated value
		configQuery = modelService.getNamedQuery(IUserConfig.class, true, "ownerid", "param");
		configs = configQuery.executeWithParameters(
			configQuery.getParameterMap("ownerid", owner.getId(), "param", "test key 1"));
		assertEquals(1, configs.size());
		assertEquals("test value", configs.get(0).getValue());
		
		// test query with refresh
		for (int i = 0; i < 100; i++) {
			String updateStatement = "UPDATE userconfig SET value = 'test value "
				+ Integer.toString(i) + "', lastupdate = " + 1 + " WHERE param = 'test key 1'";
			executor = Executors.newSingleThreadExecutor();
			executor.execute(new Runnable() {
				@Override
				public void run(){
					int affected = modelService.executeNativeUpdate(updateStatement, false);
					assertEquals(1, affected);
				}
			});
			executor.shutdown();
			executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
			configQuery =
				modelService.getNamedQuery(IUserConfig.class, true, "ownerid", "param");
			configs = configQuery.executeWithParameters(
				configQuery.getParameterMap("ownerid", owner.getId(), "param", "test key 1"));
			assertEquals(1, configs.size());
			assertEquals("test value " + Integer.toString(i), configs.get(0).getValue());
		}
	}
	
	@Test
	public void updateRefreshPatient()
	{
		IPatient patient = modelService.create(IPatient.class);
		patient.setFirstName("Max");
		patient.setLastName("Mustermann");
		modelService.save(patient);
		
		IPatient dbP1 = modelService.load(patient.getId(), IPatient.class).get();		
		assertEquals(patient, dbP1);
		patient.setLastName("Mustermann1");
		assertEquals("Mustermann", dbP1.getLastName());
		assertEquals("Mustermann1", patient.getLastName());
		
		modelService.save(patient);
		//modelService.refresh(dbP1); if automatically registerEntityChangeEvent is used we dont need this
		assertEquals("Mustermann1", dbP1.getLastName());
		assertEquals("Mustermann1", patient.getLastName());
	}
	
	@Test
	public void deepCascadedRefresh(){
		IContact contact = modelService.create(IContact.class);
		contact.setDescription1("testContact");
		IRelatedContact relatedContact1 = modelService.create(IRelatedContact.class);
		contact.addRelatedContact(relatedContact1);
		IRelatedContact relatedContact2 = modelService.create(IRelatedContact.class);
		contact.addRelatedContact(relatedContact2);
		assertEquals(2, contact.getRelatedContacts().size());
		modelService.save(contact);
		
		EntityManager em = (EntityManager) OsgiServiceUtil.getService(IElexisEntityManager.class)
			.get().getEntityManager(true);
		em.getTransaction().begin();
		int executeUpdate =
			em.createNativeQuery("UPDATE kontakt_adress_joint SET DELETED='1' WHERE ID='"
				+ relatedContact2.getId() + "'")
				.executeUpdate();
		assertEquals(1, executeUpdate);
		em.getTransaction().commit();
		
		// detached objects not updated
		assertEquals(2, contact.getRelatedContacts().size());
		assertTrue(contact.getRelatedContacts().contains(relatedContact2));
		// refresh with deep cascaded refresh
		modelService.refresh(contact, true);
		assertEquals(1, contact.getRelatedContacts().size());
		assertFalse(contact.getRelatedContacts().contains(relatedContact2));
	}
	
	private class CreatePerson implements Runnable {
		private int index;
		
		public CreatePerson(int index){
			this.index = index;
		}
		
		@Override
		public void run(){
			IPerson person = modelService.create(IPerson.class);
			person.setLastName("test lastname " + index);
			person.setFirstName("test first name " + index);
			assertTrue(modelService.save(person));
		}
	}
	
	private class QueryPerson implements Runnable {
		private int index;
		
		public QueryPerson(int index){
			this.index = index;
		}
		
		@Override
		public void run(){
			IQuery<IPerson> query = modelService.getQuery(IPerson.class);
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
			IQuery<IPerson> query = modelService.getQuery(IPerson.class);
			query.and(ModelPackage.Literals.IPERSON__LAST_NAME, COMPARATOR.LIKE,
				"%lastname " + index);
			List<IPerson> results = query.execute();
			assertEquals(1, results.size());
			modelService.remove(results.get(0));
		}
	}
}
