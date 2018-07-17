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

public class LabMappingTest {
	private IModelService modelSerice;
	
	private ILabItem item1;
	private ILabItem item2;
	
	private IContact origin1;
	private IContact origin2;
	
	@Before
	public void before(){
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
		
		item1 = modelSerice.create(ILabItem.class);
		item1.setCode("testItem1");
		item1.setName("test item name 1");
		item1.setTyp(LabItemTyp.NUMERIC);
		assertTrue(modelSerice.save(item1));
		
		item2 = modelSerice.create(ILabItem.class);
		item2.setCode("testItem2");
		item2.setName("test item name 2");
		item2.setTyp(LabItemTyp.TEXT);
		assertTrue(modelSerice.save(item2));
		
		origin1 = modelSerice.create(IContact.class);
		origin1.setDescription1("test origin 1");
		assertTrue(modelSerice.save(origin1));
		
		origin2 = modelSerice.create(IContact.class);
		origin2.setDescription1("test origin 2");
		assertTrue(modelSerice.save(origin2));
	}
	
	@After
	public void after(){
		modelSerice.remove(item1);
		modelSerice.remove(item2);
		
		modelSerice.remove(origin1);
		modelSerice.remove(origin2);
		
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	@Test
	public void create(){
		ILabMapping mapping = modelSerice.create(ILabMapping.class);
		assertNotNull(mapping);
		assertTrue(mapping instanceof ILabMapping);
		
		mapping.setItemName("TestItemMap1");
		mapping.setItem(item1);
		mapping.setOrigin(origin1);
		assertTrue(modelSerice.save(mapping));
		
		Optional<ILabMapping> loadedMapping = modelSerice.load(mapping.getId(), ILabMapping.class);
		assertTrue(loadedMapping.isPresent());
		assertFalse(mapping == loadedMapping.get());
		assertEquals(mapping, loadedMapping.get());
		assertEquals(mapping.getItemName(), loadedMapping.get().getItemName());
		assertEquals(mapping.getItem(), loadedMapping.get().getItem());
		assertEquals(mapping.getOrigin(), loadedMapping.get().getOrigin());
		
		modelSerice.remove(mapping);
	}
	
	@Test
	public void query(){
		ILabMapping mapping1 = modelSerice.create(ILabMapping.class);
		mapping1.setItemName("TestItemMap1");
		mapping1.setItem(item1);
		mapping1.setOrigin(origin1);
		modelSerice.save(mapping1);
		
		ILabMapping mapping2 = modelSerice.create(ILabMapping.class);
		mapping2.setItemName("TestItemMap2");
		mapping2.setItem(item2);
		mapping2.setOrigin(origin2);
		modelSerice.save(mapping2);
		
		IQuery<ILabMapping> query = modelSerice.getQuery(ILabMapping.class);
		query.and(ModelPackage.Literals.ILAB_MAPPING__ITEM_NAME, COMPARATOR.EQUALS, "TestItemMap2");
		query.and(ModelPackage.Literals.ILAB_MAPPING__ORIGIN, COMPARATOR.EQUALS, origin2);
		List<ILabMapping> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertEquals(mapping2, existing.get(0));
		
		query = modelSerice.getQuery(ILabMapping.class);
		query.and(ModelPackage.Literals.ILAB_MAPPING__ITEM, COMPARATOR.EQUALS, item1);
		query.and(ModelPackage.Literals.ILAB_MAPPING__ORIGIN, COMPARATOR.EQUALS, origin1);
		existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertEquals(mapping1, existing.get(0));
		
		modelSerice.remove(mapping1);
		modelSerice.remove(mapping2);
	}
}
