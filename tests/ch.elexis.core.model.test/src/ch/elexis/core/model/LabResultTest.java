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

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.test.AbstractTest;
import ch.elexis.core.types.LabItemTyp;

public class LabResultTest extends AbstractTest {

	private IPatient patient2;
	private ILabItem item1;

	@Override
	@Before
	public void before() {
		super.before();
		super.createPatient();
		super.createUserSetActiveInContext();

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
	}

	@Override
	@After
	public void after() {
		coreModelService.remove(patient2);
		coreModelService.remove(item1);
		super.after();
	}

	@Test
	public void create() {
		ILabResult result = coreModelService.create(ILabResult.class);
		assertNotNull(result);
		assertTrue(result instanceof ILabResult);

		result.setPatient(patient);
		result.setItem(item1);
		result.setReferenceFemale("<25");
		result.setReferenceMale("<30");
		result.setResult("22.56");
		result.setExtInfo("testInfo", "testInfo");
		coreModelService.save(result);

		Optional<ILabResult> loadedResult = coreModelService.load(result.getId(), ILabResult.class);
		assertTrue(loadedResult.isPresent());
		assertFalse(result == loadedResult.get());
		assertEquals(result, loadedResult.get());
		assertEquals(result.getItem(), loadedResult.get().getItem());
		assertEquals(result.getReferenceFemale(), loadedResult.get().getReferenceFemale());
		assertEquals(result.getReferenceMale(), loadedResult.get().getReferenceMale());
		assertEquals(result.getResult(), loadedResult.get().getResult());
		assertEquals(result.getExtInfo("testInfo"), loadedResult.get().getExtInfo("testInfo"));

		coreModelService.remove(result);
	}

	@Test
	public void getReference() {
		ILabItem item = coreModelService.create(ILabItem.class);
		item.setCode("testItemRef");
		item.setName("test item reference name");
		item.setTyp(LabItemTyp.NUMERIC);
		coreModelService.save(item);
		assertEquals("", item.getReferenceMale());
		assertEquals("", item.getReferenceFemale());

		ILabResult result = coreModelService.create(ILabResult.class);
		result.setPatient(patient);
		result.setItem(item);
		result.setReferenceMale("<0.35");
		result.setResult("3.26");
		coreModelService.save(result);

		ConfigServiceHolder.setUser(Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES, true);
		// if item ref is empty ref of result is used
		assertEquals("<0.35", result.getReferenceMale());
		// test if item ref is used when set
		item.setReferenceMale("<0.34");
		coreModelService.save(item);
		assertEquals("<0.34", result.getReferenceMale());
		ConfigServiceHolder.setUser(Preferences.LABSETTINGS_CFG_LOCAL_REFVALUES, false);
		assertEquals("<0.35", result.getReferenceMale());

		coreModelService.remove(result);
		coreModelService.remove(item);
	}

	@Test
	public void query() {
		ILabResult result1 = coreModelService.create(ILabResult.class);
		result1.setPatient(patient);
		result1.setItem(item1);
		result1.setReferenceFemale("<25");
		result1.setReferenceMale("<30");
		result1.setResult("22.56");
		result1.setObservationTime(LocalDateTime.of(2018, 1, 1, 10, 0));
		coreModelService.save(result1);

		ILabResult result2 = coreModelService.create(ILabResult.class);
		result2.setPatient(patient2);
		result2.setItem(item1);
		result2.setReferenceFemale("<25");
		result2.setReferenceMale("<30");
		result2.setResult("35.85");
		result2.setObservationTime(LocalDateTime.of(2018, 1, 2, 15, 0));
		coreModelService.save(result2);

		IQuery<ILabResult> query = coreModelService.getQuery(ILabResult.class);
		query.and(ModelPackage.Literals.ILAB_RESULT__ITEM, COMPARATOR.EQUALS, item1);
		List<ILabResult> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(2, existing.size());

		query = coreModelService.getQuery(ILabResult.class);
		query.and(ModelPackage.Literals.ILAB_RESULT__PATIENT, COMPARATOR.EQUALS, patient);
		existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertFalse(result1 == existing.get(0));
		assertEquals(result1, existing.get(0));

		query = coreModelService.getQuery(ILabResult.class);
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

		coreModelService.remove(result1);
		coreModelService.remove(result2);
	}
}
