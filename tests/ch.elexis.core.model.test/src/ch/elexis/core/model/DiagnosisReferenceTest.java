package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

public class DiagnosisReferenceTest extends AbstractTest {
	
	@Test
	public void create(){
		IDiagnosisReference diagnosis = modelService.create(IDiagnosisReference.class);
		assertNotNull(diagnosis);
		assertTrue(diagnosis instanceof IDiagnosisReference);
		
		diagnosis.setCode("test code");
		diagnosis.setReferredClass("test.class.name");
		assertTrue(modelService.save(diagnosis));
		
		Optional<IDiagnosisReference> loadedConfig =
			modelService.load(diagnosis.getId(), IDiagnosisReference.class);
		assertTrue(loadedConfig.isPresent());
		assertFalse(diagnosis == loadedConfig.get());
		assertEquals(diagnosis, loadedConfig.get());
		assertEquals(diagnosis.getCode(), loadedConfig.get().getCode());
		assertEquals(diagnosis.getReferredClass(), loadedConfig.get().getReferredClass());
		
		modelService.remove(diagnosis);
	}
}
