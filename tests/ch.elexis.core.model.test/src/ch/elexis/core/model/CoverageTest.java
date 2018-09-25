package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;

public class CoverageTest extends AbstractTest {
	
	@Override
	public void before(){
		super.before();
		createPatient();
		createCoverage();
	}
	
	@Override
	public void after(){
		removeCoverage();
		removePatient();
		super.after();
	}
	
	@Test
	public void createFindDeleteCoverage(){
		assertTrue(patient.getCoverages().contains(coverage));
		
		assertEquals(patient, coverage.getPatient());
		assertEquals("testCoverage", coverage.getDescription());
		assertEquals("testReason", coverage.getReason());
		assertEquals("testBillingSystem", coverage.getBillingSystem());
		assertNotNull(coverage.getDateFrom());
		assertNull(coverage.getDateTo());
		
		IQuery<ICoverage> query = modelService.getQuery(ICoverage.class);
		query.and(ModelPackage.Literals.ICOVERAGE__PATIENT, COMPARATOR.EQUALS, patient);
		assertEquals(coverage, query.executeSingleResult().get());
		
		modelService.delete(coverage);
		assertFalse(patient.getCoverages().contains(coverage));
	}
}
