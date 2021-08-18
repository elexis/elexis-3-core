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

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.LabItemTyp;

public class LabOrderTest extends AbstractTest {
	
	private IPatient patient2;
	
	private ILabItem item1;
	
	private ILabResult result1;
	private ILabResult result2;
	
	@Override
	@Before
	public void before(){
		super.before();
		super.createPatient();
		
		patient2 = coreModelService.create(IPatient.class);
		patient2.setDescription1("test patient 2");
		coreModelService.save(patient2);
		
		item1 = coreModelService.create(ILabItem.class);
		item1.setCode("testItem");
		item1.setName("test item name");
		item1.setReferenceFemale("<25");
		item1.setReferenceMale("<30");
		item1.setTyp(LabItemTyp.NUMERIC);
		coreModelService.save(item1);
		result1 = coreModelService.create(ILabResult.class);
		result1.setItem(item1);
		result1.setPatient(patient);
		result1.setResult("result 1");
		result2 = coreModelService.create(ILabResult.class);
		result2.setItem(item1);
		result2.setPatient(patient2);
		result2.setResult("result 2");
	}
	
	@Override
	@After
	public void after(){
		coreModelService.remove(result1);
		coreModelService.remove(result2);
		coreModelService.remove(item1);
		coreModelService.remove(patient2);
		super.after();
	}
	
	@Test
	public void create(){
		ILabOrder order = coreModelService.create(ILabOrder.class);
		assertNotNull(order);
		assertTrue(order instanceof ILabOrder);
		order.setItem(item1);
		order.setPatient(patient);
		order.setState(LabOrderState.ORDERED);
		coreModelService.save(order);
		assertNotNull(order.getOrderId());
		
		Optional<ILabOrder> loadedOrder = coreModelService.load(order.getId(), ILabOrder.class);
		assertTrue(loadedOrder.isPresent());
		assertFalse(order == loadedOrder.get());
		assertEquals(order, loadedOrder.get());
		assertEquals(order.getItem(), loadedOrder.get().getItem());
		assertEquals(order.getPatient(), loadedOrder.get().getPatient());
		assertEquals(order.getState(), loadedOrder.get().getState());
		
		ILabOrder order1 = coreModelService.create(ILabOrder.class);
		order.setItem(item1);
		order.setPatient(patient);
		coreModelService.save(order1);
		assertEquals(1,
			Integer.parseInt(order1.getOrderId()) - Integer.parseInt(order.getOrderId()));
		
		coreModelService.remove(order);
	}
	
	@Test
	public void query(){
		ILabOrder order1 = coreModelService.create(ILabOrder.class);
		order1.setItem(item1);
		order1.setPatient(patient);
		order1.setResult(result1);
		coreModelService.save(order1);
		
		ILabOrder order2 = coreModelService.create(ILabOrder.class);
		order2.setItem(item1);
		order2.setPatient(patient2);
		order2.setResult(result2);
		coreModelService.save(order2);
		
		IQuery<ILabOrder> query = coreModelService.getQuery(ILabOrder.class);
		query.and(ModelPackage.Literals.ILAB_ORDER__ITEM, COMPARATOR.EQUALS, item1);
		List<ILabOrder> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(2, existing.size());
		
		query = coreModelService.getQuery(ILabOrder.class);
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
		
		coreModelService.remove(order1);
		coreModelService.remove(order2);
	}
}
