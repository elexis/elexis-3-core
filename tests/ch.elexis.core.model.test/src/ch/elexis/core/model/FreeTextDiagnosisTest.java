package ch.elexis.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Optional;

import org.junit.Test;

import ch.elexis.core.test.AbstractTest;

public class FreeTextDiagnosisTest extends AbstractTest {
	
	@Test
	public void create(){
		IFreeTextDiagnosis diagnosis = coreModelService.create(IFreeTextDiagnosis.class);
		assertNotNull(diagnosis);
		assertTrue(diagnosis instanceof IFreeTextDiagnosis);
		
		diagnosis.setText("test free text");
		assertTrue(coreModelService.save(diagnosis));
		
		Optional<IFreeTextDiagnosis> loaded =
			coreModelService.load(diagnosis.getId(), IFreeTextDiagnosis.class);
		assertTrue(loaded.isPresent());
		assertFalse(diagnosis == loaded.get());
		assertEquals(diagnosis, loaded.get());
		assertEquals(diagnosis.getText(), loaded.get().getText());
		
		coreModelService.remove(diagnosis);
	}
}
