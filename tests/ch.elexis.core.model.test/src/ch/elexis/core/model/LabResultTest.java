package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
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

public class LabResultTest {
	private IModelService modelService;
	
	private IPatient patient1;
	private IPatient patient2;
	
	private ILabItem item1;
	
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
	}
	
	@After
	public void after(){
		modelService.remove(patient1);
		modelService.remove(patient2);
		modelService.remove(item1);
		
		OsgiServiceUtil.ungetService(modelService);
		modelService = null;
	}
	
	@Test
	public void create(){
		ILabResult result = modelService.create(ILabResult.class);
		assertNotNull(result);
		assertTrue(result instanceof ILabResult);
		
		result.setPatient(patient1);
		result.setItem(item1);
		result.setReferenceFemale("<25");
		result.setReferenceMale("<30");
		result.setResult("22.56");
		result.setExtInfo("testInfo", "testInfo");
		assertTrue(modelService.save(result));
		
		Optional<ILabResult> loadedResult = modelService.load(result.getId(), ILabResult.class);
		assertTrue(loadedResult.isPresent());
		assertFalse(result == loadedResult.get());
		assertEquals(result, loadedResult.get());
		assertEquals(result.getItem(), loadedResult.get().getItem());
		assertEquals(result.getReferenceFemale(), loadedResult.get().getReferenceFemale());
		assertEquals(result.getReferenceMale(), loadedResult.get().getReferenceMale());
		assertEquals(result.getResult(), loadedResult.get().getResult());
		assertEquals(result.getExtInfo("testInfo"), loadedResult.get().getExtInfo("testInfo"));
		
		modelService.remove(result);
	}
	
	@Test
	public void query(){
		ILabResult result1 = modelService.create(ILabResult.class);
		result1.setPatient(patient1);
		result1.setItem(item1);
		result1.setReferenceFemale("<25");
		result1.setReferenceMale("<30");
		result1.setResult("22.56");
		result1.setObservationTime(LocalDateTime.of(2018, 1, 1, 10, 0));
		assertTrue(modelService.save(result1));
		
		ILabResult result2 = modelService.create(ILabResult.class);
		result2.setPatient(patient2);
		result2.setItem(item1);
		result2.setReferenceFemale("<25");
		result2.setReferenceMale("<30");
		result2.setResult("35.85");
		result2.setObservationTime(LocalDateTime.of(2018, 1, 2, 15, 0));
		assertTrue(modelService.save(result2));
		
		IQuery<ILabResult> query = modelService.getQuery(ILabResult.class);
		query.and(ModelPackage.Literals.ILAB_RESULT__ITEM, COMPARATOR.EQUALS, item1);
		List<ILabResult> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(2, existing.size());
		
		query = modelService.getQuery(ILabResult.class);
		query.and(ModelPackage.Literals.ILAB_RESULT__PATIENT, COMPARATOR.EQUALS, patient1);
		existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertFalse(result1 == existing.get(0));
		assertEquals(result1, existing.get(0));
		
		query = modelService.getQuery(ILabResult.class);
		query.and(ModelPackage.Literals.ILAB_RESULT__PATIENT, COMPARATOR.EQUALS, patient2);
		query.and(ModelPackage.Literals.ILAB_RESULT__OBSERVATION_TIME, COMPARATOR.GREATER,
			LocalDateTime.of(2018, 1, 2, 13, 0));
		existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertFalse(result2 == existing.get(0));
		assertEquals(result2, existing.get(0));
		assertEquals(result2.getObservationTime(), existing.get(0).getObservationTime());
		
		modelService.remove(result1);
		modelService.remove(result2);
	}
}
