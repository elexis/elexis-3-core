package ch.elexis.core.model.builder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.temporal.ChronoField;

import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class IEncounterBuilderTest extends AbstractTest {
	
	@Override
	public void before(){
		super.before();
		createEncounter();
	}
	
	@Override
	public void after(){
		super.after();
	}
	
	@Test
	public void build(){
		
		// is created via builder
		assertNotNull(encounter.getId());
		assertNotNull(encounter.getLastupdate());
		assertFalse(encounter.isDeleted());
		assertEquals(coverage, encounter.getCoverage());
		assertEquals(mandator, encounter.getMandator());
		assertEquals(patient, encounter.getPatient());
		assertTrue(encounter.isBillable());
		assertTrue(encounter.getDate().get(ChronoField.MINUTE_OF_HOUR) > 0);
	}
	
}
