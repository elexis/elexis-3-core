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
	private IModelService modelSerice;
	
	private IPatient patient1;
	private IPatient patient2;
	
	private ILabItem item1;
	
	private ILabResult result1;
	private ILabResult result2;
	
	@Before
	public void before(){
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
		
		patient1 = modelSerice.create(IPatient.class);
		patient1.setDescription1("test patient 1");
		modelSerice.save(patient1);
		patient2 = modelSerice.create(IPatient.class);
		patient2.setDescription1("test patient 2");
		modelSerice.save(patient2);
		item1 = modelSerice.create(ILabItem.class);
		item1.setCode("testItem");
		item1.setName("test item name");
		item1.setReferenceFemale("<25");
		item1.setReferenceMale("<30");
		item1.setTyp(LabItemTyp.NUMERIC);
		modelSerice.save(item1);
		result1 = modelSerice.create(ILabResult.class);
		result1.setItem(item1);
		result1.setPatient(patient1);
		result1.setResult("result 1");
		result2 = modelSerice.create(ILabResult.class);
		result2.setItem(item1);
		result2.setPatient(patient2);
		result2.setResult("result 2");
	}
	
	@After
	public void after(){
		modelSerice.remove(patient1);
		modelSerice.remove(patient2);
		modelSerice.remove(item1);
		modelSerice.remove(result1);
		modelSerice.remove(result2);
		
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	@Test
	public void create(){
		ILabOrder order = modelSerice.create(ILabOrder.class);
		assertNotNull(order);
		assertTrue(order instanceof ILabOrder);
		order.setItem(item1);
		order.setPatient(patient1);
		order.setState(LabOrderState.ORDERED);
		assertTrue(modelSerice.save(order));
		assertNotNull(order.getOrderId());
		
		Optional<ILabOrder> loadedOrder = modelSerice.load(order.getId(), ILabOrder.class);
		assertTrue(loadedOrder.isPresent());
		assertFalse(order == loadedOrder.get());
		assertEquals(order, loadedOrder.get());
		assertEquals(order.getItem(), loadedOrder.get().getItem());
		assertEquals(order.getPatient(), loadedOrder.get().getPatient());
		assertEquals(order.getState(), loadedOrder.get().getState());
		
		ILabOrder order1 = modelSerice.create(ILabOrder.class);
		order.setItem(item1);
		order.setPatient(patient1);
		assertTrue(modelSerice.save(order1));
		assertEquals(1,
			Integer.parseInt(order1.getOrderId()) - Integer.parseInt(order.getOrderId()));
		
		modelSerice.remove(order);
	}
	
	@Test
	public void query(){
		ILabOrder order1 = modelSerice.create(ILabOrder.class);
		order1.setItem(item1);
		order1.setPatient(patient1);
		order1.setResult(result1);
		assertTrue(modelSerice.save(order1));
		
		ILabOrder order2 = modelSerice.create(ILabOrder.class);
		order2.setItem(item1);
		order2.setPatient(patient2);
		order2.setResult(result2);
		assertTrue(modelSerice.save(order2));
		
		IQuery<ILabOrder> query = modelSerice.getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__ITEM, COMPARATOR.EQUALS, item1);
		List<ILabOrder> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(2, existing.size());
		
		query = modelSerice.getQuery(ILabOrder.class);
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
		
		modelSerice.remove(order1);
		modelSerice.remove(order2);
	}
}
