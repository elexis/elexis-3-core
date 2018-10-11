package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.test.AbstractTest;

public class BillingSystemFactorTest extends AbstractTest {
	
	@Test
	public void create(){
		IBillingSystemFactor factor = coreModelService.create(IBillingSystemFactor.class);
		assertNotNull(factor);
		assertTrue(factor instanceof IBillingSystemFactor);
		
		factor.setSystem("testsystem");
		factor.setFactor(0.98);
		factor.setValidFrom(LocalDate.of(2000, 1, 1));
		factor.setValidTo(LocalDate.of(9999, 12, 31));
		assertTrue(coreModelService.save(factor));
		
		Optional<IBillingSystemFactor> loadedFactor =
			coreModelService.load(factor.getId(), IBillingSystemFactor.class);
		assertTrue(loadedFactor.isPresent());
		assertFalse(factor == loadedFactor.get());
		assertEquals(factor, loadedFactor.get());
		assertEquals(factor.getFactor(), loadedFactor.get().getFactor(), 0.001);
		
		coreModelService.remove(factor);
	}
	
	@Test
	public void query(){
		IBillingSystemFactor factor = coreModelService.create(IBillingSystemFactor.class);
		factor.setSystem("testsystem");
		factor.setFactor(0.98);
		factor.setValidFrom(LocalDate.of(2000, 1, 1));
		factor.setValidTo(LocalDate.of(9999, 12, 31));
		assertTrue(coreModelService.save(factor));
		
		IBillingSystemFactor factor1 = coreModelService.create(IBillingSystemFactor.class);
		factor1.setSystem("testsystem");
		factor1.setFactor(0.90);
		factor1.setValidFrom(LocalDate.of(1990, 1, 1));
		factor1.setValidTo(LocalDate.of(1999, 12, 31));
		assertTrue(coreModelService.save(factor1));
		
		IQuery<IBillingSystemFactor> query = coreModelService.getQuery(IBillingSystemFactor.class);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__SYSTEM, COMPARATOR.EQUALS,
			"testsystem");
		List<IBillingSystemFactor> existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(2, existing.size());
		
		query = coreModelService.getQuery(IBillingSystemFactor.class);
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__SYSTEM, COMPARATOR.EQUALS,
			"testsystem");
		query.and(ModelPackage.Literals.IBILLING_SYSTEM_FACTOR__VALID_TO,
			COMPARATOR.GREATER_OR_EQUAL, LocalDate.of(2000, 1, 1));
		existing = query.execute();
		assertNotNull(existing);
		assertFalse(existing.isEmpty());
		assertEquals(1, existing.size());
		assertEquals(factor.getFactor(), existing.get(0).getFactor(), 0.001);
		
		coreModelService.remove(factor);
		coreModelService.remove(factor1);
	}
}
