package ch.elexis.core.model.perf;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.model.ILabItem;
import ch.elexis.core.model.ILabMapping;
import ch.elexis.core.model.ILabOrder;
import ch.elexis.core.model.ILabResult;
import ch.elexis.core.model.ILaboratory;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.INamedQuery;
import ch.elexis.core.types.Gender;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.elexis.data.DBConnection;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabMapping;
import ch.elexis.data.LabOrder;
import ch.elexis.data.LabResult;
import ch.elexis.data.Labor;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.rgw.tools.TimeTool;

public class LaborPerformanceTest {
	
	private IModelService modelService;
	
	private List<IPatient> jpaPatients;
	private List<ILabItem> jpaItems;
	
	private List<Patient> poPatients;
	private List<LabItem> poItems;
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class).get();
		
		modelService.getQuery(ILabResult.class).execute().forEach(r -> modelService.remove(r));
		modelService.getQuery(ILabMapping.class).execute().forEach(m -> modelService.remove(m));
		modelService.getQuery(ILabItem.class).execute().forEach(i -> modelService.remove(i));
		modelService.getQuery(IPatient.class).execute().forEach(p -> modelService.remove(p));
		
		if (PersistentObject.getDefaultConnection() == null) {
			// connect to separate in mem database
			PersistentObject.connect(getTestDBConnection("poTest"));
		} else {
			new Query<LabResult>(LabResult.class).execute().forEach(r -> r.removeFromDatabase());
			new Query<LabMapping>(LabMapping.class).execute().forEach(m -> m.removeFromDatabase());
			new Query<LabItem>(LabItem.class).execute().forEach(i -> i.removeFromDatabase());
			new Query<Patient>(Patient.class).execute().forEach(p -> p.removeFromDatabase());
		}
	}
	
	@After
	public void after(){
		OsgiServiceUtil.ungetService(modelService);
	}
	
	@Test
	public void jpaCreateModel(){
		long perfStartTime = System.currentTimeMillis();
		IMandator mandator = modelService.create(IMandator.class);
		mandator.setDescription1("test");
		mandator.setDescription2("mandator");
		modelService.save(mandator);
		
		jpaPatients = new ArrayList<>();
		// create patients
		for (int i = 0; i < 10; i++) {
			IPatient patient = modelService.create(IPatient.class);
			patient.setFirstName("firsname" + i);
			patient.setLastName("lastname" + i);
			patient.setGender((i % 2 == 0) ? Gender.FEMALE : Gender.MALE);
			patient.setDateOfBirth(LocalDateTime.of(1999, 1, i + 1, 1, i + 1));
			modelService.save(patient);
			jpaPatients.add(patient);
		}
		// create laboratories
		ILaboratory lab1 = modelService.create(ILaboratory.class);
		lab1.setCode("test1");
		lab1.setDescription1("Labor " + "test1");
		modelService.save(lab1);
		ILaboratory lab2 = modelService.create(ILaboratory.class);
		lab2.setCode("test2");
		lab2.setDescription1("Labor " + "test2");
		modelService.save(lab2);
		jpaItems = new ArrayList<>();
		// create labitems
		for (int i = 0; i < 10; i++) {
			jpaItems.add(createLabItem("code" + i, "name" + i, (i % 2 == 0) ? lab1 : lab2, "1", "2",
				"?", LabItemTyp.NUMERIC, "test", (i % 2 == 0) ? "1" : "2"));
		}
		System.out.println("jpaCreateModel items created after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
		TimeTool time = new TimeTool();
		// create results
		for (int i = 0; i < 1000; i++) {
			int index10 = i % 10;
			createLabResult(jpaPatients.get(index10), time, jpaItems.get(index10),
				Integer.toString(index10), null, (i % 2 == 0) ? "2" : "1",
				(i % 2 == 0) ? lab1 : lab2, null, null, null, mandator, time);
		}
		System.out.println("jpaCreateModel results created after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
	}
	
	@Test
	public void jpaQueryModel(){
		jpaCreateModel();
		long perfStartTime = System.currentTimeMillis();
		// query lab items
		HashMap<String, Object> parameters = new HashMap<>();
		for (int i = 0; i < 100; i++) {
			int index10 = i % 10;
			parameters.put("code", "code" + index10);
			parameters.put("name", "name" + index10);
			parameters.put("typ", LabItemTyp.NUMERIC);
			INamedQuery<ILabItem> namedQuery =
				modelService.getNamedQuery(ILabItem.class, "code", "name", "typ");
			List<ILabItem> items = namedQuery.executeWithParameters(parameters);
			assertFalse(items.isEmpty());
		}
		System.out.println("jpaQueryModel items queried after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
		// query lab results
		parameters = new HashMap<>();
		for (int i = 0; i < 100; i++) {
			int index10 = i % 10;
			parameters.put("patient", jpaPatients.get(index10));
			parameters.put("item", jpaItems.get(index10));
			INamedQuery<ILabResult> namedQuery =
				modelService.getNamedQuery(ILabResult.class, "patient", "item");
			List<ILabResult> results = namedQuery.executeWithParameters(parameters);
			assertFalse(results.isEmpty());
		}
		System.out.println("jpaQueryModel results queried after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
	}
	
	@Test
	public void jpaQueryAndAccessModel(){
		jpaCreateModel();
		long perfStartTime = System.currentTimeMillis();
		// query lab items
		HashMap<String, Object> parameters = new HashMap<>();
		for (int i = 0; i < 100; i++) {
			int index10 = i % 10;
			parameters.put("code", "code" + index10);
			parameters.put("name", "name" + index10);
			parameters.put("typ", LabItemTyp.NUMERIC);
			INamedQuery<ILabItem> namedQuery =
				modelService.getNamedQuery(ILabItem.class, "code", "name", "typ");
			List<ILabItem> items = namedQuery.executeWithParameters(parameters);
			assertFalse(items.isEmpty());
			for (ILabItem iLabItem : items) {
				iLabItem.getTyp();
				iLabItem.getGroup();
				iLabItem.getCode();
				iLabItem.getPriority();
				iLabItem.getReferenceFemale();
			}
		}
		System.out.println("jpaQueryModel items queried and access after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
		// query lab results
		parameters = new HashMap<>();
		for (int i = 0; i < 100; i++) {
			int index10 = i % 10;
			parameters.put("patient", jpaPatients.get(index10));
			parameters.put("item", jpaItems.get(index10));
			INamedQuery<ILabResult> namedQuery =
				modelService.getNamedQuery(ILabResult.class, "patient", "item");
			List<ILabResult> results = namedQuery.executeWithParameters(parameters);
			assertFalse(results.isEmpty());
			for (ILabResult iLabResult : results) {
				iLabResult.getObservationTime();
				iLabResult.getResult();
				iLabResult.getReferenceFemale();
				iLabResult.getPathologicDescription();
				iLabResult.isPathologic();
				iLabResult.getItem();
				iLabResult.getItem().getLabel();
			}
		}
		System.out.println("jpaQueryModel results queried and access after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
	}
	
	@Test
	public void poCreateModel(){
		long perfStartTime = System.currentTimeMillis();
		Mandant mandant = new Mandant("mandator", "test", "1.1.1999", "M");
		poPatients = new ArrayList<>();
		// create patients
		for (int i = 0; i < 10; i++) {
			Patient patient = new Patient("lastname" + i, "firstname" + i,
				(i + 1) + "." + 1 + ".1999", (i % 2 == 0) ? "F" : "M");
			poPatients.add(patient);
		}
		// create laboratories
		Labor lab1 = new Labor("test1", "Labor " + "test1");
		Labor lab2 = new Labor("test2", "Labor " + "test2");
		poItems = new ArrayList<>();
		// create labitems
		for (int i = 0; i < 10; i++) {
			poItems.add(createPoLabItem("code" + i, "name" + i, (i % 2 == 0) ? lab1 : lab2, "1",
				"2", "?", LabItemTyp.NUMERIC, "test", (i % 2 == 0) ? "1" : "2"));
		}
		System.out.println("poCreateModel items created after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
		TimeTool time = new TimeTool();
		// create results
		for (int i = 0; i < 1000; i++) {
			int index10 = i % 10;
			createPoLabResult(poPatients.get(index10), time, poItems.get(index10),
				Integer.toString(index10), null, (i % 2 == 0) ? "2" : "1",
				(i % 2 == 0) ? lab1 : lab2, null, null, null, mandant.getId(), time, "");
		}
		System.out.println("poCreateModel results created after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
	}
	
	private DBConnection getTestDBConnection(String name){
		DBConnection connection = new DBConnection();
		connection.setDBConnectString("jdbc:h2:mem:" + name);
		connection.setDBUser("sa");
		connection.setDBPassword("");
		assertTrue(connection.connect());
		return connection;
	}
	
	@Test
	public void poQueryModel(){
		poCreateModel();
		long perfStartTime = System.currentTimeMillis();
		// query lab items
		for (int i = 0; i < 100; i++) {
			int index10 = i % 10;
			Query<LabItem> query = new Query<>(LabItem.class);
			query.add(LabItem.SHORTNAME, Query.EQUALS, "code" + index10);
			query.add(LabItem.TITLE, Query.EQUALS, "name" + index10);
			query.add(LabItem.TYPE, Query.EQUALS, Integer.toString(LabItemTyp.NUMERIC.getType()));
			List<LabItem> items = query.execute();
			assertFalse(items.isEmpty());
		}
		System.out.println("poQueryModel items queried after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
		// query lab results
		for (int i = 0; i < 100; i++) {
			int index10 = i % 10;
			Query<LabResult> query = new Query<>(LabResult.class);
			query.add(LabResult.PATIENT_ID, Query.EQUALS, poPatients.get(index10).getId());
			query.add(LabResult.ITEM_ID, Query.EQUALS, poItems.get(index10).getId());
			List<LabResult> results = query.execute();
			assertFalse(results.isEmpty());
		}
		System.out.println("poQueryModel results queried after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
	}
	
	@Test
	public void poQueryAndAccessModel(){
		poCreateModel();
		long perfStartTime = System.currentTimeMillis();
		// query lab items
		for (int i = 0; i < 100; i++) {
			int index10 = i % 10;
			Query<LabItem> query = new Query<>(LabItem.class);
			query.add(LabItem.SHORTNAME, Query.EQUALS, "code" + index10);
			query.add(LabItem.TITLE, Query.EQUALS, "name" + index10);
			query.add(LabItem.TYPE, Query.EQUALS, Integer.toString(LabItemTyp.NUMERIC.getType()));
			List<LabItem> items = query.execute();
			assertFalse(items.isEmpty());
			for (LabItem labItem : items) {
				labItem.getTyp();
				labItem.getGroup();
				labItem.getKuerzel();
				labItem.getPriority();
				labItem.getReferenceFemale();
			}
		}
		System.out.println("poQueryModel items queried and access after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
		// query lab results
		for (int i = 0; i < 100; i++) {
			int index10 = i % 10;
			Query<LabResult> query = new Query<>(LabResult.class);
			query.add(LabResult.PATIENT_ID, Query.EQUALS, poPatients.get(index10).getId());
			query.add(LabResult.ITEM_ID, Query.EQUALS, poItems.get(index10).getId());
			List<LabResult> results = query.execute();
			assertFalse(results.isEmpty());
			for (LabResult labResult : results) {
				labResult.getObservationTime();
				labResult.getResult();
				labResult.getRefFemale();
				labResult.getPathologicDescription();
				labResult.isFlag(LabResultConstants.PATHOLOGIC);
				labResult.getItem();
				labResult.getItem().getLabel();
			}
		}
		System.out.println("poQueryModel results queried and access after "
			+ Long.valueOf((System.currentTimeMillis() - perfStartTime)) + "ms");
	}
	
	/**
	 * Method from LabImportUtil
	 * 
	 * @param code
	 * @param name
	 * @param origin
	 * @param male
	 * @param female
	 * @param unit
	 * @param typ
	 * @param group
	 * @param priority
	 * @return
	 */
	private ILabItem createLabItem(String code, String name, ILaboratory origin, String male,
		String female, String unit, LabItemTyp typ, String group, String priority){
		ILabItem ret = modelService.create(ILabItem.class);
		ret.setCode(code);
		ret.setName(name);
		ret.setReferenceMale(male);
		ret.setReferenceFemale(female);
		ret.setUnit(unit);
		ret.setTyp(typ);
		ret.setGroup(group);
		ret.setPriority(priority);
		modelService.save(ret);
		
		ILabMapping mapping = modelService.create(ILabMapping.class);
		mapping.setItem(ret);
		mapping.setOrigin(origin);
		mapping.setItemName(code);
		modelService.save(mapping);
		return ret;
	}
	
	/**
	 * Method from LabImportUtil
	 * 
	 * @param code
	 * @param name
	 * @param labor
	 * @param male
	 * @param female
	 * @param unit
	 * @param typ
	 * @param testGroupName
	 * @param nextTestGroupSequence
	 * @return
	 */
	public LabItem createPoLabItem(String code, String name, Labor labor, String male,
		String female, String unit, LabItemTyp typ, String testGroupName,
		String nextTestGroupSequence){
		LabItem ret = new LabItem(code, name, labor.getId(), male, female, unit, typ, testGroupName,
			nextTestGroupSequence);
		LabMapping mapping = new LabMapping(labor.getId(), code, ret.getId(), false);
		return ret;
	}
	
	/**
	 * Method from LabImportUtil
	 * 
	 * @param patient
	 * @param date
	 * @param labItem
	 * @param result
	 * @param comment
	 * @param refVal
	 * @param laboratory
	 * @param subId
	 * @param labOrder
	 * @param orderId
	 * @param mandator
	 * @param observationTime
	 * @return
	 */
	private ILabResult createLabResult(IPatient patient, TimeTool date, ILabItem labItem,
		String result, String comment, String refVal, ILaboratory laboratory, String subId,
		ILabOrder labOrder, String orderId, IMandator mandator, TimeTool observationTime){
		
		ILabResult labResult = modelService.create(ILabResult.class);
		labResult.setPatient(patient);
		labResult.setDate(date.toLocalDate());
		labResult.setItem(labItem);
		labResult.setObservationTime(observationTime.toLocalDateTime());
		labResult.setOrigin(laboratory);
		if (patient.getGender() == Gender.FEMALE) {
			labResult.setReferenceFemale(refVal);
		} else {
			labResult.setReferenceMale(refVal);
		}
		labResult.setResult(result);
		labResult.setComment(comment);
		
		// create new ILabOrder or set result in existing
		if (labOrder == null) {
			ILabOrder order = modelService.create(ILabOrder.class);
			order.setItem(labItem);
			order.setPatient(patient);
			order.setResult(labResult);
			order.setTimeStamp(LocalDateTime.now());
			order.setObservationTime(observationTime.toLocalDateTime());
			order.setMandator(mandator);
			if (orderId != null) {
				order.setOrderId(orderId);
			}
			modelService.save(order);
		} else {
			labOrder.setResult(labResult);
		}
		
		modelService.save(labResult);
		return labResult;
	}
	
	/**
	 * Method from LabImportUtil
	 * 
	 * @param patient
	 * @param date
	 * @param labItem
	 * @param result
	 * @param comment
	 * @param refVal
	 * @param origin
	 * @param subId
	 * @param labOrder
	 * @param orderId
	 * @param mandantId
	 * @param time
	 * @param groupName
	 * @return
	 */
	public LabResult createPoLabResult(Patient patient, TimeTool date, LabItem labItem,
		String result, String comment, String refVal, Labor origin, String subId, LabOrder labOrder,
		String orderId, String mandantId, TimeTool time, String groupName){
		
		Patient pat = Patient.load(patient.getId());
		LabItem item = LabItem.load(labItem.getId());
		Labor labor = Labor.load(origin.getId());
		
		LabResult labResult = LabResult.createLabResultAndAssertLabOrder(pat, date, item, result,
			comment, labor, refVal, labOrder, orderId, mandantId, time, groupName);
		
		if (subId != null) {
			labResult.setDetail(LabResult.EXTINFO_HL7_SUBID, subId);
		}
		// TODO LockHook too early
		return labResult;
	}
}
