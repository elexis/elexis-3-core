package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.types.LabItemTyp;

public class LabItemTest extends AbstractTest {
	
	@Test
	public void create(){
		ILabItem item = modelService.create(ILabItem.class);
		assertNotNull(item);
		assertTrue(item instanceof ILabItem);
		
		item.setCode("testItem");
		item.setName("test item name");
		item.setReferenceFemale("<25");
		item.setReferenceMale("<30");
		item.setTyp(LabItemTyp.NUMERIC);
		assertTrue(modelService.save(item));
		
		Optional<ILabItem> loadedItem = modelService.load(item.getId(), ILabItem.class);
		assertTrue(loadedItem.isPresent());
		assertFalse(item == loadedItem.get());
		assertEquals(item, loadedItem.get());
		assertEquals(item.getCode(), loadedItem.get().getCode());
		assertEquals(item.getReferenceFemale(), loadedItem.get().getReferenceFemale());
		assertEquals(item.getReferenceMale(), loadedItem.get().getReferenceMale());
		assertEquals(item.getTyp(), loadedItem.get().getTyp());
		
		modelService.remove(item);
	}
	
	@Test
	public void query(){
		ILabItem item1 = modelService.create(ILabItem.class);
		item1.setCode("testItem1");
		item1.setName("test item 1");
		item1.setReferenceFemale("<25");
		item1.setReferenceMale("<30");
		item1.setTyp(LabItemTyp.NUMERIC);
		assertTrue(modelService.save(item1));
		
		ILabItem item2 = modelService.create(ILabItem.class);
		item2.setCode("testItem2");
		item2.setName("test item 2");
		item2.setReferenceFemale("<25");
		item2.setReferenceMale("<30");
		item2.setTyp(LabItemTyp.TEXT);
		assertTrue(modelService.save(item2));
		
		IQuery<ILabItem> query = modelService.getQuery(ILabItem.class);
		query.and(ModelPackage.Literals.ILAB_ITEM__CODE, COMPARATOR.EQUALS, "testItem2");
		List<ILabItem> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertFalse(item2 == existing.get(0));
		assertEquals(item2, existing.get(0));
		assertEquals(item2.getCode(), existing.get(0).getCode());
		assertEquals(LabItemTyp.TEXT, existing.get(0).getTyp());
		
		modelService.remove(item1);
		modelService.remove(item2);
	}
}
