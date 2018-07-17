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
import ch.elexis.core.utils.OsgiServiceUtil;

public class LaboratoryTest {
	private IModelService modelSerice;
	
	@Before
	public void before(){
		modelSerice = OsgiServiceUtil.getService(IModelService.class).get();
	}
	
	@After
	public void after(){
		OsgiServiceUtil.ungetService(modelSerice);
		modelSerice = null;
	}
	
	@Test
	public void create(){
		ILaboratory laboratory = modelSerice.create(ILaboratory.class);
		assertNotNull(laboratory);
		assertTrue(laboratory instanceof ILaboratory);
		
		laboratory.setCode("TestLab");
		laboratory.setDescription1("Laboratory Test");
		assertTrue(modelSerice.save(laboratory));
		
		Optional<ILaboratory> loadedLaboratory =
			modelSerice.load(laboratory.getId(), ILaboratory.class);
		assertTrue(loadedLaboratory.isPresent());
		assertFalse(laboratory == loadedLaboratory.get());
		assertEquals(laboratory, loadedLaboratory.get());
		assertEquals(laboratory.getCode(), loadedLaboratory.get().getCode());
		assertEquals(laboratory.getDescription1(), loadedLaboratory.get().getDescription1());
		
		modelSerice.remove(laboratory);
	}
	
	@Test
	public void query(){
		ILaboratory laboratory1 = modelSerice.create(ILaboratory.class);
		laboratory1.setCode("TestLab1");
		laboratory1.setDescription1("Laboratory Test 1");
		modelSerice.save(laboratory1);
		ILaboratory laboratory2 = modelSerice.create(ILaboratory.class);
		laboratory2.setCode("TestLab2");
		laboratory2.setDescription1("Laboratory Test 2");
		modelSerice.save(laboratory2);
		
		IQuery<ILaboratory> query = modelSerice.getQuery(ILaboratory.class);
		query.startGroup();
		query.or(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.LIKE, "%TestLab%");
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "%TestLab%");
		List<ILaboratory> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(2, existing.size());
		
		query = modelSerice.getQuery(ILaboratory.class);
		query.startGroup();
		query.or(ModelPackage.Literals.ICONTACT__CODE, COMPARATOR.LIKE, "%Lab1%");
		query.or(ModelPackage.Literals.ICONTACT__DESCRIPTION1, COMPARATOR.LIKE, "%Lab1%");
		existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertEquals(laboratory1.getCode(), existing.get(0).getCode());
		assertEquals(laboratory1.getDescription1(), existing.get(0).getDescription1());
		
		modelSerice.remove(laboratory1);
		modelSerice.remove(laboratory2);
	}
}
