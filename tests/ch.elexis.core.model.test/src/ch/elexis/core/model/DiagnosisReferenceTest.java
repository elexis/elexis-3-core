package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class DiagnosisReferenceTest extends AbstractTest {
	
	@Test
	public void create(){
		IDiagnosisReference diagnosis = coreModelService.create(IDiagnosisReference.class);
		assertNotNull(diagnosis);
		assertTrue(diagnosis instanceof IDiagnosisReference);
		
		diagnosis.setCode("test code");
		diagnosis.setReferredClass("test.class.name");
		assertTrue(coreModelService.save(diagnosis));
		
		Optional<IDiagnosisReference> loadedConfig =
			coreModelService.load(diagnosis.getId(), IDiagnosisReference.class);
		assertTrue(loadedConfig.isPresent());
		assertFalse(diagnosis == loadedConfig.get());
		assertEquals(diagnosis, loadedConfig.get());
		assertEquals(diagnosis.getCode(), loadedConfig.get().getCode());
		assertEquals(diagnosis.getReferredClass(), loadedConfig.get().getReferredClass());
		
		coreModelService.remove(diagnosis);
	}
}
