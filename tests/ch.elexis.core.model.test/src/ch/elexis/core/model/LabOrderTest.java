package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.LabItemTyp;
import ch.elexis.core.utils.OsgiServiceUtil;

public class LabOrderTest {
	private IModelService modelService;
	
	private IPatient patient1;
	private IPatient patient2;
	
	private ILabItem item1;
	
	private ILabResult result1;
	private ILabResult result2;
	
	@Before
	public void before(){
		modelService = OsgiServiceUtil.getService(IModelService.class,
			"(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		
		patient1 = modelService.create(IPatient.class);
		patient1.setDescription1("test patient 1");
		modelService.save(patient1);
		patient2 = modelService.create(IPatient.class);
		patient2.setDescription1("test patient 2");
		modelService.save(patient2);
		item1 = modelService.create(ILabItem.class);
		item1.setCode("testItem");
		item1.setName("test item name");
		item1.setReferenceFemale("<25");
		item1.setReferenceMale("<30");
		item1.setTyp(LabItemTyp.NUMERIC);
		modelService.save(item1);
		result1 = modelService.create(ILabResult.class);
		result1.setItem(item1);
		result1.setPatient(patient1);
		result1.setResult("result 1");
		result2 = modelService.create(ILabResult.class);
		result2.setItem(item1);
		result2.setPatient(patient2);
		result2.setResult("result 2");
	}
	
	@After
	public void after(){
		modelService.remove(patient1);
		modelService.remove(patient2);
		modelService.remove(item1);
		modelService.remove(result1);
		modelService.remove(result2);
		
		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}
	
	@Test
	public void create(){
		ILabOrder order = modelService.create(ILabOrder.class);
		assertNotNull(order);
		assertTrue(order instanceof ILabOrder);
		order.setItem(item1);
		order.setPatient(patient1);
		order.setState(LabOrderState.ORDERED);
		assertTrue(modelService.save(order));
		assertNotNull(order.getOrderId());
		
		Optional<ILabOrder> loadedOrder = modelService.load(order.getId(), ILabOrder.class);
		assertTrue(loadedOrder.isPresent());
		assertFalse(order == loadedOrder.get());
		assertEquals(order, loadedOrder.get());
		assertEquals(order.getItem(), loadedOrder.get().getItem());
		assertEquals(order.getPatient(), loadedOrder.get().getPatient());
		assertEquals(order.getState(), loadedOrder.get().getState());
		
		ILabOrder order1 = modelService.create(ILabOrder.class);
		order.setItem(item1);
		order.setPatient(patient1);
		assertTrue(modelService.save(order1));
		assertEquals(1,
			Integer.parseInt(order1.getOrderId()) - Integer.parseInt(order.getOrderId()));
		
		modelService.remove(order);
	}
	
	@Test
	public void query(){
		ILabOrder order1 = modelService.create(ILabOrder.class);
		order1.setItem(item1);
		order1.setPatient(patient1);
		order1.setResult(result1);
		assertTrue(modelService.save(order1));
		
		ILabOrder order2 = modelService.create(ILabOrder.class);
		order2.setItem(item1);
		order2.setPatient(patient2);
		order2.setResult(result2);
		assertTrue(modelService.save(order2));
		
		IQuery<ILabOrder> query = modelService.getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__ITEM, COMPARATOR.EQUALS, item1);
		List<ILabOrder> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(2, existing.size());
		
		query = modelService.getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__PATIENT, COMPARATOR.EQUALS, patient2);
		existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertFalse(order2 == existing.get(0));
		assertEquals(order2, existing.get(0));
		assertEquals(order2.getPatient(), existing.get(0).getPatient());
		
		// test query via result
		assertEquals(order1, result1.getLabOrder());
		
		modelService.remove(order1);
		modelService.remove(order2);
	}
}
