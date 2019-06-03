package ch.elexis.core.model.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.model.builder.ICoverageBuilder;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.ISubQuery;
import ch.elexis.core.types.Country;
import ch.elexis.core.utils.OsgiServiceUtil;

public class CoreQueryTest {
	private IModelService modelService;
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
		clearContacts();
		clearCoverages();
	}
	
	@After
	public void after(){
		clearContacts();
		clearCoverages();
		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}
	
	@Test
	public void queryExecute(){
		IQuery<IContact> query = modelService.getQuery(IContact.class);
		assertNotNull(query);
		List<IContact> results = query.execute();
		assertNotNull(results);
		assertTrue(results.isEmpty());
	}
	
	@Test
	public void queryDeleted(){
		createContact("test1", "test1");
		IContact contact2 = createContact("test2", "test2");
		modelService.delete(contact2);
		createContact("test3", "test3");
		
		// get query with existing where deleted group
		IQuery<IContact> query = modelService.getQuery(IContact.class);
		assertNotNull(query);
		List<IContact> results = query.execute();
		assertNotNull(results);
		assertEquals(2, results.size());
		
		// get query without existing where deleted group
		query = modelService.getQuery(IContact.class, true);
		assertNotNull(query);
		results = query.execute();
		assertNotNull(results);
		assertEquals(3, results.size());
	}
	
	@Test
	public void queryGroups(){
		createContact("test1", "test1");
		IContact contact2 = createContact("test2", "test2");
		modelService.delete(contact2);
		createContact("test3", "test3");
		
		// get query with existing where deleted group
		IQuery<IContact> query = modelService.getQuery(IContact.class);
		assertNotNull(query);
		query.startGroup();
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "test%");
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.EQUALS, "nonexisting");
		query.startGroup();
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "test%");
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.EQUALS, "nonexisting");
		query.andJoinGroups();
		List<IContact> results = query.execute();
		assertNotNull(results);
		assertEquals(2, results.size());
		
		// get query without existing where deleted group
		query = modelService.getQuery(IContact.class, true);
		assertNotNull(query);
		query.startGroup();
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "test%");
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.EQUALS, "nonexisting");
		query.startGroup();
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "test%");
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.EQUALS, "nonexisting");
		query.orJoinGroups();
		results = query.execute();
		assertNotNull(results);
		assertEquals(3, results.size());
	}
	
	@Test
	public void findAll(){
		createContact("test1", "test1");
		createContact("test2", "test2");
		
		List<IContact> contacts = modelService.findAll(IContact.class);
		assertEquals(2, contacts.size());
	}
	
	@Test
	public void findAllById(){
		IContact iContact1 = createContact("test1", "test1");
		IContact iContact2 = createContact("test2", "test2");
		IContact iContact3 = createContact("test3", "test3");

		assertEquals(1, modelService.findAllById(Arrays.asList(iContact1.getId()), IContact.class).size());
		assertEquals(2, modelService.findAllById(Arrays.asList(iContact1.getId(), iContact2.getId()), IContact.class).size());
		assertEquals(3, modelService.findAllById(Arrays.asList(iContact1.getId(), iContact2.getId(), iContact3.getId()), IContact.class).size());
		assertEquals(0, modelService.findAllById(new ArrayList<>(), IContact.class).size());
	}
	
	@Test
	public void queryComplexWithIN(){
		IContact iContact1 = createContact("test1", "test1");
		IContact iContact2 = createContact("test2", "test2");
		IContact iContact3 = createContact("test3", "test2");
		iContact1.setCountry(Country.CH);
		iContact1.setPatient(true);
		iContact2.setCountry(Country.AT);
		iContact3.setCountry(Country.DE);
		modelService.save(iContact1);
		modelService.save(iContact2);
		modelService.save(iContact3);
		
		// get all contacts with lastname in (test2, xy)
		IQuery<IContact> query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.IN, Arrays.asList("test2", "xy"));
		assertEquals(2, query.execute().size());
		
		// get all contacts with lastname in (test1, test2, test3)
		query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.IN, Arrays.asList("test1", "test2", "test3"));
		assertEquals(3, query.execute().size());
		
		// get all contacts with lastname in (test1, test2, test3) and firstname = (test2)
		query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.IN, Arrays.asList("test1", "test2", "test3"));
		query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.EQUALS, "test2");
		List<IContact> results = query.execute();
		assertEquals(1, results.size());
		assertEquals(iContact2.getId(), results.get(0).getId());
		
		// get all contacts with country in (CH, AT)
		query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__COUNTRY, COMPARATOR.IN, Arrays.asList(Country.CH, Country.AT));
		assertEquals(2, query.execute().size());
		
		// get all contact with country in (CH, AT, DE, US) and patient = true
		query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__COUNTRY, COMPARATOR.IN, Arrays.asList(Country.CH, Country.AT, Country.DE, Country.US));
		query.and(ModelPackage.Literals.ICONTACT__PATIENT, COMPARATOR.EQUALS, true);
		results = query.execute();
		assertEquals(1, results.size());
		assertEquals(iContact1.getId(), results.get(0).getId());
		
		// get all contact with country in (CH, AT, DE, US) or patient = true
		query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__COUNTRY, COMPARATOR.IN, Arrays.asList(Country.CH, Country.AT, Country.DE, Country.US));
		query.or(ModelPackage.Literals.ICONTACT__PATIENT, COMPARATOR.EQUALS, true);
		results = query.execute();
		assertEquals(3, results.size());
		
		// get all contact with country in (x,y,z)
		query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION2, COMPARATOR.IN, Arrays.asList("x,y,z"));
		assertEquals(0, query.execute().size());
		
		// get all contact with country in ()
		query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__COUNTRY, COMPARATOR.IN, new ArrayList<>());
		assertEquals(0, query.execute().size());
		
	}
	
	@Test
	public void queryContact(){
		createContact("test1", "test1");
		createContact("test2", "test2");
		
		IQuery<IContact> query = modelService.getQuery(IContact.class);
		assertNotNull(query);
		List<IContact> results = query.execute();
		assertNotNull(results);
		assertEquals(2, results.size());
	}
	
	@Test
	public void queryContactDescription(){
		createContact("test1", "test1");
		createContact("test2", "test2");
		
		IQuery<IContact> query = modelService.getQuery(IContact.class);
		assertNotNull(query);
		query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.EQUALS, "test1");
		List<IContact> results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("test1", results.get(0).getDescription1());
		
		query = modelService.getQuery(IContact.class);
		query.and(ModelPackage.Literals.ICONTACT__DESCRIPTION3, COMPARATOR.EQUALS, (String) null);
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
		
		IQuery<IPatient> query = modelService.getQuery(IPatient.class);
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
		
		IQuery<IPatient> query = modelService.getQuery(IPatient.class);
		assertNotNull(query);
		query.and(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.EQUALS, "patient1");
		List<IPatient> results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("patient1", results.get(0).getFirstName());
		
		query = modelService.getQuery(IPatient.class);
		assertNotNull(query);
		query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
			LocalDate.of(1999, 1, 1));
		results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals(LocalDate.of(1999, 1, 1), results.get(0).getDateOfBirth().toLocalDate());
		
		query = modelService.getQuery(IPatient.class);
		assertNotNull(query);
		query.and(ModelPackage.Literals.IPERSON__FIRST_NAME, COMPARATOR.EQUALS, "patient2");
		query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.EQUALS,
			LocalDate.of(1999, 2, 2));
		results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("patient2", results.get(0).getFirstName());
		assertEquals(LocalDate.of(1999, 2, 2), results.get(0).getDateOfBirth().toLocalDate());
		
		query = modelService.getQuery(IPatient.class);
		assertNotNull(query);
		query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.GREATER_OR_EQUAL,
			LocalDate.of(1999, 2, 2));
		query.and(ModelPackage.Literals.IPERSON__DATE_OF_BIRTH, COMPARATOR.LESS,
			LocalDate.of(1999, 12, 12));
		results = query.execute();
		assertNotNull(results);
		assertEquals(1, results.size());
		assertEquals("patient2", results.get(0).getFirstName());
		assertEquals(LocalDate.of(1999, 2, 2), results.get(0).getDateOfBirth().toLocalDate());
		
	}
	
	@Test
	public void queryNativeDistinctQuery(){
		createContact("test1", "test1");
		createContact("test2", "test2");
		
		String nativeQuery = "SELECT DISTINCT BEZEICHNUNG1 FROM KONTAKT";
		
		List<?> collect = modelService.executeNativeQuery(nativeQuery).collect(Collectors.toList());
		assertEquals(Arrays.asList(new String[] {
			"test1", "test2"
		}), collect);
	}
	
	@Test
	public void queryOrderBy(){
		createContact("test1", "test1");
		createContact("test2", "test2");
		createContact("test3", "test3");
		createContact("test4", "test4");
		
		IQuery<IContact> query = modelService.getQuery(IContact.class);
		query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION2, ORDER.ASC);
		List<IContact> ordered = query.execute();
		assertEquals("test1", ordered.get(0).getDescription1());
		
		query = modelService.getQuery(IContact.class);
		query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION2, ORDER.DESC);
		ordered = query.execute();
		assertEquals("test4", ordered.get(0).getDescription1());
		
		query = modelService.getQuery(IContact.class);
		Map<String, Object> caseContext = new HashMap<>();
		caseContext.put("when|description2|equals|test3", Integer.valueOf(1));
		caseContext.put("otherwise", Integer.valueOf(2));
		query.orderBy(caseContext, ORDER.ASC);
		query.orderBy(ModelPackage.Literals.ICONTACT__DESCRIPTION2, ORDER.DESC);
		ordered = query.execute();
		assertEquals("test3", ordered.get(0).getDescription1());
		assertEquals("test4", ordered.get(1).getDescription1());
		assertEquals("test2", ordered.get(2).getDescription1());
	}
	
	@Test
	public void subQueryTest(){
		IPatient patient1 = createPatient("patient1", "patient1", LocalDate.of(1999, 1, 1));
		IPatient patient2 = createPatient("patient2", "patient2", LocalDate.of(1999, 2, 2));
		createPatient("patient3", "patient3", LocalDate.of(1999, 12, 12));
		ICoverage coverage1 = createCoverage(patient1, "patient1");
		ICoverage coverage2 = createCoverage(patient2, "patient2");
		
		IQuery<IPatient> query = modelService.getQuery(IPatient.class);
		ISubQuery<ICoverage> subQuery = query.createSubQuery(ICoverage.class, modelService);
		subQuery.andParentCompare("description1", COMPARATOR.EQUALS, "bezeichnung");
		query.exists(subQuery);
		List<IPatient> results = query.execute();
		assertEquals(2, results.size());
		assertTrue(results.contains(patient1));
		assertTrue(results.contains(patient2));
		
		modelService.remove(coverage2);
		results = query.execute();
		assertEquals(1, results.size());
		assertTrue(results.contains(patient1));
		assertFalse(results.contains(patient2));
	}
	
	private void clearContacts(){
		IQuery<IContact> query = modelService.getQuery(IContact.class, true);
		List<IContact> results = query.execute();
		results.stream().forEach(c -> modelService.remove(c));
	}
	
	private void clearCoverages(){
		IQuery<ICoverage> query = modelService.getQuery(ICoverage.class, true);
		List<ICoverage> results = query.execute();
		results.stream().forEach(c -> modelService.remove(c));
	}
	
	private IContact createContact(String desc1, String desc2){
		IContact contact = modelService.create(IContact.class);
		assertNotNull(contact);
		assertTrue(contact instanceof IContact);
		
		contact.setDescription1(desc1);
		contact.setDescription2(desc2);
		assertTrue(modelService.save(contact));
		return contact;
	}
	
	private IPatient createPatient(String firstName, String lastName, LocalDate birthDate){
		IPatient patient = modelService.create(IPatient.class);
		assertNotNull(patient);
		assertTrue(patient instanceof IPatient);
		
		patient.setPatient(true);
		patient.setLastName(lastName);
		patient.setFirstName(firstName);
		patient.setDateOfBirth(birthDate.atStartOfDay());
		assertTrue(modelService.save(patient));
		
		return patient;
	}
	
	private ICoverage createCoverage(IPatient patient, String coverageLabel){
		ICoverage coverage = new ICoverageBuilder(modelService, patient, coverageLabel,
			"testReason", "testBillingSystem").buildAndSave();
		return coverage;
	}
}
